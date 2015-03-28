package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.io.InputStream ;
import java.nio.* ;

import android.opengl.GLES11 ;
import android.opengl.GLUtils;

import android.graphics.BitmapFactory ;
import android.graphics.Bitmap ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.resources.texture.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;

public class GLTextureManager extends AbstractManager<Texture>
{
	/**
		When loading a texture the TextureManager will stream the 
		image content a-synchronously.
		To ensure the textures are added safely to resources we 
		temporarily store the images in a queue.
		The Bitmap are then binded to OpenGL and added 
		to resources in order. If we don't do this images may be 
		binded to OpenGL out of order causing significant performance 
		degradation.
	*/
	private final ArrayList<Tuple<String, Bitmap>> toBind = new ArrayList<Tuple<String, Bitmap>>() ;
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

	public GLTextureManager()
	{
		final ResourceLoader<Texture> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<Texture>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public Texture load( final String _file, final Settings _settings )
			{
				return loadTextureASync( _file ) ;
			}

			protected Texture loadTextureASync( final String _file )
			{
				final Thread load = new Thread( "LOAD_TEXTURE" )
				{
					public void run()
					{
						final byte[] image = ByteReader.readBytes( _file ) ;
						if( _file == null )
						{
							Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
							return ;
						}

						final Bitmap bitmap = BitmapFactory.decodeByteArray( image, 0, image.length ) ;
						synchronized( toBind )
						{
							// We don't want to bind the Bitmap now
							// as that will take control of the OpenGL context.
							toBind.add( new Tuple<String, Bitmap>( _file, bitmap ) ) ;
						}
					}
				} ;

				// We want to allocate the key for the resource so the texture 
				// is not reloaded if another object wishes to use it before 
				// the texture has fully loaded.
				// The Renderer should skip the texture, until it is finally 
				// available to render/
				add( _file, null ) ;
				load.start() ;
				return null ;
			}
		} ) ;
	}

	@Override
	public Texture get( final String _file )
	{
		synchronized( toBind )
		{
			for( final Tuple<String, Bitmap> tuple : toBind )
			{
				final Bitmap bitmap = tuple.getRight() ;
				add( tuple.getLeft(), bind( bitmap ) ) ;
				bitmap.recycle() ;
			}
			toBind.clear() ;
		}

		final Texture texture = super.get( _file ) ;
		if( texture != null )
		{
			texture.register() ;
		}

		return texture ;
	}

	/**
		Return the meta information associated with an image
		defined by _path.
		If the meta data has yet to be generated, create it 
		and store the meta data in imageMetas. This hashmap 
		is persistant across the runtime of the renderer.
		If the meta data changes from one call to the next, 
		the meta data stored is NOT updated.
		FileStream would need to be updated to support 
		file modification timestamps.
	*/
	public MalletTexture.Meta getMeta( final String _path )
	{
		return metaGenerator.getMeta( _path ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public Texture bind( final Bitmap _image )
	{
		GLES11.glEnable( GLES11.GL_TEXTURE_2D ) ;

		final int textureID = glGenTextures() ;
		GLES11.glBindTexture( GLES11.GL_TEXTURE_2D, textureID ) ;

		// Create Nearest Filtered Texture
		GLES11.glTexParameterf( GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MIN_FILTER, GLES11.GL_LINEAR ) ;
		GLES11.glTexParameterf( GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_MAG_FILTER, GLES11.GL_LINEAR ) ;
 
		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		GLES11.glTexParameterf( GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_S, GLES11.GL_CLAMP_TO_EDGE ) ;
		GLES11.glTexParameterf( GLES11.GL_TEXTURE_2D, GLES11.GL_TEXTURE_WRAP_T, GLES11.GL_REPEAT ) ;

		GLUtils.texImage2D( GLES11.GL_TEXTURE_2D, 0, _image, 0 ) ;

		return new Texture( new GLImage( textureID, _image.getWidth(), _image.getHeight() ) ) ;
	}

	private int glGenTextures()
	{
		final int[] id = new int[1] ;
		GLES11.glGenTextures( 1, id, 0 ) ;

		return id[0] ;
	}

	protected static class MetaGenerator
	{
		private final HashMap<String, MalletTexture.Meta> imageMetas = new HashMap<String, MalletTexture.Meta>() ;

		/**
			Return the meta information associated with an image
			defined by _path.
			If the meta data has yet to be generated, create it 
			and store the meta data in imageMetas. This hashmap 
			is persistant across the runtime of the renderer.
			If the meta data changes from one call to the next, 
			the meta data stored is NOT updated.
			FileStream would need to be updated to support 
			file modification timestamps.
		*/
		public MalletTexture.Meta getMeta( final String _path )
		{
			synchronized( imageMetas )
			{
				MalletTexture.Meta meta = imageMetas.get( _path ) ;
				if( meta == null )
				{
					final FileStream file = GlobalFileSystem.getFile( _path ) ;
					if( file.exists() == true )
					{
						final AndroidByteIn desktopIn = ( AndroidByteIn )file.getByteInStream() ;
						meta = createMeta( _path, desktopIn.getInputStream() ) ;
						if( meta != null )
						{
							imageMetas.put( _path, meta ) ;
							return meta ;
						}
					}
				}
			}

			Logger.println( "Failed to create Texture Meta: " + _path, Logger.Verbosity.NORMAL ) ;
			return new MalletTexture.Meta( _path, 0, 0 ) ;
		}

		private static MalletTexture.Meta createMeta( final String _path, final InputStream _stream )
		{
			final BitmapFactory.Options options = new BitmapFactory.Options() ;
			options.inJustDecodeBounds = true ;

			BitmapFactory.decodeStream( _stream, null, options ) ;
			int width = options.outWidth ;
			int height = options.outHeight ;

			return new MalletTexture.Meta( _path, width, height ) ;
		}
	}
}
