package com.linxonline.mallet.renderer.desktop.GL ;

import java.awt.image.BufferedImage ;
import javax.media.opengl.* ;

import com.linxonline.mallet.resources.texture.ImageInterface ;

public class GLImage implements ImageInterface
{
	public final int[] textureIDs ;			// Buffer ID for openGL
	private final int width ;				// Width of texture
	private final int height ;				// Height of texture

	public GLImage( int _textureID, final int _width, final int _height )
	{
		textureIDs = new int[1] ;
		textureIDs[0] = _textureID ;

		width = _width ;
		height = _height ;
	}

	public final int getWidth()
	{
		return width ;
	}

	public final int getHeight()
	{
		return height ;
	}
	
	public final void destroy()
	{
		//System.out.println( "Removing texture.." ) ;
		GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL2 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL2() ;
		if( gl != null )
		{
			gl.glDeleteTextures( textureIDs.length, textureIDs, 0 ) ;
		}
		GLRenderer.getCanvas().getContext().release() ;
	}
}