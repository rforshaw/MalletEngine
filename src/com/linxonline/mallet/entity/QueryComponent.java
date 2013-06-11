package com.linxonline.mallet.entity ;

import java.util.ArrayList ;

import com.linxonline.mallet.entity.query.* ;
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