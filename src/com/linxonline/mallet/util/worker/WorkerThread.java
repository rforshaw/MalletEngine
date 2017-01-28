package com.linxonline.mallet.util.worker ;

import java.util.List ;

import com.linxonline.mallet.util.locks.* ;

public class WorkerThread extends Thread
{
	private boolean stop = false ;						// true == Kill the thread
	private boolean ready = true ;						// true == ready for action, false == current working
	private LockInterface block = new JLock() ;			// Block thread while waiting for work
	private MultiLockInterface multiLock = null ;		// Lock to calling thread

	private Worker<?> worker = null ;						// Defines execution and data set
	private int start = 0 ;								// Start of data subset
	private int end = 0 ;								// End of data subset

	public WorkerThread() {}

	public void setMultiLock( final MultiLockInterface _multiLock )
	{
		multiLock = _multiLock ;
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
				/*final List list = worker.getDataSet() ;
				for( int i = start; i < end; i++ )
				{
					type = worker.exec( i, list.get( i ) ) ;		// Does the hard execution work
				}*/
			}

			if( multiLock != null )
			{
				// Inform calling thread you've finished
				// Multilock will only be set if calling thread 
				// is expected to wait for the work to be completed.
				multiLock.unlock() ;
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
		return ready ;
	}

	/**
		Resume Worker Thread execution.
		Call setMultiLock(), setRange(), setWorker() 
		before unpausing.
	*/
	public void unpause()
	{
		ready = false ;
		block.unlock() ;
	}

	private void pause()
	{
		ready = true ;
		block.lock() ;
	}

	public void end()
	{
		stop = true ;
	}
}
