package com.linxonline.mallet.util.worker ;

import java.util.List ;
import java.util.Stack ;
import java.util.ArrayList ;

import java.util.concurrent.atomic.AtomicInteger ;

import com.linxonline.mallet.util.locks.* ;

public final class WorkerGroup
{
	private final Stack<WorkerThread> availableWorkers = new Stack<WorkerThread>() ;
	private final ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>() ;

	private final ILock lock = new Lock() ;
	private final Condition condition ;

	public WorkerGroup()
	{
		this( "Worker", 2 ) ;
	}

	public WorkerGroup( String _group, final int _threads )
	{
		availableWorkers.ensureCapacity( _threads ) ;
		workers.ensureCapacity( _threads ) ;
		condition = new Condition( lock, _threads ) ;

		for( int i = 0; i < _threads; i++ )
		{
			final WorkerThread thread = new WorkerThread( _group + Integer.toString( i ) ) ;
			thread.start() ;

			availableWorkers.push( thread ) ;
		}
	}

	/**
		Process the passed in worker across multiple 
		threads, this function is blocking until 
		data set has been processed completely.
	*/
	public <T> void exec( final List<T> _dataset, final Worker<T> _worker )
	{
		if( _dataset.isEmpty() == true )
		{
			// If there is no data to process then we 
			// might as well return early.
			return ;
		}

		int threadLength = availableWorkers.size() ;
		final int dataSize = _dataset.size() ;
		if( threadLength > dataSize )
		{
			// You should never have more threads than data,
			// Else don't have more threads than data.
			threadLength = dataSize ;
		}

		int start = 0 ;
		final int range = dataSize / threadLength ; 	// Split the entities between the threads.
		final int remainder = dataSize % threadLength ;
		condition.reset( threadLength ) ;

		for( int i = 0; i < threadLength; ++i )
		{
			final WorkerThread thread = availableWorkers.pop() ;
			workers.add( thread ) ;

			final int extra = ( i == 0 ) ? remainder : 0 ;
			final int end = start + range + extra ;

			thread.setState( condition, _worker, _dataset, start, end ) ;
			start += range + extra ;

			thread.unpause() ;			// Resume data updating
		}

		// Only continue once all WorkerThreads have finished
		lock.lock() ;

		relinquishWorkers() ;
	}

	/**
		Execute the passed in workers on the available threads.
		You cannot have more workers than available threads.
		Each worker is given their own thread to execute on, 
		allowing a worker to have their own state.
	*/
	public <T> void exec( final List<T> _dataset, final Worker<T>[] _workers )
	{
		if( _dataset.isEmpty() == true )
		{
			// If there is no data to process then we 
			// might as well return early.
			return ;
		}

		int threadLength = availableWorkers.size() ;
		if( threadLength > _workers.length )
		{
			// If the user passes in multiple workers it 
			// is expected that each worker contains its own 
			// state which cannot be used across multiple threads.
			threadLength = _workers.length ;
		}

		final int dataSize = _dataset.size() ;
		if( threadLength > dataSize )
		{
			// You should never have more threads than data,
			// Else don't have more threads than data.
			threadLength = dataSize ;
		}

		int start = 0 ;
		final int range = dataSize / threadLength ; 	// Split the entities between the threads.
		final int remainder = dataSize % threadLength ;
		condition.reset( threadLength ) ;

		for( int i = 0; i < threadLength; ++i )
		{
			final WorkerThread thread = availableWorkers.pop() ;
			workers.add( thread ) ;

			final Worker<T> worker = _workers[i] ;

			final int extra = ( i == 0 ) ? remainder : 0 ;
			final int end = start + range + extra ;

			thread.setState( condition, worker, _dataset, start, end ) ;
			start += range + extra ;

			thread.unpause() ;			// Resume data updating
		}

		// Only continue once all WorkerThreads have finished
		lock.lock() ;
		relinquishWorkers() ;
	}

	/**
		Workers that have completed their task can be 
		put back into the available worker pool.
	*/
	private boolean relinquishWorkers()
	{
		if( workers.isEmpty() == false )
		{
			final int size = workers.size() ;
			for( int i = 0; i < size; i++ )
			{
				final WorkerThread thread = workers.get( i ) ;
				thread.pause() ;
				availableWorkers.push( thread ) ;
			}
			workers.clear() ;
		}

		return true ;
	}
}
