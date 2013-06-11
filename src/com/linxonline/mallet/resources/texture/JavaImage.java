package com.linxonline.mallet.resources.texture ;

import java.awt.image.BufferedImage ;

public class JavaImage implements ImageInterface
{
	public BufferedImage bufferedImage = null ;
	public int width = 0 ;
	public int height = 0 ;

	public JavaImage( BufferedImage _bufferedImage )
	{
		bufferedImage = _bufferedImage ;
		width = bufferedImage.getWidth() ;
		height = bufferedImage.getHeight() ;
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
