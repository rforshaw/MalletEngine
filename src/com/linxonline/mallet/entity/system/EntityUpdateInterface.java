package com.linxonline.mallet.entity.system ;

import java.util.ArrayList ;
import com.linxonline.mallet.entity.* ;

public interface EntityUpdateInterface
{
	public void addEntity( final Entity _entity ) ;
	public void removeEntity( final Entity _entity ) ;

	public void update( final float _dt ) ;
	public void clear() ;

	public ArrayList<Entity> getEntities() ;
	public boolean getCleanup( final ArrayList<Entity> _entities ) ;
}