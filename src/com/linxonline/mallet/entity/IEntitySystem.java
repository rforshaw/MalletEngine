package com.linxonline.mallet.entity ;

import java.util.List ;

/**
	Allow the default Entity System to be completely 
	rewritten without much issue.
*/
public interface IEntitySystem
{
	public void addEntity( final Entity _entity ) ;					// Add Entity at the appropriate time
	public void removeEntity( final Entity _entity ) ;				// Remove Entity at the appropriate time

	public void update( final float _dt ) ;
	public void clear() ;

	public List<Entity> getEntities( final List<Entity> _entities ) ;
}
