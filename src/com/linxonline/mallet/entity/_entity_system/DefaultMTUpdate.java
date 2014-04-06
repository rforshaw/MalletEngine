package com.linxonline.mallet.entity ;

import java.util.ArrayList ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.util.locks.* ;
import com.linxonline.mallet.system.GlobalConfig ;

public class DefaultMTUpdate extends DefaultSTUpdate
{
	private final MultiJLock multiLock = new MultiJLock() ;
	private EntityThread[] threads ;

	public DefaultMTUpdate()
	{
		init( 2 ) ;
	}

	public DefaultMTUpdate( final int _threads )
	{
		init( _threads ) ;
	}

	private void init( final int _threads )
	{
		threads = new EntityThread[_threads] ;
		for( int i = 0; i < threads.length; i++ )
		{
			threads[i] = new EntityThread( entities ) ;
			threads[i].start() ;
		}
	}

	@Override
	public void update( final float _dt )
	{
		multiLock.reset() ;
		int threadLength = threads.length ;
		final int entitiesLength = entities.size() ;
		if( threadLength > entitiesLength )
		{
			// You should never have more threads than entities,
			// Else don't have more threads than entities.
			threadLength = entitiesLength ;
		}

		int start = 0 ;
		int range = entitiesLength / threadLength ; 	// Split the entities between the threads.

		EntityThread thread = null ;
		for( int i = 0; i < threadLength; ++i )
		{
			thread = threads[i] ;
			thread.setRange( start, start + range ) ;
			start += range ;

			multiLock.interest() ;
			thread.setDeltaTime( _dt ) ;
			thread.unpause() ;			// Resume entity updating
		}

		multiLock.lock() ;		 		// Only continue once all EntityThreads have finished
	}

	private class EntityThread extends Thread
	{
		public boolean run = false ;
		private final ArrayList<Entity> entities ;
		private final LockInterface block ;

		private float deltaTime = 0.0f ;
		private int begin = 0 ;
		private int end = 0 ;

		public EntityThread( final ArrayList<Entity> _entities )
		{
			entities = _entities ;
			block = new JLock() ;
		}

		public void setDeltaTime( final float _dt )
		{
			deltaTime = _dt ;
		}

		/**
			Informs the thread which range of entities it will be processing
		**/
		public void setRange( final int _begin, final int _end )
		{
			begin = _begin ;
			end = _end ;
		}

		public void update( final float _dt )
		{
			Entity entity = null ;
			for( int i = begin; i < end; ++i )
			{
				entity = entities.get( i ) ;
				entity.update( _dt ) ;

				if( entity.destroy == true )
				{
					synchronized( cleanup )
					{
						cleanup.add( entity ) ;
					}
				}
			}
		}

		@Override
		public void run()
		{
			while( true )
			{
				update( deltaTime ) ;
				multiLock.unlock() ;  		// Inform main thread you've finished
				pause() ;					// Wait for main thread to call again
			}
		}

		public void unpause()
		{
			block.unlock() ;
		}

		public void pause()
		{
			block.lock() ;
		}
	}
}