package com.linxonline.mallet.entity.query ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.settings.* ;

public class HashMapQuery extends Query
{
	public final static String NAME = "NAME" ;

	private final Map<String, Entity> entities = MalletMap.<String, Entity>newMap() ;

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

	public List<Entity> queryForEntities( final Settings _query )
	{
		return MalletList.<Entity>newList() ;
	}

	public void addEntity( final Entity _entity )
	{
		final String name = _entity.id.name ;
		if( exists( name ) == false )
		{
			entities.put( name, _entity ) ;
		}
	}

	public void removeEntity( final Entity _entity )
	{
		final String name = _entity.id.name ;
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
