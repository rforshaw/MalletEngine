package com.linxonline.mallet.resources.model ;

import com.linxonline.mallet.resources.Resource ;

public final class Model extends Resource
{
	public final GeometryInterface geometry ;

	public Model( final GeometryInterface _geometry )
	{
		geometry = _geometry ;
	}

	public final <T> T getGeometry( Class<T> _type )
	{
		return _type.cast( geometry ) ;
	}

	public void destroy()
	{
		geometry.destroy() ;
	}
	
	@Override
	public String type()
	{
		return "MODEL" ;
	}
}