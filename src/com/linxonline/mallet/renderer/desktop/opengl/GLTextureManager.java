package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.Map ;
import java.util.Iterator ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.color.ColorSpace ;
import java.awt.geom.AffineTransform ;
import javax.imageio.* ;
import java.io.* ;
import javax.imageio.stream.* ;

import java.nio.* ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.desktop.* ;
import com.linxonline.mallet.io.AbstractManager ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.Parallel ;

import com.linxonline.mallet.renderer.* ;

public final class GLTextureManager extends AbstractManager<String, GLImage>
{
	/**
		Limit the number of textures that can be loaded
		to the GPU on any one render cycle.
		This should allow the engine to be responsive
		if lots of textures are loaded in one go.
	*/
	private static final int TEXTURE_BIND_LIMIT = 1 ;

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
	private final List<Tuple<String, BufferedImage[]>> toBind = MalletList.<Tuple<String, BufferedImage[]>>newList() ;
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

	/**
		Currently two OpenGL image formats are supported: GL_RGBA and GL_ABGR_EXT.
		It's set to GL_RGBA by default due to the extension potentially not 
		being available, though unlikely. BufferedImage by default orders the channels ABGR.
	*/
	private int imageFormat = MGL.GL_RGBA ;
	private int bindCount = 0 ;

	public GLTextureManager()
	{
		ImageIO.setUseCache( false ) ;

		final ResourceLoader<String, GLImage> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<String, GLImage>()
		{
			@Override
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".jpg", ".JPG", ".JPEG", ".jpeg", ".png", ".PNG" ) ;
			}

			@Override
			public GLImage load( final String _file )
			{
				Parallel.run( new TextureRunner( _file ) ) ;
				return null ;
			}
		} ) ;
	}

	public void resetBindCount()
	{
		bindCount = 0 ;
	}

	public boolean texturesLoaded()
	{
		return toBind.isEmpty() ;
	}

	@Override
	public GLImage get( final String _file )
	{
		synchronized( toBind )
		{
			// GLRenderer will continuously call get() until it 
			// receives a Texture, so we only need to bind 
			// textures that are waiting for the OpenGL context 
			// when the render requests it.
			while( toBind.isEmpty() == false && bindCount++ < TEXTURE_BIND_LIMIT  )
			{
				final Tuple<String, BufferedImage[]> tuple = toBind.remove( toBind.size() - 1 ) ;
				put( tuple.getLeft(), bind( tuple.getRight() ) ) ;
			}
		}

		return super.get( _file ) ;
	}

	/**
		Return the meta information associated with an image
		defined by _path.
		If the meta data has yet to be generated, create it 
		and store the meta data in imageMetas. This hashmap 
		is persistent across the runtime of the renderer.
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

	public GLImage bind( final BufferedImage[] _images )
	{
		return bind( _images, InternalFormat.COMPRESSED ) ;
	}

	public GLImage bind( final BufferedImage _image, final InternalFormat _format )
	{
		return bind( new BufferedImage[] { _image }, _format ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public GLImage bind( final BufferedImage[] _images, final InternalFormat _format )
	{
		final int textureID = glGenTextures() ;
		MGL.glBindTexture( MGL.GL_TEXTURE_2D, textureID ) ;

		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_CLAMP_TO_EDGE ) ;
		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;
		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_LINEAR ) ;
		MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_LINEAR_MIPMAP_LINEAR ) ;

		final BufferedImage base = _images[0] ;
		final int baseWidth = base.getWidth() ;
		final int baseHeight = base.getHeight() ;
		final int channels = base.getSampleModel().getNumBands() ;
		int internalFormat = MGL.GL_RGB ;

		if( MGL.isExtensionAvailable( "GL_EXT_abgr" ) == true )
		{
			switch( channels )
			{
				case 4  : imageFormat = MGL.GL_ABGR_EXT ; break ;
				default :
				case 3  : imageFormat = MGL.GL_BGR ; break ;
				case 1  : imageFormat = MGL.GL_RED ; break ;
			}
		}
		else
		{
			switch( channels )
			{
				case 4  : imageFormat = MGL.GL_RGBA ; break ;
				default :
				case 3  : imageFormat = MGL.GL_RGB ; break ;
				case 1  : imageFormat = MGL.GL_RED ; break ;
			}
		}

		MGL.glPixelStorei( MGL.GL_UNPACK_ALIGNMENT, 1 ) ;

		for( int i = 0; i < _images.length; ++i )
		{
			final BufferedImage image = _images[i] ;
			if( image == null )
			{
				System.out.println( "Texture level " + i + " not available skipping." ) ;
				continue ;
			}

			final int mipWidth = image.getWidth() ;
			final int mipHeight = image.getHeight() ;

			MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 
							i, 
							getGLInternalFormat( channels, _format ), 
							mipWidth, 
							mipHeight, 
							0, 
							imageFormat, 
							MGL.GL_UNSIGNED_BYTE, 
							getByteBuffer( image ) ) ;
		}
		//MGL.glBindTexture( MGL.GL_TEXTURE_2D, 0 ) ;			// Reset to default texture
		//GLRenderer.handleError( "Reset Bind Texture", gl ) ;

		final long estimatedConsumption = baseWidth * baseHeight * ( channels * 8 ) ;
		return new GLImage( textureID, estimatedConsumption ) ;
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
			case 4  :
			{
				switch( format )
				{
					case COMPRESSED   : return MGL.GL_COMPRESSED_RGBA ;
					default           :
					case UNCOMPRESSED : return MGL.GL_RGBA ;
				}
			}
			default :
			case 3  :
			{
				switch( format )
				{
					case COMPRESSED   : return MGL.GL_COMPRESSED_RGB ;
					default           :
					case UNCOMPRESSED : return MGL.GL_RGB ;
				}
			}
			case 1  :
			{
				return MGL.GL_RED ;
			}
		}
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
			if( imageFormat == MGL.GL_RGBA )
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

	private int glGenTextures()
	{
		final int[] id = new int[1] ;
		MGL.glGenTextures( 1, id, 0 ) ;

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
		public MalletTexture.Meta getMeta( final String _path )
		{
			synchronized( imageMetas )
			{
				final MalletTexture.Meta meta = imageMetas.get( _path ) ;
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
			MalletTexture.Meta meta = null ; 
			try( final DesktopByteIn desktopIn = ( DesktopByteIn )_file.getByteInStream() )
			{
				meta = createMeta( _path, desktopIn.getInputStream() ) ;
				return meta ;
			}
			catch( Exception ex )
			{
				return meta ;
			}
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
						return new MalletTexture.Meta( _path, reader.getWidth( 0 ), reader.getHeight( 0 ) ) ;
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

	private class TextureRunner implements Parallel.IRun
	{
		private final String texturePath ;

		public TextureRunner( final String _file )
		{
			texturePath = _file ;
		}

		private int calculateMipMapLevels( int _width, int _height )
		{
			int levels = 1 ;	// base level

			do
			{
				_width /= ( _width > 1 ) ? 2 : 1 ;
				_height /= ( _height > 1 ) ? 2 : 1 ;

				levels += 1 ;
			} while( _width > 1 || _height > 1 ) ;

			return levels ;
		}

		@Override
		public void run()
		{
			final FileStream file = GlobalFileSystem.getFile( texturePath ) ;
			if( file.exists() == false )
			{
				Logger.println( "Failed to create Texture: " + texturePath, Logger.Verbosity.NORMAL ) ;
				return ;
			}

			try( final DesktopByteIn in = ( DesktopByteIn )file.getByteInStream() )
			{
				final InputStream stream = in.getInputStream() ;
				final BufferedImage image = ImageIO.read( stream ) ;

				// Generate our own mipmaps.
				int width = image.getWidth() ;
				int height = image.getHeight() ;

				final int levels = calculateMipMapLevels( width, height ) ;
				final BufferedImage[] images = new BufferedImage[levels] ;
				images[0] = image ;

				final AffineTransform at = new AffineTransform();
				at.scale( 0.5, 0.5 ) ;
				final AffineTransformOp operation = new AffineTransformOp( at, AffineTransformOp.TYPE_BICUBIC ) ;

				for( int i = 1; i < levels; ++i )
				{
					width /= ( width > 1 ) ? 2 : 1 ;
					height /= ( height > 1 ) ? 2 : 1 ;

					final BufferedImage source = images[i - 1] ;
					final BufferedImage destination = new BufferedImage( width, height, source.getType() ) ;
					images[i] = destination ;

					final Graphics2D g2 = ( Graphics2D )destination.getGraphics() ;
					g2.drawImage( source, operation, 0, 0 ) ;
					g2.dispose() ;
				}

				synchronized( toBind )
				{
					// We don't want to bind the BufferedImage now
					// as that will take control of the OpenGL context.
					toBind.add( new Tuple<String, BufferedImage[]>( texturePath, images ) ) ;
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace() ;
			}
		}
	}
}
