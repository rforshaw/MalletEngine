package com.linxonline.mallet.renderer.G2D ;

import java.awt.image.BufferedImage ;
import com.linxonline.mallet.resources.ImageInterface ;

public class G2DImage implements ImageInterface
{
	public BufferedImage bufferedImage = null ;
	public int width = 0 ;
	public int height = 0 ;

	public G2DImage( BufferedImage _bufferedImage )
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

	public final void destroy() {}
}
