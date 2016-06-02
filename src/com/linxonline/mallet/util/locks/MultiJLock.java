package com.linxonline.mallet.util.locks ;

/**
	Thread locking implemented via Java's notifyAll() and wait() functionality.
**/
public class MultiJLock implements MultiLockInterface
{
	private final Object lock = new Object() ;
	private int numLock = 0 ;

	public void reset()
	{
		synchronized( lock ) { numLock = 0 ; }
	}

	/**
		Inform the lock that something needs 
		to be completed before lock() will continue.
	*/
	public void interest()
	{
		synchronized( lock ) { ++numLock ; }
	}

	/**
		Any party that called interest() will need to 
		eventually call unlock to allow the thread that 
		called lock to continue execution. 
	*/
	public void unlock()
	{
		synchronized( lock )
		{
			--numLock ;
			if( numLock == 0 ) { lock.notifyAll() ; }
		}
	}

	/**
		Call to block calling thread.
		Calling thread will continue when all interest()'ed 
		parties have called unlock().
	*/
	public void lock()
	{
		synchronized( lock )
		{
			try
			{
				if( numLock > 0 ) { lock.wait() ; }
			}
			catch( InterruptedException ex ) {}
		}
	}
}