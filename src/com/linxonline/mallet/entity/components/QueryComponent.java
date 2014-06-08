package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.query.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.settings.* ;

public class QueryComponent extends Component
{
	private SearchInterface searchInterface = null ;

	public QueryComponent()
	{
		super( "ENTITY_QUERY", "QUERYCOMPONENT" ) ;
	}

	public void setSearch( final SearchInterface _interface )
	{
		searchInterface = _interface ;
	}

	@Override
	public void passInitialEvents( final ArrayList<Event> _events )
	{
		final Event<QueryComponent> event = new Event<QueryComponent>( "ADD_GAME_STATE_QUERY", this ) ;
		_events.add( event ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event> _events )
	{
		searchInterface = null ;
	}

	public Entity queryForEntity( final String _queryName, final Settings _query )
	{
		if( searchInterface != null )
		{
			return searchInterface.queryForEntity( _queryName, _query ) ;
		}

		return null;
	}

	public ArrayList<Entity> queryForEntities( final String _queryName, final Settings _query )
	{
		if( searchInterface != null )
		{
			return searchInterface.queryForEntities( _queryName, _query ) ;
		}

		return new ArrayList<Entity>() ;
	}

}