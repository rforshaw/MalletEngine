package com.linxonline.mallet.entity.system ;

import java.util.List ;
import com.linxonline.mallet.entity.query.* ;
import com.linxonline.mallet.entity.* ;

/**
	Allow the default Entity System to be completely 
	rewritten without much issue.
*/
public interface EntitySystemInterface
{
	public void addEntity( final Entity _entity ) ;					// Add Entity at the appropriate time
	public void removeEntity( final Entity _entity ) ;				// Remove Entity at the appropriate time
	public void addEntityNow( final Entity _entity ) ;				// Add Entity now

	public void addQuery( final QueryInterface _interface ) ;

	public void update( final float _dt ) ;
	public void clear() ;

	public SearchInterface getSearch() ;
	public List<Entity> getEntities() ;
	public Entity getEntityByName( final String _name ) ;
}
