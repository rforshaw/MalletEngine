package com.linxonline.mallet.entity.query ;

import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.settings.* ;

public interface QueryInterface
{
	public Entity queryForEntity( final Settings _query ) ;
	public List<Entity> queryForEntities( final Settings _query ) ;

	public void addEntity( final Entity _entity ) ;
	public void removeEntity( final Entity _entity ) ;

	public String getQueryName() ;

	public void clear() ;
}
