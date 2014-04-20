package com.linxonline.mallet.resources.texture ;

import com.linxonline.mallet.resources.Resource ;

public final class Texture<T extends ImageInterface> extends Resource
{
	public int width = 0 ;
	public int height = 0 ;
	public T image = null ;

	public Texture( T _image )
	{
		if( _image != null )
		{
			image = _image ;
			width = image.getWidth() ;
			height = image.getHeight() ;
		}
	}

	public final int getWidth()
	{
		return width ;
	}

	public final int getHeight()
	{
		return height ;
	}
	
	public final T getImage()
	{
		return image ;
	}
	
	public final void destroy()
	{
		image.destroy() ;
	}
	
	@Override
	public String type()
	{
		return "TEXTURE" ;
	}
}
