package com.linxonline.mallet.renderer.GL ;

import java.awt.image.BufferedImage ;

import com.linxonline.mallet.resources.ImageInterface ;

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
}