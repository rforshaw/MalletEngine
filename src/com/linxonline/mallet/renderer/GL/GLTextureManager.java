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
	public GLTextureManager() {}

	@Override
	protected Texture loadTexture( final String _file )
	{
		try
		{
			final byte[] image = GlobalFileSystem.getResourceRaw( _file ) ;
			final InputStream in = new ByteArrayInputStream( image ) ;

			return bind( ImageIO.read( in ) ) ;
		}
		catch( IOException _ex )
		{
			_ex.printStackTrace() ;
		}

		return null ;
	}

	/**
		Copies the BufferedImage into video memory.
		Ensure BufferedImage is in 4BYTE_ABGR or INT_ARGB.
		For best performance use 4BYTE_ABGR.
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
						 GL2.GL_RGBA, 
						 GL2.GL_UNSIGNED_BYTE, 
						 convertImageData( _image ) ) ;

		gl.glGenerateMipmap( GL2.GL_TEXTURE_2D ) ;

		GLRenderer.getCanvas().getContext().release() ;
		return new Texture( new GLImage( textureID, width, height ) ) ;
	}

	/**
		Massage the Data so OpenGL can read it.
		By default the ImageIO content is stored in a Byte stream,
		the Font Genorator images are stored in Int streams.
		For best performance ensure BufferedImage is Byte stream.
		Other stream types are not supported.
	*/
	private ByteBuffer convertImageData( final BufferedImage _image )
	{
		final DataBuffer buffer = _image.getRaster().getDataBuffer() ;
		final int type = buffer.getDataType() ;
		
		if( type == DataBuffer.TYPE_INT )
		{
			final int[] data = ( (  DataBufferInt )  buffer).getData() ;
			final ByteBuffer imageBuffer = ByteBuffer.allocateDirect( data.length * 4 ) ;
			imageBuffer.order( ByteOrder.nativeOrder() ) ;

			for( int i = 0; i < data.length; i++ )
			{
				imageBuffer.put( ( byte )( ( data[i] >> 16 ) & 0xFF ) ) ;	// alpha
				imageBuffer.put( ( byte )( ( data[i] >> 8  ) & 0xFF ) ) ;	// red
				imageBuffer.put( ( byte )( ( data[i]       ) & 0xFF ) ) ;	// green
				imageBuffer.put( ( byte )( ( data[i] >> 24 ) & 0xFF ) ) ;	// blue
			}

			imageBuffer.flip() ;
			return imageBuffer ;
		}
		else if( type == DataBuffer.TYPE_BYTE )
		{
			final byte[] data = ( (  DataBufferByte )  buffer).getData() ;
			byte alpha, red, green, blue ;
			for( int i = 0; i < data.length; i += 4 )
			{
				alpha = data[i + 3] ;
				red   = data[i + 2] ;
				green = data[i + 1] ;
				blue  = data[i] ;

				data[i]     = alpha ;
				data[i + 1] = red ;
				data[i + 2] = green ;
				data[i + 3] = blue ;
			}

			final ByteBuffer imageBuffer = ByteBuffer.wrap( data, 0, data.length ) ;
			return imageBuffer ;
		}

		System.out.println( "Failed to determine DataBuffer type." ) ;
		return null ;
	}
	
	private int glGenTextures( GL2 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenTextures( 1, id, 0 ) ;

		return id[0] ;
	}
}
