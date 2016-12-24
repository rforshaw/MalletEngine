package com.linxonline.mallet.entity.system ;

import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.worker.* ;
import com.linxonline.mallet.system.GlobalConfig ;

public class DefaultMTUpdate extends DefaultSTUpdate
{
	private final WorkerGroup workers ;
	private final EntityWorker entityWorker = new EntityWorker() ;

	public DefaultMTUpdate()
	{
		this( 2 ) ;
	}

	public DefaultMTUpdate( final int _threads )
	{
		super() ;
		workers = new WorkerGroup( _threads ) ; 
	}

	@Override
	public void update( final float _dt )
	{
		entityWorker.setDeltaTime( _dt ) ;
		workers.exec( entityWorker ) ;				// This will block until all entities have been processed
	}

	private class EntityWorker implements Worker<Entity>
	{
		private float deltaTime = 0.0f ;

		public EntityWorker() {}

		public void setDeltaTime( final float _dt )
		{
			deltaTime = _dt ;
		}

		@Override
		public ExecType exec( final int _index, final Entity _entity )
		{
			_entity.update( deltaTime ) ;
			if( _entity.destroy == true )
			{
				synchronized( cleanup )
				{
					cleanup.add( _entity ) ;
				}
			}

			return ExecType.FINISH ;
		}

		public List<Entity> getDataSet()
		{
			return entities ;
		}
	}
}
