package com.linxonline.mallet.entity.system ;

import java.util.List ;
import com.linxonline.mallet.entity.* ;

public interface EntityUpdateInterface
{
	public void addEntity( final Entity _entity ) ;
	public void removeEntity( final Entity _entity ) ;

	public void update( final float _dt ) ;
	public void clear() ;

	public List<Entity> getEntities() ;
	public boolean getCleanup( final List<Entity> _entities ) ;
}
