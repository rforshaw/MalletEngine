package com.linxonline.mallet.util.worker ;

import java.util.List ;
import java.util.Stack ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.locks.* ;

public class WorkerGroup
{
	private final Stack<WorkerThread> availableWorkers = new Stack<WorkerThread>() ;
	private final ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>() ;

	private final MultiJLock multiLock = new MultiJLock() ;

	public WorkerGroup()
	{
		this( 2 ) ;
	}

	public WorkerGroup( final int _threads )
	{
		availableWorkers.ensureCapacity( _threads ) ;
		workers.ensureCapacity( _threads ) ;

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
	public void exec( final Worker _worker )
	{
		multiLock.reset() ;
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

		for( int i = 0; i < threadLength; ++i )
		{
			final WorkerThread worker = availableWorkers.pop() ;
			workers.add( worker ) ;

			worker.setMultiLock( multiLock ) ;
			worker.setRange( start, start + range ) ;
			worker.setWorker( _worker ) ;

			start += range ;

			multiLock.interest() ;
			worker.unpause() ;			// Resume data updating
		}

		multiLock.lock() ;		 		// Only continue once all EntityThreads have finished

		relinquishWorkers() ;
	}

	/**
		Workers that have completed their task can be 
		put back into the available worker pool.
	*/
	private void relinquishWorkers()
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
