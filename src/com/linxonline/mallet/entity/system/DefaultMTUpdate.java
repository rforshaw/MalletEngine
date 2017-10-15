package com.linxonline.mallet.entity.system ;

import java.util.List ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.worker.* ;
import com.linxonline.mallet.core.GlobalConfig ;

public class DefaultMTUpdate extends DefaultSTUpdate
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
		workers = new WorkerGroup( _threads ) ; 
	}

	@Override
	public void update( final float _dt )
	{
		entityWorker.setDeltaTime( _dt ) ;
		workers.exec( entityWorker ) ;				// This will block until all entities have been processed
	}

	private class EntityWorker extends Worker<Entity>
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

		@Override
		public List<Entity> getDataSet()
		{
			return entities ;
		}
	}
}
