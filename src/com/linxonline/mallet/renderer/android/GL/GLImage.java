package com.linxonline.mallet.renderer.android.GL ;

import android.opengl.GLES11 ;

import com.linxonline.mallet.resources.texture.ImageInterface ;

public class GLImage implements ImageInterface
{
	public int textureID = 0 ;				// Buffer ID for openGL
	private final int width ;				// Width of texture
	private final int height ;				// Height of texture

	public GLImage( int _textureID, final int _width, final int _height )
	{
		textureID = _textureID ;
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

	@Override
	public final void destroy()
	{
		final int[] textures = new int[1] ;
		textures[0] = textureID ;
		GLES11.glDeleteTextures( 1, textures, 0 ) ;
	}
}