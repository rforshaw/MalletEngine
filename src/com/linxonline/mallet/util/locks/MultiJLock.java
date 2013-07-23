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

	public void interest()
	{
		synchronized( lock ) { ++numLock ; }
	}
	
	public void unlock()
	{
		synchronized( lock )
		{
			--numLock ;
			if( numLock == 0 ) { lock.notifyAll() ; }
		}
	}

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