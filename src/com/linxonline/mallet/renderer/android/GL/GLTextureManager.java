package com.linxonline.mallet.renderer.android.GL ;

import java.util.List ;
import java.util.Map ;
import java.io.InputStream ;
import java.nio.* ;

import android.util.DisplayMetrics ;
import android.opengl.GLES20 ;
import android.opengl.GLUtils;
import android.opengl.ETC1Util ;
import android.opengl.ETC1Util.ETC1Texture ;
import android.opengl.ETC1 ;

import android.graphics.BitmapFactory ;
import android.graphics.Bitmap ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.io.reader.ByteReader ;
import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

import com.linxonline.mallet.renderer.* ;

public class GLTextureManager extends AbstractManager<GLImage>
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
	private final List<Tuple<String, Bitmap>> toBind = MalletList.<Tuple<String, Bitmap>>newList() ;
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

	private final boolean supportedETC1 = ETC1Util.isETC1Supported() ;
	protected GLWorldState worldState ;

	public GLTextureManager()
	{
		final ResourceLoader<GLImage> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<GLImage>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".PNG", ".png" ) ;
			}

			public GLImage load( final String _file )
			{
				return loadTextureASync( _file ) ;
			}

			protected GLImage loadTextureASync( final String _file )
			{
				final Thread load = new Thread( "LOAD_TEXTURE" )
				{
					public void run()
					{
						System.out.println( "Loading Texture: " + _file ) ;
						final FileStream file = GlobalFileSystem.getFile( _file ) ;
						if( file.exists() == false )
						{
							Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
							return ;
						}

						final AndroidByteIn in = ( AndroidByteIn )file.getByteInStream() ;
						final Bitmap bitmap = BitmapFactory.decodeStream( in.getInputStream() ) ;
						file.close() ;
						
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
				put( _file, null ) ;

				load.start() ;
				return null ;
			}
		} ) ;

		loader.add( new ResourceDelegate<GLImage>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".JPG", ".jpg", ".JPEG", ".jpeg" ) ;
			}

			public GLImage load( final String _file )
			{
				return loadTextureASync( _file ) ;
			}

			protected GLImage loadTextureASync( final String _file )
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
						file.close() ;

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
				put( _file, null ) ;

				load.start() ;
				return null ;
			}
		} ) ;
	}

	public void setWorldState( final GLWorldState _worldState )
	{
		worldState = _worldState ;
	}

	@Override
	public GLImage get( final String _file )
	{
		synchronized( toBind )
		{
			// GLRenderer will continuosly call get() until it 
			// recieves a Texture, so we only need to bind 
			// textures that are waiting for the OpenGL context 
			// when the render requests it.
			if( toBind.isEmpty() == false )
			{
				final int size = toBind.size() ;
				for( int i = 0; i < size; i++ )
				{
					final Tuple<String, Bitmap> tuple = toBind.get( i ) ;
					final Bitmap bitmap = tuple.getRight() ;
					put( tuple.getLeft(), bind( bitmap ) ) ;
					bitmap.recycle() ;
				}
				toBind.clear() ;
			}
		}

		final GLImage image = super.get( _file ) ;
		if( image != null )
		{
			//System.out.println( "GLImage: " + _file ) ;
			return image ;
		}

		final GLWorld world = worldState.getWorld( _file ) ;
		if( world != null )
		{
			return world.getImage() ;
		}

		return null ;
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

	public GLImage bind( final Bitmap _image )
	{
		return bind( _image, InternalFormat.COMPRESSED ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public GLImage bind( final Bitmap _image, final InternalFormat _format )
	{
		GLES20.glEnable( GLES20.GL_TEXTURE_2D ) ;

		final int textureID = glGenTextures() ;
		GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, textureID ) ;

		// Create Nearest Filtered Texture
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR ) ;
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR ) ;
 
		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE ) ;
		GLES20.glTexParameterf( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT ) ;

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
			GLES20.glPixelStorei( GLES20.GL_UNPACK_ALIGNMENT, 1 ) ;
			GLUtils.texImage2D( GLES20.GL_TEXTURE_2D, 0, _image, 0 ) ;
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
			ETC1Util.loadTexture( GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, etc1tex ) ;
		}*/

		final long estimatedConsumption = _image.getWidth() * _image.getHeight() * ( 3 * 8 ) ;
		return new GLImage( textureID, estimatedConsumption ) ;
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
					case COMPRESSED   : return GLES20.GL_COMPRESSED_RGBA ;
					case UNCOMPRESSED : return GLES20.GL_RGBA ;
				}
			}
			case RGB_565  :
			{
				switch( _format )
				{
					case COMPRESSED   : return GLES20.GL_COMPRESSED_RGB ;
					case UNCOMPRESSED : return GLES20.GL_RGB ;
				}
			}
		}*/

		return GLES20.GL_RGB ;
	}

	private int glGenTextures()
	{
		final int[] id = new int[1] ;
		GLES20.glGenTextures( 1, id, 0 ) ;

		return id[0] ;
	}

	public enum InternalFormat
	{
		COMPRESSED,
		UNCOMPRESSED
	}

	/**
		Retains meta information about textures.
		A texture can be loaded and used by the renderer,
		without storing the meta data.
	*/
	protected static class MetaGenerator
	{
		private final Map<String, MalletTexture.Meta> imageMetas = MalletMap.<String, MalletTexture.Meta>newMap() ;

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
			if( in != null )
			{
				meta = createMeta( _path, in.getInputStream() ) ;
				if( meta != null )
				{
					imageMetas.put( _path, meta ) ;
					file.close() ;
					return meta ;
				}
			}

			Logger.println( "Unable to create meta data: " + _path, Logger.Verbosity.NORMAL ) ;
			file.close() ;
			return new MalletTexture.Meta( _path, 0, 0 ) ;
		}

		private static MalletTexture.Meta createMeta( final String _path, final InputStream _stream )
		{
			final BitmapFactory.Options options = new BitmapFactory.Options() ;
			options.inJustDecodeBounds = true ;

			BitmapFactory.decodeStream( _stream, null, options ) ;
			final int width = options.outWidth ;
			final int height = options.outHeight ;

			return new MalletTexture.Meta( _path, height, width ) ;
		}
	}
}
