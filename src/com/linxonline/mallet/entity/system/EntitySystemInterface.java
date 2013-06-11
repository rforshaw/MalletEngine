package com.linxonline.mallet.entity.system ;

import java.util.ArrayList ;
import com.linxonline.mallet.entity.query.* ;
import com.linxonline.mallet.entity.* ;

public interface EntitySystemInterface
{
	public void addEntity( final Entity _entity ) ;					// Add Entity at the appropriate time
	public void removeEntity( final Entity _entity ) ;				// Remove Entity at the appropriate time
	public void addEntityNow( final Entity _entity ) ;				// Add Entity now

	public void addQuery( final QueryInterface _interface ) ;

	public void update( final float _dt ) ;
	public void clear() ;

	public SearchInterface getSearch() ;
	public ArrayList<Entity> getEntities() ;
	public Entity getEntityByName( final String _name ) ;
}