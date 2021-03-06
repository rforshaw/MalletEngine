package com.linxonline.mallet.util.worker ;

import java.util.List ;
import java.util.concurrent.atomic.AtomicBoolean ;

import com.linxonline.mallet.util.locks.* ;

public final class WorkerThread extends Thread
{
	private boolean stop = false ;									// true == Kill the thread
	private boolean paused = true ;

	private Object block = new Object() ;			// Block thread while waiting for work
	private Condition groupCondition = null ;		// Lock to calling thread
	
	private Worker<?> worker = null ;					// Defines execution and data set
	private List dataset = null ;
	private int start = 0 ;								// Start of data subset
	private int end = 0 ;								// End of data subset

	public WorkerThread()
	{
		this( "WorkerThread" ) ;
	}

	public WorkerThread( String _name )
	{
		super( _name ) ;
	}

	public void setState( final Condition _condition,
						  final Worker<?> _worker,
						  final List _dataset,
						  final int _start,
						  final int _end )
	{
		synchronized( block )
		{
			setWorker( _condition, _worker ) ;
			setRange( _dataset, _start, _end ) ;
		}
	}

	/**
		Define the subset of the Worker DataSet this 
		worker thread will be processing.
	*/
	private void setRange( final List _dataset, final int _start, final int _end )
	{
		dataset = _dataset ;
		start = _start ;
		end = _end ;
	}

	/**
		Set the worker, without this the thread will 
		not process anything.
	*/
	private void setWorker( final Condition _condition,final Worker<?> _worker )
	{
		worker = _worker ;
		groupCondition = _condition ;
	}

	@Override
	public void run()
	{
		while( stop == false )
		{
			try
			{
				synchronized( block )
				{
					// Block this thread until another thread resumes it,
					// this should be the Main thread from a WorkerGroup.
					while( paused == true )
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
				if( worker.exec( dataset, start, end ) == Worker.ExecType.FINISH )
				{
					setWorker( groupCondition, null ) ;
					setRange( null, 0, 0 ) ;
					paused = true ;
				}

				if( groupCondition != null )
				{
					// Inform calling WorkerGroup/thread you've finished
					// Multilock will only be set if calling thread 
					// is expected to wait for the work to be completed.
					//System.out.println( "Unregister" ) ;
					groupCondition.met() ;
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
			if( paused == true )
			{
				//System.out.println( getName() + " request unpause" ) ;
				paused = false ;
				block.notifyAll() ;
			}
		}
	}

	public void pause()
	{
		synchronized( block )
		{
			//System.out.println( getName() + " request pause" ) ;
			if( paused == false )
			{
				//System.out.println( getName() + " request pause" ) ;
				paused = true ;
			}
		}
	}

	public void end()
	{
		synchronized( block )
		{
			stop = true ;
		}
	}
}
