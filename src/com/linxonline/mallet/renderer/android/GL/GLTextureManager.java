package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.io.InputStream ;
import java.nio.* ;

import android.util.DisplayMetrics ;
import android.opengl.GLES11 ;
import android.opengl.GLUtils;
import android.opengl.ETC1Util ;
import android.opengl.ETC1Util.ETC1Texture ;
import android.opengl.ETC1 ;

import android.graphics.BitmapFactory ;
import android.graphics.Bitmap ;

import com.linxonline.mallet.system.GlobalConfig ;
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
	
	private boolean supportedETC1 = ETC1Util.isETC1Supported() ;

	public GLTextureManager()
	{
		final ResourceLoader<Texture> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<Texture>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".PNG", ".png" ) ;
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
						final FileStream file = GlobalFileSystem.getFile( _file ) ;
						if( file.exists() == false )
						{
							Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
							return ;
						}

						final AndroidByteIn in = ( AndroidByteIn )file.getByteInStream() ;
						final Bitmap bitmap = BitmapFactory.decodeStream( in.getInputStream() ) ;
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

		loader.add( new ResourceDelegate<Texture>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".JPG", ".jpg", ".JPEG", ".jpeg" ) ;
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
						final FileStream file = GlobalFileSystem.getFile( _file ) ;
						if( file.exists() == false )
						{
							Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
							return ;
						}

						// We know Jpegs do not support an alpha channel, 
						// so we can generate a Bitmap using 2 bytes instead of 4.
						final BitmapFactory.Options options = new BitmapFactory.Options() ;
						options.inPreferredConfig = Bitmap.Config.RGB_565 ;

						final AndroidByteIn in = ( AndroidByteIn )file.getByteInStream() ;
						final Bitmap bitmap = BitmapFactory.decodeStream( in.getInputStream(), null, options ) ;

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

	public Texture bind( final Bitmap _image )
	{
		return bind( _image, InternalFormat.COMPRESSED ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public Texture bind( final Bitmap _image, final InternalFormat _format )
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

		// If texture compression is enabled then use texture compression.
		// Unless the texture request specifically requests not to use compression.
		// If compression is disabled then never use compression, even if specified.
		// Uncompressed will provide the best results, compressed can provide blury or 
		// create artifacts, for example on Intel chips. Certain textures should never 
		// be compressed such as fonts.
		//final boolean useCompression = GlobalConfig.getBoolean( "TEXTURECOMPRESSION", true ) ;
		//final InternalFormat format = ( useCompression == true ) ? _format : InternalFormat.UNCOMPRESSED ;

		//if( format == InternalFormat.UNCOMPRESSED || _image.getConfig() == Bitmap.Config.ARGB_8888 )
		//{
			// If the texture has been flagged to not use 
			// compression or contains an alpha channel, then 
			// do not compress the texture.
			GLES11.glPixelStorei( GLES11.GL_UNPACK_ALIGNMENT, 1 ) ;
			GLUtils.texImage2D( GLES11.GL_TEXTURE_2D, 0, _image, 0 ) ;
		//}
		/*else		// This is too slow
		{
			// We don't want the memory hit of converting an ARGB_8888 
			// that doesn't use alpha to RGB_565. Too many copies!
			final int width = _image.getWidth() ;
			final int height = _image.getHeight() ;
			final int size = _image.getRowBytes() * height ;

			// Personally this implementation still has 1 too many copies.
			final ByteBuffer buffer = ByteBuffer.allocateDirect( size ) ;
			buffer.order( ByteOrder.nativeOrder() ) ;

			_image.copyPixelsToBuffer( buffer ) ;
			buffer.position( 0 ) ;

			// RGB_565 is 2 bytes per pixel
			final ETC1Texture etc1tex = ETC1Util.compressTexture( buffer, width, height, 2, 2 * width ) ;
			ETC1Util.loadTexture( GLES11.GL_TEXTURE_2D, 0, 0, GLES11.GL_RGB, GLES11.GL_UNSIGNED_SHORT_5_6_5, etc1tex ) ;
		}*/

		return new Texture( new GLImage( textureID, _image.getWidth(), _image.getHeight() ) ) ;
	}

	private int getGLInternalFormat( final Bitmap.Config _config, final InternalFormat _format )
	{
		/*switch( _config )
		{
			case ARGB_4444 :
			case ARGB_8888 :
			{
				switch( _format )
				{
					case COMPRESSED   : return GLES11.GL_COMPRESSED_RGBA ;
					case UNCOMPRESSED : return GLES11.GL_RGBA ;
				}
			}
			case RGB_565  :
			{
				switch( _format )
				{
					case COMPRESSED   : return GLES11.GL_COMPRESSED_RGB ;
					case UNCOMPRESSED : return GLES11.GL_RGB ;
				}
			}
		}*/

		return GLES11.GL_RGB ;
	}

	private int glGenTextures()
	{
		final int[] id = new int[1] ;
		GLES11.glGenTextures( 1, id, 0 ) ;

		return id[0] ;
	}

	public enum InternalFormat
	{
		COMPRESSED,
		UNCOMPRESSED
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
		public synchronized MalletTexture.Meta getMeta( final String _path )
		{
			MalletTexture.Meta meta = imageMetas.get( _path ) ;
			if( meta != null )
			{
				return meta ;
			}

			final FileStream file = GlobalFileSystem.getFile( _path ) ;
			if( file.exists() == false )
			{
				Logger.println( "No Texture found to create Meta: " + _path, Logger.Verbosity.NORMAL ) ;
				return new MalletTexture.Meta( _path, 0, 0 ) ;
			}

			final AndroidByteIn in = ( AndroidByteIn )file.getByteInStream() ;
			meta = createMeta( _path, in.getInputStream() ) ;
			if( meta != null )
			{
				imageMetas.put( _path, meta ) ;
				return meta ;
			}

			Logger.println( "Unable to create meta data: " + _path, Logger.Verbosity.NORMAL ) ;
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
