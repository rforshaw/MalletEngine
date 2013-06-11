package com.linxonline.mallet.resources.gl ;

import java.awt.image.BufferedImage ;

import com.linxonline.mallet.resources.texture.* ;

public class GLImage extends JavaImage
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