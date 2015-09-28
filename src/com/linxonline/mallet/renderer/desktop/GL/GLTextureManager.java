package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.color.ColorSpace ;
import javax.media.opengl.* ;
import javax.imageio.* ;
import java.io.* ;
import javax.imageio.stream.* ;

import java.nio.* ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.desktop.* ;
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
		The BufferedImages are then binded to OpenGL and added 
		to resources in order. If we don't do this images may be 
		binded to OpenGL out of order causing significant performance 
		degradation.
	*/
	private final ArrayList<Tuple<String, BufferedImage>> toBind = new ArrayList<Tuple<String, BufferedImage>>() ;
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

	/**
		Currently two OpenGL image formats are supported: GL_RGBA and GL_ABGR_EXT.
		It's set to GL_RGBA by default due to the extension potentially not 
		being available, though unlikely. BufferedImage by default orders the channels ABGR.
	*/
	protected int imageFormat = GL3.GL_RGBA ;

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
				// We want to allocate the key for the resource so the texture 
				// is not reloaded if another object wishes to use it before 
				// the texture has fully loaded.
				// The Renderer should skip the texture, until it is finally 
				// available to render/
				add( _file, null ) ;

				TextureThread load = new TextureThread( "LOAD_TEXTURE", _file ) ;
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
			// GLRenderer will continuosly call get() untill it 
			// recieves a Texture, so we only need to bind 
			// textures that are waiting for the OpenGL context 
			// when the render requests it.
			for( final Tuple<String, BufferedImage> tuple : toBind )
			{
				add( tuple.getLeft(), bind( tuple.getRight() ) ) ;
			}
			toBind.clear() ;
		}

		final Texture texture = super.get( _file ) ;
		if( texture != null )
		{
			// Register the texture for being used.
			// The renderer should call unregister when 
			// the calling Render Event is no longer being used.
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
		Change the image format used to bind textures.
		GL_RGBA and GL_ABGR_EXT are supported.
	*/
	public void setImageFormat( final int _format )
	{
		imageFormat = _format ;
	}

	public Texture bind( final BufferedImage _image )
	{
		return bind( _image, InternalFormat.COMPRESSED ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public Texture bind( final BufferedImage _image, final InternalFormat _format )
	{
		final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;
		if( gl == null )
		{
			System.out.println( "GL context doesn't exist" ) ;
			return null ;
		}

		gl.glEnable( GL.GL_TEXTURE_2D ) ;

		final int textureID = glGenTextures( gl ) ;
		gl.glBindTexture( GL3.GL_TEXTURE_2D, textureID ) ;

		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT ) ;
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT ) ;
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR ) ;
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR ) ;

		final int width = _image.getWidth() ;
		final int height = _image.getHeight() ;
		final int channels = _image.getSampleModel().getNumBands() ;
		int internalFormat = GL3.GL_RGB ;

		if( gl.isExtensionAvailable( "GL_EXT_abgr" ) == true )
		{
			switch( channels )
			{
				case 4 : imageFormat = GL2.GL_ABGR_EXT ;  break ;
				case 3 : imageFormat = GL3.GL_BGR ;   break ;
				case 1 : imageFormat = GL3.GL_RED ; break ;
			}
		}
		else
		{
			switch( channels )
			{
				case 4 : imageFormat = GL3.GL_RGBA ;      break ;
				case 3 : imageFormat = GL3.GL_RGB ;       break ;
				case 1 : imageFormat = GL3.GL_RED ; break ;
			}
		}

		gl.glPixelStorei( GL3.GL_UNPACK_ALIGNMENT, 1 ) ;
		gl.glTexImage2D( GL3.GL_TEXTURE_2D, 
						 0, 
						 getGLInternalFormat( channels, _format ), 
						 width, 
						 height, 
						 0, 
						 imageFormat, 
						 GL3.GL_UNSIGNED_BYTE, 
						 getByteBuffer( _image ) ) ;

		gl.glGenerateMipmap( GL3.GL_TEXTURE_2D ) ;
		gl.glBindTexture( GL.GL_TEXTURE_2D, 0 ) ;			// Reset to default texture

		return new Texture( new GLImage( textureID, width, height ) ) ;
	}

	private int getGLInternalFormat( final int _channels, final InternalFormat _format )
	{
		// If texture compression is enabled then use texture compression.
		// Unless the texture request specifically requests not to use compression.
		// If compression is disabled then never use compression, even if specified.
		// Uncompressed will provide the best results, compressed can provide blury or 
		// create artifacts, for example on Intel chips. Certain textures should never 
		// be compressed such as fonts.
		final boolean useCompression = GlobalConfig.getBoolean( "TEXTURECOMPRESSION", true ) ;
		final InternalFormat format = ( useCompression == true ) ? _format : InternalFormat.UNCOMPRESSED ;

		switch( _channels )
		{
			case 4 :
			{
				switch( format )
				{
					case COMPRESSED   : return GL3.GL_COMPRESSED_RGBA ;
					case UNCOMPRESSED : return GL3.GL_RGBA ;
				}
			}
			case 3 :
			{
				switch( format )
				{
					case COMPRESSED   : return GL3.GL_COMPRESSED_RGB ;
					case UNCOMPRESSED : return GL3.GL_RGB ;
				}
			}
			case 1 :
			{
				return GL3.GL_RED ;
			}
		}

		return GL3.GL_RGB ;
	}

	/**
		Returns a ByteBuffer of BufferedImage data.
		Ensure BufferedImage is of 4BYTE_ABGR type.
		If imageFormat is set to GL_RGBA, byte stream will be converted.
	*/
	private ByteBuffer getByteBuffer( final BufferedImage _image )
	{
		final DataBuffer buffer = _image.getRaster().getDataBuffer() ;
		final int type = buffer.getDataType() ;

		if( type == DataBuffer.TYPE_BYTE )
		{
			final byte[] data = ( (  DataBufferByte )  buffer).getData() ;
			if( imageFormat == GL3.GL_RGBA )
			{
				convertABGRtoRGBA( data ) ;
			}

			return ByteBuffer.wrap( data ) ;
		}

		System.out.println( "Failed to determine DataBuffer type." ) ;
		return null ;
	}

	private byte[] convertABGRtoRGBA( final byte[] _data )
	{
		byte alpha, red, green, blue ;
		final int size = _data.length ;
		for( int i = 0; i < size; i += 4 )
		{
			red   = _data[i + 3] ;
			green = _data[i + 2] ;
			blue  = _data[i + 1] ;
			alpha = _data[i] ;

			_data[i]     = red ;
			_data[i + 1] = green ;
			_data[i + 2] = blue ;
			_data[i + 3] = alpha ;
		}

		return _data ;
	}

	private int glGenTextures( GL3 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenTextures( 1, id, 0 ) ;

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
				if( meta != null)
				{
					return meta ;
				}

				final FileStream file = GlobalFileSystem.getFile( _path ) ;
				if( file.exists() == false )
				{
					Logger.println( "No Texture found to create Meta: " + _path, Logger.Verbosity.NORMAL ) ;
					return new MalletTexture.Meta( _path, 0, 0 ) ;
				}

				return addMeta( _path, createMeta( _path, file ) ) ; 
			}
		}

		private MalletTexture.Meta addMeta( final String _path, final MalletTexture.Meta _meta )
		{
			if( _meta != null )
			{
				imageMetas.put( _path, _meta ) ;
				return _meta ;
			}

			Logger.println( "Failed to create Texture Meta: " + _path, Logger.Verbosity.NORMAL ) ;
			return new MalletTexture.Meta( _path, 0, 0 ) ;
		}

		private MalletTexture.Meta createMeta( final String _path, final FileStream _file )
		{
			final DesktopByteIn desktopIn = ( DesktopByteIn )_file.getByteInStream() ;
			return createMeta( _path, desktopIn.getInputStream() ) ;
		}

		private static MalletTexture.Meta createMeta( final String _path, final InputStream _stream )
		{
			try( final ImageInputStream in = ImageIO.createImageInputStream( _stream ) )
			{
				final Iterator<ImageReader> readers = ImageIO.getImageReaders( in ) ;
				if( readers.hasNext() )
				{
					final ImageReader reader = readers.next() ;
					try
					{
						reader.setInput( in ) ;
						// Add additional Meta information to MalletTexture as 
						// and when it becomes needed. It shouldn't hold too much (RGB, RGBA, Mono, endinese, 32, 24-bit, etc)
						// data as a game-developer shouldn't need detailed information.
						return new MalletTexture.Meta( _path, reader.getHeight( 0 ), reader.getWidth( 0 ) ) ;
					}
					finally
					{
						reader.dispose() ;
					}
				}
			}
			catch( IOException ex )
			{
				ex.printStackTrace() ;
			}

			return null ;
		}
	}

	private class TextureThread extends Thread
	{
		private final String texturePath ;

		public TextureThread( final String _name, final String _file )
		{
			super( _name ) ;
			texturePath = _file ;
		}

		public void run()
		{
			final FileStream file = GlobalFileSystem.getFile( texturePath ) ;
			if( file.exists() == false )
			{
				Logger.println( "Failed to create Texture: " + texturePath, Logger.Verbosity.NORMAL ) ;
				return ;
			}
		
			try
			{
				final DesktopByteIn in = ( DesktopByteIn )file.getByteInStream() ;
				final InputStream stream = in.getInputStream() ;
				final BufferedImage image = ImageIO.read( stream ) ;
				in.close() ;

				synchronized( toBind )
				{
					// We don't want to bind the BufferedImage now
					// as that will take control of the OpenGL context.
					toBind.add( new Tuple<String, BufferedImage>( texturePath, image ) ) ;
				}
			}
			catch( IOException ex )
			{
				ex.printStackTrace() ;
			}
		}
	}
}
