package com.linxonline.mallet.entity.system ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.entity.* ;

public class DefaultSTUpdate implements EntityUpdateInterface
{
	protected final List<Entity> entities = MalletList.<Entity>newList() ;		// Entities that are active
	protected final List<Entity> cleanup = MalletList.<Entity>newList() ;		// Entities that need to be removed

	public DefaultSTUpdate() {}
	
	public void addEntity( final Entity _entity )
	{
		entities.add( _entity ) ;
	}

	public void removeEntity( final Entity _entity )
	{
		entities.remove( _entity ) ;
	}

	public void update( final float _dt )
	{
		Entity entity = null ;
		final int entitySize = entities.size() ;
		for( int i = 0; i < entitySize; ++i )
		{
			entity = entities.get( i ) ;
			entity.update( _dt ) ;

			if( entity.isDead() == true )
			{
				cleanup.add( entity ) ;
			}
		}
	}

	public void clear()
	{
		entities.clear() ;
		cleanup.clear() ;
	}

	public List<Entity> getEntities()
	{
		return entities ;
	}

	/**
		Used by entity-system to determine what entities need to be un-hooked.
		Should be replaced with callback functionality.
	**/
	public boolean getCleanup( final List<Entity> _entities )
	{
		if( cleanup.size() > 0 )
		{
			_entities.addAll( cleanup ) ;
			cleanup.clear() ;
			return true ;
		}

		return false ;
	}
}
