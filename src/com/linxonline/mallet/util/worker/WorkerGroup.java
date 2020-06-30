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
	private final WorkerCondition condition ;

	private final ILock lock = new Lock() ;

	public WorkerGroup()
	{
		this( "Worker", 2 ) ;
	}

	public WorkerGroup( String _group, final int _threads )
	{
		availableWorkers.ensureCapacity( _threads ) ;
		workers.ensureCapacity( _threads ) ;
		condition = new WorkerCondition( _threads ) ;

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
		//System.out.println( "Exec Worker Group" ) ;
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

		//System.out.println( "Available: " + threadLength + " Amount: " + dataSize + " Divided: " + range + " Remainder: " + remainder  ) ;
		//System.out.println( "Start Group: " + range ) ;
		for( int i = 0; i < threadLength; ++i )
		{
			final WorkerThread thread = availableWorkers.pop() ;
			workers.add( thread ) ;

			final int extra = ( i == 0 ) ? remainder : 0 ;

			thread.setWorkerCondition( lock, condition ) ;
			thread.setRange( _dataset, start, start + range + extra ) ;
			thread.setWorker( _worker ) ;

			start += range + extra ;

			thread.unpause() ;			// Resume data updating
		}

		//System.out.println( "Lock Group" ) ;
		// Only continue once all WorkerThreads have finished
		lock.lock( condition ) ;

		//System.out.println( "Relinquish Group" ) ;
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
		//System.out.println( "Exec Worker Group" ) ;
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

		//System.out.println( "Available: " + threadLength + " Amount: " + dataSize + " Divided: " + range + " Remainder: " + remainder  ) ;
		//System.out.println( "Start Group: " + range ) ;
		for( int i = 0; i < threadLength; ++i )
		{
			final WorkerThread thread = availableWorkers.pop() ;
			workers.add( thread ) ;

			final Worker<T> worker = _workers[i] ;

			final int extra = ( i == 0 ) ? remainder : 0 ;

			thread.setWorkerCondition( lock, condition ) ;
			thread.setRange( _dataset, start, start + range + extra ) ;
			thread.setWorker( worker ) ;

			start += range + extra ;

			thread.unpause() ;			// Resume data updating
		}

		//System.out.println( "Lock Group" ) ;
		// Only continue once all WorkerThreads have finished
		lock.lock( condition ) ;

		//System.out.println( "Relinquish Group" ) ;
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

	public static class WorkerCondition implements ICondition
	{
		private int active ;

		public WorkerCondition( final int _workers )
		{
			active = _workers ;
		}

		public synchronized void reset( final int _workers )
		{
			//System.out.println( "Reset: " + _workers ) ;
			active = _workers  ;
		}

		public synchronized void unregister()
		{
			//System.out.println( "Unregister" ) ;
			--active ;
		}

		public synchronized boolean isConditionMet()
		{
			//System.out.println( "Group Condition: " + active ) ;
			return active <= 0 ;
		}
	}
}
