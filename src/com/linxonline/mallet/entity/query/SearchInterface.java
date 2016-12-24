package com.linxonline.mallet.entity.query ;

import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.settings.* ;

/**
	If your not happy with how the QuerySystem works then 
	you can make your own from the ground up.

	Use this interface if you want to ensure the EntitySystem
	or anything else can make use of your new Search System.
**/
public interface SearchInterface
{
	public Entity queryForEntity( final String _queryName, final Settings _query ) ;
	public List<Entity> queryForEntities( final String _queryName, final Settings _query ) ;
	
	public void clear() ;
}
