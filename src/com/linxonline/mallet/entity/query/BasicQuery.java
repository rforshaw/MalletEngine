package com.linxonline.mallet.entity.query ;

import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.Utility ;
import com.linxonline.mallet.util.settings.* ;

/**
	This class is horribly slow, and is really 
	just an example of how to extend the Query class.
	Shouldn't really be used for anything, expect 
	the most ineffecient search algorithm 
**/
public class BasicQuery extends Query
{
	private List<Entity> entities = Utility.<Entity>newArrayList() ;

	public BasicQuery( final String _name )
	{
		super( _name ) ;
	}
	
	public Entity queryForEntity( final Settings _query )
	{
		final String name = _query.getString( "NAME", null ) ;
		if( name != null )
		{
			final int size = entities.size() ;
			Entity entity = null ;
			for( int i = 0; i < size; ++i )
			{
				entity = entities.get( i ) ;
				if( entity.id.isName( name ) == true )
				{
					return entity ;
				}
			}
		}

		return null ;
	}

	public List<Entity> queryForEntities( final Settings _query )
	{
		return Utility.<Entity>newArrayList() ;
	}

	public void addEntity( final Entity _entity )
	{
		if( exists( _entity ) == false )
		{
			entities.add( _entity ) ;
		}
	}

	public void removeEntity( final Entity _entity )
	{
		if( exists( _entity ) == true )
		{
			entities.remove( _entity ) ;
		}
	}

	private boolean exists( final Entity _entity )
	{
		return entities.contains( _entity ) ;
	}
	
	public void clear()
	{
		entities.clear() ;
	}
}
