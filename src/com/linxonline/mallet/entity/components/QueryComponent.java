package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.query.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.Utility ;
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
	public void passInitialEvents( final List<Event<?>> _events )
	{
		_events.add( new Event<QueryComponent>( "ADD_GAME_STATE_QUERY", this ) ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
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

	public List<Entity> queryForEntities( final String _queryName, final Settings _query )
	{
		if( searchInterface != null )
		{
			return searchInterface.queryForEntities( _queryName, _query ) ;
		}

		return Utility.<Entity>newArrayList() ;
	}

}
