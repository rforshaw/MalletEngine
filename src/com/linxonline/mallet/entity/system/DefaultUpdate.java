package com.linxonline.mallet.entity.system ;

import java.util.ArrayList ;
import com.linxonline.mallet.entity.* ;

public class DefaultUpdate implements EntityUpdateInterface
{
	protected final ArrayList<Entity> entities = new ArrayList<Entity>() ;
	protected final ArrayList<Entity> cleanup = new ArrayList<Entity>() ;

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
			
			if( entity.destroy == true )
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

	public ArrayList<Entity> getEntities()
	{
		return entities ;
	}

	public ArrayList<Entity> getCleanup()
	{
		if( cleanup.size() > 0 )
		{
			final ArrayList<Entity> clean = new ArrayList<Entity>( cleanup ) ;
			cleanup.clear() ;
			return clean ;
		}

		return cleanup ;
	}
}