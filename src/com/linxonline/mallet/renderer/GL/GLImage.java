package com.linxonline.mallet.renderer.GL ;

import java.awt.image.BufferedImage ;

import com.linxonline.mallet.resources.texture.ImageInterface ;

public class GLImage implements ImageInterface
{
	public int textureID = 0 ;
	private final int width ;
	private final int height ;

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