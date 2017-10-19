package com.linxonline.mallet.util.worker ;

import java.util.List ;
import java.util.concurrent.atomic.AtomicBoolean ;

import com.linxonline.mallet.util.locks.* ;

public class WorkerThread extends Thread
{
	private boolean stop = false ;								// true == Kill the thread
	private boolean paused = true ;

	private Object block = new Object() ;						// Block thread while waiting for work
	private ILock groupLock = null ;
	private WorkerGroup.WorkerCondition groupCondition = null ;		// Lock to calling thread
	
	private Worker<?> worker = null ;					// Defines execution and data set
	private int start = 0 ;								// Start of data subset
	private int end = 0 ;								// End of data subset

	public WorkerThread()
	{
		super( "WorkerThread" ) ;
	}

	public void setWorkerCondition( final ILock _lock, final WorkerGroup.WorkerCondition _condition )
	{
		synchronized( block )
		{
			groupLock = _lock ;
			groupCondition = _condition ;
		}
	}

	/**
		Define the subset of the Worker DataSet this 
		worker thread will be processing.
	*/
	public void setRange( final int _start, final int _end )
	{
		synchronized( block )
		{
			start = _start ;
			end = _end ;
		}
	}

	/**
		Set the worker, without this the thread will 
		not process anything.
	*/
	public void setWorker( final Worker<?> _worker )
	{
		synchronized( block )
		{
			worker = _worker ;
		}
	}

	@Override
	public void run()
	{
		ExecType type = ExecType.FINISH ;
		while( stop == false )
		{
			try
			{
				while( paused == true )
				{
					synchronized( block )
					{
						block.wait() ;
					}
				}
			}
			catch( InterruptedException ex ) {}

			if( worker != null )
			{
				// Execute the work specified by the developer
				//System.out.println( "Exec Worker: " + start + " to: " + end ) ;
				type = worker.exec( start, end ) ;
				//System.out.println( "Work Complete!" ) ;

				setWorker( null ) ;
				paused = true ;

				if( groupLock != null && groupCondition != null )
				{
					// Inform calling WorkerGroup/thread you've finished
					// Multilock will only be set if calling thread 
					// is expected to wait for the work to be completed.
					//System.out.println( "Unregister" ) ;
					groupCondition.unregister() ;
					groupLock.unlock() ;
				}
			}
		}
	}

	/**
		Resume Worker Thread execution.
		Call setMultiLock(), setRange(), setWorker() 
		before unpausing.
	*/
	public void unpause()
	{
		synchronized( block )
		{
			//System.out.println( "Request unpause" ) ;
			paused = false ;
			block.notify() ;
		}
	}

	public void pause()
	{
		synchronized( block )
		{
			//System.out.println( "Request pause" ) ;
			paused = true ;
			block.notify() ;
		}
	}

	public synchronized void end()
	{
		stop = true ;
	}
}
