package com.linxonline.mallet.entity.query ;

import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.settings.* ;

/**
	Even though an Interface is available to make use of the QuerySystem, it is 
	expected that most developers will use this abstract class.
	
	This Query class deals with the boiler plate code required.
	Such as the Query name.
**/
public abstract class Query implements QueryInterface
{
	protected String name = "UNKNOWN" ;

	public Query( final String _name )
	{
		name = _name ;
	}
	
	public abstract Entity queryForEntity( final Settings _query ) ;
	public abstract List<Entity> queryForEntities( final Settings _query ) ;

	public abstract void addEntity( final Entity _entity ) ;
	public abstract void removeEntity( final Entity _entity ) ;

	public String getQueryName()
	{
		return name ;
	}

	public abstract void clear() ;
}
