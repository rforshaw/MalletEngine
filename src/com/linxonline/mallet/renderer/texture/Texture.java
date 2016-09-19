package com.linxonline.mallet.renderer.texture ;

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
	public boolean equals( final Object _obj )
	{
		if( this == _obj )
		{
			return true ;
		}
		
		if( _obj == null )
		{
			return false ;
		}

		return image.equals( _obj ) ;
	}

	@Override
	public int hashCode()
	{
		return image.hashCode() ;
	}

	@Override
	public String type()
	{
		return "TEXTURE" ;
	}
}
