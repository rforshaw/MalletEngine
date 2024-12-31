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
	private final List<Tuple<String, BufferedImage[]>> toCreateTextures = MalletList.<Tuple<String, BufferedImage[]>>newList() ;
	private final List<Tuple<String, BufferedImage[][]>> toCreateTextureArrays = MalletList.<Tuple<String, BufferedImage[][]>>newList() ;
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

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
		return toCreateTextures.isEmpty() && toCreateTextureArrays.isEmpty() ;
	}

	@Override
	public GLImage get( final String _file )
	{
		synchronized( toCreateTextures )
		{
			// GLRenderer will continuously call get() until it 
			// receives a Texture, so we only need to bind 
			// textures that are waiting for the OpenGL context 
			// when the render requests it.
			while( toCreateTextures.isEmpty() == false && bindCount++ < TEXTURE_BIND_LIMIT  )
			{
				final Tuple<String, BufferedImage[]> tuple = toCreateTextures.remove( toCreateTextures.size() - 1 ) ;
				put( tuple.getLeft(), createGLImage( tuple.getRight() ) ) ;
			}
		}

		return super.get( _file ) ;
	}

	public GLImage getByTextureArray( final TextureArray _texture )
	{
		synchronized( toCreateTextureArrays )
		{
			// GLRenderer will continuously call get() until it 
			// receives a Texture, so we only need to bind 
			// textures that are waiting for the OpenGL context 
			// when the render requests it.
			while( toCreateTextureArrays.isEmpty() == false && bindCount++ < TEXTURE_BIND_LIMIT  )
			{
				final Tuple<String, BufferedImage[][]> tuple = toCreateTextureArrays.remove( toCreateTextureArrays.size() - 1 ) ;
				put( tuple.getLeft(), createGLImage( tuple.getRight() ) ) ;
			}
		}

		clean() ;

		final String id = _texture.getID() ;
		if( exists( id ) == true )
		{
			// May still return a null resource but
			// the key has been assigned.
			return resources.get( id ) ;
		}

		put( id, null ) ;
		Parallel.run( new TextureArrayRunner( _texture ) ) ;

		return null ;
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
	public Texture.Meta getMeta( final String _path )
	{
		return metaGenerator.getMeta( _path ) ;
	}

	public GLImage createGLImage( final BufferedImage[] _images )
	{
		return createGLImage( _images, InternalFormat.COMPRESSED ) ;
	}

	public GLImage createGLImage( final BufferedImage _image, final InternalFormat _format )
	{
		return createGLImage( new BufferedImage[] { _image }, _format ) ;
	}

	/**
		Upload the BufferedImages to the GPU.
		Return a GLImage that contains the id to access the image.
		The first image [0] is considered the base, images afterwards
		are the mipmaps of the base image.
	*/
	public GLImage createGLImage( final BufferedImage[] _images, final InternalFormat _format )
	{
		final int[] id = new int[1] ;

		MGL.glGenTextures( 1, id, 0 ) ;
		MGL.glBindTexture( MGL.GL_TEXTURE_2D, id[0] ) ;

		final BufferedImage base = _images[0] ;
		final int channels = base.getSampleModel().getNumBands() ;

		final int internalFormat = getGLInternalFormat( channels, _format ) ;
		final int imageFormat = getImageFormat( channels ) ; ;

		MGL.glPixelStorei( MGL.GL_UNPACK_ALIGNMENT, 1 ) ;

		long consumption = 0L ;

		for( int i = 0; i < _images.length; ++i )
		{
			final BufferedImage image = _images[i] ;
			if( image == null )
			{
				System.out.println( "Texture level " + i + " not available skipping." ) ;
				continue ;
			}

			final int width = image.getWidth() ;
			final int height = image.getHeight() ;
			consumption += width * height * ( channels * 8 ) ;

			MGL.glTexImage2D( MGL.GL_TEXTURE_2D, 
							i,
							internalFormat, 
							width, 
							height, 
							0, 
							imageFormat, 
							MGL.GL_UNSIGNED_BYTE, 
							ByteBuffer.wrap( convertToFormat( image, imageFormat ) ) ) ;
		}

		return GLImage.create( id, consumption ) ;
	}

	public GLImage createGLImage( final BufferedImage[][] _images )
	{
		return createGLImage( _images, InternalFormat.COMPRESSED ) ;
	}

	public GLImage createGLImage( final BufferedImage[][] _images, final InternalFormat _format )
	{
		final int[] id = new int[1] ;

		MGL.glGenTextures( 1, id, 0 ) ;
		MGL.glBindTexture( MGL.GL_TEXTURE_2D_ARRAY, id[0] ) ;

		MGL.glPixelStorei( MGL.GL_UNPACK_ALIGNMENT, 1 ) ;

		long consumption = 0L ;

		final int mipmapSize = _images.length ;
		for( int i = 0; i < mipmapSize; ++i )
		{
			final BufferedImage[] images = _images[i] ;
			if( images == null )
			{
				System.out.println( "Images " + i + " not available skipping." ) ;
				continue ;
			}

			final BufferedImage base = images[0] ;
			final int channels = base.getSampleModel().getNumBands() ;

			final int internalFormat = getGLInternalFormat( channels, _format ) ;
			final int imageFormat = getImageFormat( channels ) ; ;
			
			final int width = base.getWidth() ;
			final int height = base.getHeight() ;

			final int size = ( width * height * ( channels * 8 ) ) * images.length ;

			final ByteBuffer buffer = ByteBuffer.allocateDirect( size ) ;
			for( final BufferedImage layer : images )
			{
				buffer.put( convertToFormat( layer, internalFormat ) ) ;
			}
			buffer.position( 0 ) ;

			consumption += size ;

			MGL.glTexImage3D( MGL.GL_TEXTURE_2D_ARRAY, 
							i,
							internalFormat, 
							width, 
							height,
							images.length,
							0, 
							imageFormat, 
							MGL.GL_UNSIGNED_BYTE, 
							buffer ) ;
		}

		return GLImage.create( id, consumption ) ;
	}

	/**
		Return the image format we want our texture to
		be once it's uploaded to the GPU.
		The important aspect is to identify whether we
		want to compress the texture or not.
	*/
	private static int getGLInternalFormat( final int _channels, final InternalFormat _format )
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
		We need to identify the pixel format of the BufferedImage.
		We'll return a format supported by OpenGL.
		By default BufferedImage uses the ABGR, BGR, structure, so
		if our graphics card can support that format we'll return
		them, and avoid having to convert our image to an RGBA structure.
	*/
	private static int getImageFormat( final int _channels )
	{
		if( MGL.isExtensionAvailable( "GL_EXT_abgr" ) == true )
		{
			switch( _channels )
			{
				case 4  : return MGL.GL_ABGR_EXT ;
				default :
				case 3  : return MGL.GL_BGR ;
				case 1  : return MGL.GL_RED ;
			}
		}

		switch( _channels )
		{
			case 4  : return MGL.GL_RGBA ;
			default :
			case 3  : return MGL.GL_RGB ;
			case 1  : return MGL.GL_RED ;
		}
	}

	private static byte[] convertToFormat( final BufferedImage _image, final int _format )
	{
		final DataBuffer buffer = _image.getRaster().getDataBuffer() ;
		final int type = buffer.getDataType() ;

		switch( type )
		{
			default                   :
			{
				System.out.println( "Failed to determine DataBuffer type, only support byte type." ) ;
				return null ;
			}
			case DataBuffer.TYPE_BYTE :
			{
				final byte[] data = ( (  DataBufferByte )  buffer).getData() ;
				switch( _format )
				{
					default          : return data ;
					case MGL.GL_RGBA : return convertABGRtoRGBA( data ) ;
				}
			}
		}
	}
	
	private static byte[] convertABGRtoRGBA( final byte[] _data )
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
	protected static final class MetaGenerator
	{
		private final Map<String, Texture.Meta> imageMetas = MalletMap.<String, Texture.Meta>newMap() ;

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
		public Texture.Meta getMeta( final String _path )
		{
			synchronized( imageMetas )
			{
				final Texture.Meta meta = imageMetas.get( _path ) ;
				if( meta != null)
				{
					return meta ;
				}

				final FileStream file = GlobalFileSystem.getFile( _path ) ;
				if( file.exists() == false )
				{
					Logger.println( "No Texture found to create Meta: " + _path, Logger.Verbosity.NORMAL ) ;
					return new Texture.Meta( _path, 0, 0 ) ;
				}

				return addMeta( _path, createMeta( _path, file ) ) ; 
			}
		}

		private Texture.Meta addMeta( final String _path, final Texture.Meta _meta )
		{
			if( _meta != null )
			{
				imageMetas.put( _path, _meta ) ;
				return _meta ;
			}

			Logger.println( "Failed to create Texture Meta: " + _path, Logger.Verbosity.NORMAL ) ;
			return new Texture.Meta( _path, 0, 0 ) ;
		}

		private Texture.Meta createMeta( final String _path, final FileStream _file )
		{
			Texture.Meta meta = null ; 
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

		private static Texture.Meta createMeta( final String _path, final InputStream _stream )
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
						// Add additional Meta information to Texture as 
						// and when it becomes needed. It shouldn't hold too much (RGB, RGBA, Mono, endinese, 32, 24-bit, etc)
						// data as a game-developer shouldn't need detailed information.
						return new Texture.Meta( _path, reader.getWidth( 0 ), reader.getHeight( 0 ) ) ;
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

	private static int calculateMipMapLevels( int _width, int _height )
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

	private static BufferedImage[] generateMipMaps( final InputStream _stream ) throws IOException
	{
		final BufferedImage image = ImageIO.read( _stream ) ;

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

		return images ;
	}

	private final class TextureRunner implements Parallel.IRun
	{
		private final String path ;

		public TextureRunner( final String _file )
		{
			path = _file ;
		}

		@Override
		public void run()
		{
			final FileStream file = GlobalFileSystem.getFile( path ) ;
			if( file.exists() == false )
			{
				Logger.println( "Failed to create Texture: " + path, Logger.Verbosity.NORMAL ) ;
				return ;
			}

			try( final DesktopByteIn in = ( DesktopByteIn )file.getByteInStream() )
			{
				final InputStream stream = in.getInputStream() ;
				final BufferedImage[] images = generateMipMaps( stream ) ;

				synchronized( toCreateTextures )
				{
					// We don't want to bind the BufferedImage now
					// as that will take control of the OpenGL context.
					toCreateTextures.add( new Tuple<String, BufferedImage[]>( path, images ) ) ;
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace() ;
			}
		}
	}
	
	private final class TextureArrayRunner implements Parallel.IRun
	{
		private final TextureArray texture ;

		public TextureArrayRunner( final TextureArray _texture )
		{
			texture = _texture ;
		}

		@Override
		public void run()
		{
			final int size = texture.getDepth() ;
			final int levels = calculateMipMapLevels( texture.getWidth(), texture.getHeight() ) ;

			final BufferedImage[][] buffers = new BufferedImage[levels][] ;
			for( int i = 0; i < levels; ++i )
			{
				buffers[i] = new BufferedImage[size] ;
			}

			for( int i = 0; i < size; ++i )
			{
				final Texture.Meta meta = texture.getMeta( i ) ;
				final String path = meta.getPath() ;

				final FileStream file = GlobalFileSystem.getFile( path ) ;
				if( file.exists() == false )
				{
					Logger.println( "Failed to create Texture: " + path, Logger.Verbosity.NORMAL ) ;
					return ;
				}

				try( final DesktopByteIn in = ( DesktopByteIn )file.getByteInStream() )
				{
					final InputStream stream = in.getInputStream() ;
					final BufferedImage[] images = generateMipMaps( stream ) ;

					for( int j = 0; j < levels; ++j )
					{
						buffers[j][i] = images[j] ;
					}
				}
				catch( Exception ex )
				{
					ex.printStackTrace() ;
				}
			}

			synchronized( toCreateTextureArrays )
			{
				// We don't want to bind the BufferedImage now
				// as that will take control of the OpenGL context.
				toCreateTextureArrays.add( new Tuple<String, BufferedImage[][]>( texture.getID(), buffers ) ) ;
			}
		}
	}
}
