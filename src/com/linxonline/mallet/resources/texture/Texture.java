package com.linxonline.mallet.resources.texture ;

import com.linxonline.mallet.resources.Resource ;

public final class Texture extends Resource
{
	private static final String type = "TEXTURE" ;
	public int width = 0 ;
	public int height = 0 ;
	public ImageInterface image = null ;

	public Texture( ImageInterface _image )
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
	
	public final <T> T getImage( Class<T> _type )
	{
		return _type.cast( image ) ;
	}
	
	@Override
	public String type()
	{
		return type ;
	}
}
