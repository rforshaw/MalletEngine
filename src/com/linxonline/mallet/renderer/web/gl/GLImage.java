package com.linxonline.mallet.renderer.web.gl ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLTexture ;

import com.linxonline.mallet.renderer.texture.ImageInterface ;

public class GLImage implements ImageInterface
{
	public final WebGLTexture[] textureIDs = new WebGLTexture[1] ;			// Buffer ID for openGL
	private final int width ;				// Width of texture
	private final int height ;				// Height of texture

	public GLImage( final WebGLTexture _textureID, final int _width, final int _height )
	{
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
		final WebGLRenderingContext gl = GLRenderer.getContext() ;
		if( gl != null )
		{
			gl.deleteTexture( textureIDs[0] ) ;
		}
	}
}
