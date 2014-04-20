package com.linxonline.mallet.resources.model ;

import com.linxonline.mallet.resources.Resource ;

public final class Model extends Resource
{
	private static final String type = "MODEL" ;
	public GeometryInterface geometry = null ;

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
		return type ;
	}
}