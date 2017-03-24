package com.linxonline.mallet.util.worker ;

import java.util.List ;
import java.util.Stack ;
import java.util.ArrayList ;

import java.util.concurrent.atomic.AtomicInteger ;

import com.linxonline.mallet.util.locks.* ;

public class WorkerGroup
{
	private final Stack<WorkerThread> availableWorkers = new Stack<WorkerThread>() ;
	private final ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>() ;
	private final WorkerCondition condition ;

	private final ILock lock = new Lock() ;

	public WorkerGroup()
	{
		this( 2 ) ;
	}

	public WorkerGroup( final int _threads )
	{
		availableWorkers.ensureCapacity( _threads ) ;
		workers.ensureCapacity( _threads ) ;
		condition = new WorkerCondition( _threads ) ;

		for( int i = 0; i < _threads; i++ )
		{
			final WorkerThread thread = new WorkerThread() ;
			thread.start() ;

			availableWorkers.push( thread ) ;
		}
	}

	/**
		Process the passed in worker across multiple 
		threads, this function is blocking until 
		data set has been processed completely.
	*/
	public void exec( final Worker<?> _worker )
	{
		//System.out.println( "Exec Worker Group" ) ;
		final List<?> dataset = _worker.getDataSet() ;

		int threadLength = availableWorkers.size() ;
		final int dataSize = dataset.size() ;
		if( threadLength > dataSize )
		{
			// You should never have more threads than data,
			// Else don't have more threads than data.
			threadLength = dataSize ;
		}

		int start = 0 ;
		final int range = dataSize / threadLength ; 	// Split the entities between the threads.
		condition.reset() ;

		for( int i = 0; i < threadLength; ++i )
		{
			final WorkerThread thread = availableWorkers.pop() ;
			workers.add( thread ) ;

			thread.setWorkerCondition( lock, condition ) ;
			thread.setRange( start, start + range ) ;
			thread.setWorker( _worker ) ;

			start += range ;

			thread.unpause() ;			// Resume data updating
		}

		//System.out.println( "Lock Group" ) ;
		lock.lock( condition ) ;		 // Only continue once all EntityThreads have finished

		relinquishWorkers() ;
	}

	/**
		Workers that have completed their task can be 
		put back into the available worker pool.
	*/
	private void relinquishWorkers()
	{
		if( workers.isEmpty() == false )
		{
			final int size = workers.size() ;
			for( int i = 0; i < size; i++ )
			{
				final WorkerThread thread = workers.get( i ) ;
				thread.setWorker( null ) ;
				availableWorkers.push( thread ) ;
			}
			workers.clear() ;
		}
	}

	public static class WorkerCondition implements ICondition
	{
		private final int workers ;
		private final AtomicInteger active ;

		public WorkerCondition( final int _workers )
		{
			workers = _workers ;
			active = new AtomicInteger( workers ) ;
		}

		public void reset()
		{
			//System.out.println( "Reset" ) ;
			active.set( workers ) ;
		}

		public void unregister()
		{
			//System.out.println( "Unregister" ) ;
			active.decrementAndGet() ;
		}

		public boolean isConditionMet()
		{
			//System.out.println( "Group Condition: " + active ) ;
			return active.intValue() <= 0 ;
		}
	}
}
