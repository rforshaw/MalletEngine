package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.color.ColorSpace ;
import javax.media.opengl.* ;
import javax.imageio.* ;
import java.io.* ;

import java.nio.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.desktop.* ;
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

	/**
		Currently two OpenGL image formats are supported: GL_RGBA and GL_ABGR_EXT.
		It's set to GL_RGBA by default due to the extension potentially not 
		being available, though unlikely. BufferedImage by default orders the channels ABGR.
	*/
	protected int imageFormat = GL2.GL_RGBA ;

	public GLTextureManager() {}

	@Override
	public Texture get( final String _file )
	{
		synchronized( toBind )
		{
			for( final Tuple<String, BufferedImage> tuple : toBind )
			{
				add( tuple.getLeft(), bind( tuple.getRight() ) ) ;
			}
			toBind.clear() ;
		}

		return super.get( _file ) ;
	}

	/**
		Change the image format used to bind textures.
		GL_RGBA and GL_ABGR_EXT are supported.
	*/
	public void setImageFormat( final int _format )
	{
		imageFormat = _format ;
	}

	@Override
	protected Texture createResource( final String _file )
	{
		return loadTextureASync( _file ) ;
	}

	protected Texture loadTextureASync( final String _file )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		if( file.exists() == false )
		{
			Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final Thread load = new Thread( "LOAD_TEXTURE" )
		{
			public void run()
			{
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
						toBind.add( new Tuple<String, BufferedImage>( _file, image ) ) ;
					}
				}
				catch( IOException ex )
				{
					ex.printStackTrace() ;
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

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public Texture bind( final BufferedImage _image )
	{
		//GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL2 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL2() ;
		if( gl == null )
		{
			System.out.println( "GL context doesn't exist" ) ;
			return null ;
		}

		gl.glEnable( GL.GL_TEXTURE_2D ) ;

		final int textureID = glGenTextures( gl ) ;
		gl.glBindTexture( GL2.GL_TEXTURE_2D, textureID ) ;

		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT ) ;
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT ) ;
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR ) ;
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR ) ;

		final int width = _image.getWidth() ;
		final int height = _image.getHeight() ;
		final int channels = _image.getSampleModel().getNumBands() ;

		if( gl.isExtensionAvailable( "GL_EXT_abgr" ) == true )
		{
			switch( channels )
			{
				case 4 : imageFormat = GL2.GL_ABGR_EXT ; break ;
				case 3 : imageFormat = GL2.GL_BGR ;      break ;
			}
		}
		else
		{
			switch( channels )
			{
				case 4 : imageFormat = GL2.GL_RGBA ; break ;
				case 3 : imageFormat = GL2.GL_RGB ;  break ;
			}
		}

		gl.glTexImage2D( GL2.GL_TEXTURE_2D, 
						 0, 
						 channels, 
						 width, 
						 height, 
						 0, 
						 imageFormat, 
						 GL2.GL_UNSIGNED_BYTE, 
						 getByteBuffer( _image ) ) ;

		gl.glGenerateMipmap( GL2.GL_TEXTURE_2D ) ;

		//GLRenderer.getCanvas().getContext().release() ;
		return new Texture( new GLImage( textureID, width, height ) ) ;
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
			if( imageFormat == GL2.GL_RGBA )
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

	private int glGenTextures( GL2 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenTextures( 1, id, 0 ) ;

		return id[0] ;
	}
}
