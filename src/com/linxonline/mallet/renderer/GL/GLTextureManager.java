package com.linxonline.mallet.renderer.GL ;

import java.awt.* ;
import java.awt.image.* ;
import java.awt.color.ColorSpace ;
import javax.media.opengl.* ;
import javax.imageio.* ;
import java.io.* ;

import java.nio.* ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.texture.Texture ;

public class GLTextureManager extends TextureManager
{
	/**
		Currently two OpenGL image formats are supported: GL_RGBA and GL_ABGR_EXT.
		It's set to GL_RGBA by default due to the extension potentially not 
		being available, though unlikely.
	*/
	protected int imageFormat = GL2.GL_RGBA ;

	public GLTextureManager() {}

	/**
		Change the image format used to bind textures.
		GL_RGBA and GL_ABGR_EXT are supported.
	*/
	public void setImageFormat( final int _format )
	{
		imageFormat = _format ;
	}
	
	@Override
	protected Texture loadTexture( final String _file )
	{
		try
		{
			final byte[] image = GlobalFileSystem.getResourceRaw( _file ) ;
			if( image != null )
			{
				final InputStream in = new ByteArrayInputStream( image ) ;
				return bind( ImageIO.read( in ) ) ;
			}
		}
		catch( IOException _ex )
		{
			_ex.printStackTrace() ;
		}

		return null ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public Texture bind( final BufferedImage _image )
	{
		GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
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

		gl.glTexImage2D( GL2.GL_TEXTURE_2D, 
						 0, 
						 GL2.GL_RGBA, 
						 width, 
						 height, 
						 0, 
						 imageFormat, 
						 GL2.GL_UNSIGNED_BYTE, 
						 getByteBuffer( _image ) ) ;

		gl.glGenerateMipmap( GL2.GL_TEXTURE_2D ) ;

		GLRenderer.getCanvas().getContext().release() ;
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
