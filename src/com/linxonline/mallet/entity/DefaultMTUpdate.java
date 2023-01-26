package com.linxonline.mallet.entity ;

import java.util.List ;

import com.linxonline.mallet.util.Parallel ;
import com.linxonline.mallet.core.GlobalConfig ;

public class DefaultMTUpdate implements IEntityUpdate
{
	private final EntityWorker entityWorker = new EntityWorker() ;

	public DefaultMTUpdate()
	{
		super() ;
	}

	@Override
	public void update( final float _dt, final List<Entity> _update, final List<Entity> _cleanup )
	{
		entityWorker.setDeltaTime( _dt ) ;
		entityWorker.setCleanup( _cleanup ) ;
		Parallel.forEach( _update, entityWorker ) ;
	}

	private static class EntityWorker implements Parallel.IRangeRun<Entity>
	{
		private float deltaTime = 0.0f ;
		private List<Entity> cleanup ;

		public EntityWorker() {}

		public void setDeltaTime( final float _dt )
		{
			deltaTime = _dt ;
		}

		public void setCleanup( final List<Entity> _cleanup )
		{
			cleanup = _cleanup ;
		}

		@Override
		public void run( final int _index, final Entity _entity )
		{
			_entity.update( deltaTime ) ;
			if( _entity.isDead() == true )
			{
				synchronized( cleanup )
				{
					cleanup.add( _entity ) ;
				}
			}
		}
	}
}
