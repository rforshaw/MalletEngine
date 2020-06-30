package com.linxonline.mallet.entity ;

import java.util.List ;

import com.linxonline.mallet.util.worker.* ;
import com.linxonline.mallet.core.GlobalConfig ;

public class DefaultMTUpdate implements IEntityUpdate
{
	private final WorkerGroup workers ;
	private final EntityWorker entityWorker = new EntityWorker() ;

	public DefaultMTUpdate()
	{
		this( 4 ) ;
	}

	public DefaultMTUpdate( final int _threads )
	{
		super() ;
		workers = new WorkerGroup( "ENTITY UPDATE", _threads ) ; 
	}

	@Override
	public void update( final float _dt, final List<Entity> _update, final List<Entity> _cleanup )
	{
		entityWorker.setDeltaTime( _dt ) ;
		entityWorker.setCleanup( _cleanup ) ;
		workers.exec( _update, entityWorker ) ;				// This will block until all entities have been processed
	}

	private static class EntityWorker extends Worker<Entity>
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
		public ExecType exec( final int _index, final Entity _entity )
		{
			//System.out.println( "Updating: " + _entity.id ) ;
			_entity.update( deltaTime ) ;
			if( _entity.isDead() == true )
			{
				synchronized( cleanup )
				{
					cleanup.add( _entity ) ;
				}
			}

			return ExecType.CONTINUE ;
		}
	}
}
