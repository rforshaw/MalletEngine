package com.linxonline.mallet.renderer.GL ;

import java.awt.image.BufferedImage ;

import com.linxonline.mallet.renderer.G2D.G2DImage ;

public class GLImage extends G2DImage
{
	public int textureID = 0 ;

	public GLImage( int _textureID, BufferedImage _bufferedImage )
	{
		super( _bufferedImage ) ;
		textureID = _textureID ;
	}
	
	public void clearSourceImage()
	{
		bufferedImage = null ;
	}
}