package com.linxonline.mallet.renderer.texture ;

import com.linxonline.mallet.resources.Resource ;

public final class Texture<T extends ImageInterface> extends Resource
{
	public final T image ;

	public Texture( final T _image )
	{
		super() ;
		image = _image ;
	}

	public T getImage()
	{
		return image ;
	}

	@Override
	public long getMemoryConsumption()
	{
		return image.getMemoryConsumption() ;
	}
	
	@Override
	public void destroy()
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
