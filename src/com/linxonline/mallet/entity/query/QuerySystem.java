package com.linxonline.mallet.entity.query ;

import java.util.HashMap ;
import java.util.Collection ;
import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.Utility ;
import com.linxonline.mallet.util.settings.* ;

/**
	The Query System was designed to allow the developer to search the Entities of a GameState
	in a range of optimal ways depending on what the GameState required.

	It contains different ways in how a developer could search the Entities, what queries are 
	available is dependant on which ones the developer adds to the system.
**/
public class QuerySystem implements SearchInterface
{
	/**
		HashMap horribly slow to iterate over, possibly replace with a tree structure?
	**/
	protected final HashMap<String, QueryInterface> queryInterfaces = new HashMap<String, QueryInterface>() ;

	public void addQuery( final QueryInterface _interface )
	{
		queryInterfaces.put( _interface.getQueryName(), _interface ) ;
	}

	/**
		Return an Entity
		Based Specify a Query by _queryName
		And use _query to denote the specific criteria the Query will use to 
		search for the Entity
	**/
	public Entity queryForEntity( final String _queryName, final Settings _query )
	{
		if( queryInterfaces.containsKey( _queryName ) == true )
		{
			return queryInterfaces.get( _queryName ).queryForEntity( _query ) ;
		}

		return null ;
	}

	/**
		Return an Entities
		Based Specify a Query by _queryName
		And use _query to denote the specific criteria the Query will use to 
		search for the Entities
	**/
	public List<Entity> queryForEntities( final String _queryName, final Settings _query )
	{
		if( queryInterfaces.containsKey( _queryName ) == true )
		{
			return queryInterfaces.get( _queryName ).queryForEntities( _query ) ;
		}

		return Utility.<Entity>newArrayList() ;
	}

	public void addEntity( final Entity _entity )
	{
		final Collection<QueryInterface> values = queryInterfaces.values() ;
		for( final QueryInterface queryInterface : values )
		{
			queryInterface.addEntity( _entity ) ;
		}
	}

	public void removeEntity( final Entity _entity )
	{
		final Collection<QueryInterface> values = queryInterfaces.values() ;
		for( final QueryInterface queryInterface : values )
		{
			queryInterface.removeEntity( _entity ) ;
		}
	}

	public void clear()
	{
		final Collection<QueryInterface> interfaces = queryInterfaces.values() ;
		for( final QueryInterface inter : interfaces )
		{
			inter.clear() ;
		}
	}
}
