package com.linxonline.mallet.util.worker ;

import java.util.List ;
import java.util.concurrent.atomic.AtomicBoolean ;

import com.linxonline.mallet.util.locks.* ;

public class WorkerThread extends Thread
						  implements ICondition
{
	private boolean stop = false ;								// true == Kill the thread
	private AtomicBoolean ready = new AtomicBoolean( true ) ;	// true == ready for action, false == current working
	private ILock block = new Lock() ;							// Block thread while waiting for work
	private ILock groupLock = null ;
	private WorkerGroup.WorkerCondition groupCondition = null ;		// Lock to calling thread

	private Worker<?> worker = null ;					// Defines execution and data set
	private int start = 0 ;								// Start of data subset
	private int end = 0 ;								// End of data subset

	public WorkerThread() {}

	public void setWorkerCondition( final ILock _lock, final WorkerGroup.WorkerCondition _condition )
	{
		groupLock = _lock ;
		groupCondition = _condition ;
	}

	/**
		Define the subset of the Worker DataSet this 
		worker thread will be processing.
	*/
	public void setRange( final int _start, final int _end )
	{
		start = _start ;
		end = _end ;
	}

	/**
		Set the worker, without this the thread will 
		not process anything.
	*/
	public void setWorker( final Worker<?> _worker )
	{
		worker = _worker ;
	}

	@Override
	public void run()
	{
		ExecType type = ExecType.FINISH ;
		while( stop == false )
		{
			if( worker != null )
			{
				type = worker.exec( start, end ) ;
			}

			if( groupLock != null && groupCondition != null )
			{
				// Inform calling thread you've finished
				// Multilock will only be set if calling thread 
				// is expected to wait for the work to be completed.
				groupCondition.unregister() ;
				groupLock.unlock() ;
			}

			if( type == ExecType.FINISH )
			{
				// If the execution has been completed pause 
				// the thread and wait for further instructions.
				pause() ;
			}
		}
	}

	/**
		If true the thread is happy to accept further 
		work, if false the thread is currently processing 
		something and should not be pestered.
	*/
	public boolean ready()
	{
		return ready.get() ;
	}

	@Override
	public boolean isConditionMet()
	{
		return !ready() ;
	}
	
	/**
		Resume Worker Thread execution.
		Call setMultiLock(), setRange(), setWorker() 
		before unpausing.
	*/
	public void unpause()
	{
		ready.set( false ) ;
		block.unlock() ;
	}

	private void pause()
	{
		ready.set( true ) ;
		block.lock( this ) ;
	}

	public void end()
	{
		stop = true ;
	}
}
