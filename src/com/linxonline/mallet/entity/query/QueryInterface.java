package com.linxonline.mallet.entity.query ;

import java.util.ArrayList ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.settings.* ;

public interface QueryInterface
{
	public Entity queryForEntity( final Settings _query ) ;
	public ArrayList<Entity> queryForEntities( final Settings _query ) ;

	public void addEntity( final Entity _entity ) ;
	public void removeEntity( final Entity _entity ) ;

	public String getQueryName() ;

	public void clear() ;
}