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

	public Texture bind( BufferedImage _image )
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

		final ByteBuffer buffer = convertImageData( _image ) ;

		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT ) ;
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT ) ;
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR ) ;
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR ) ;

		gl.glTexImage2D( GL2.GL_TEXTURE_2D, 
						 0, 
						 GL2.GL_RGBA, 
						 _image.getWidth(), 
						 _image.getHeight(), 
						 0, 
						 GL2.GL_RGBA, 
						 GL2.GL_UNSIGNED_BYTE, 
						 buffer ) ;

		gl.glGenerateMipmap( GL2.GL_TEXTURE_2D ) ;

		GLRenderer.getCanvas().getContext().release() ;
		return new Texture( new GLImage( textureID, _image ) ) ;
	}

	// Massage the Data so OpenGL can read it.
	// More Efficient way?
	private ByteBuffer convertImageData( BufferedImage _image )
	{
		final ColorModel glAlphaColorModel = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), 
																	  new int[] { 8, 8, 8, 8 },
																	  true, 
																	  false, 
																	  Transparency.TRANSLUCENT, 
																	  DataBuffer.TYPE_BYTE ) ;

		final WritableRaster raster = Raster.createInterleavedRaster( DataBuffer.TYPE_BYTE,
																	  _image.getWidth(),
																	  _image.getHeight(), 
																	  4, 
																	  null ) ;

		final BufferedImage texImage = new BufferedImage( glAlphaColorModel, 
														  raster, 
														  true, 
														  null ) ;

		// copy the source image into the produced image
		final Graphics g = texImage.getGraphics() ;
		g.drawImage( _image, 0, 0, null ) ;

		// build a byte buffer from the temporary image
		// that is used by OpenGL to produce a texture.
		final byte[] data = ( ( DataBufferByte ) texImage.getRaster().getDataBuffer() ).getData() ;

		final ByteBuffer imageBuffer = ByteBuffer.allocateDirect( data.length ) ;
		imageBuffer.order( ByteOrder.nativeOrder() ) ;
		imageBuffer.put( data ) ;
		imageBuffer.flip() ;

		return imageBuffer;
}
	
	private int glGenTextures( GL2 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenTextures( 1, id, 0 ) ;

		return id[0] ;
	}
}
