package com.linxonline.mallet.entity.query ;

import java.util.HashMap ;
import java.util.ArrayList ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.settings.* ;

public class HashMapQuery extends Query
{
	public final static String NAME = "NAME" ;

	private final HashMap<String, Entity> entities = new HashMap<String, Entity>() ;

	public HashMapQuery( final String _name )
	{
		super( _name ) ;
	}
	
	public Entity queryForEntity( final Settings _query )
	{
		final String name = _query.getString( "NAME", null ) ;
		if( name != null )
		{
			// If entities containsKey, name; return Entity else null
			return exists( name ) == true ? entities.get( name ) : null ;
		}

		return null ;
	}

	public ArrayList<Entity> queryForEntities( final Settings _query )
	{
		return new ArrayList<Entity>() ;
	}

	public void addEntity( final Entity _entity )
	{
		final String name = _entity.getName() ;
		if( exists( name ) == false )
		{
			entities.put( name, _entity ) ;
		}
	}

	public void removeEntity( final Entity _entity )
	{
		final String name = _entity.getName() ;
		if( exists( name ) == true )
		{
			entities.remove( name ) ;
		}
	}

	private boolean exists( final String _name )
	{
		return entities.containsKey( _name ) ;
	}

	public void clear()
	{
		entities.clear() ;
	}
}