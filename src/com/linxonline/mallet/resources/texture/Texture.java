package com.linxonline.mallet.resources.texture ;

import com.linxonline.mallet.resources.Resource ;

public final class Texture<T extends ImageInterface> extends Resource
{
	public final int width ;
	public final int height ;
	public final T image ;

	public Texture( T _image )
	{
		image = _image ;
		width = ( image != null ) ? image.getWidth() : 0 ;
		height = ( image != null ) ? image.getHeight() : 0 ;
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
