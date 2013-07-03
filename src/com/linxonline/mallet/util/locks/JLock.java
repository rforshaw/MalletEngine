package com.linxonline.mallet.util.locks ;

/**
	Thread locking implemented via Java's notifyAll() and wait() functionality.
**/
public interface JLock
{
	private final Object lock = new Object() ;

	public void unlock()
	{
		synchronized( lock )
		{
			lock.notifyAll() ;
		}
	}

	public void lock()
	{
		synchronized( lock )
		{
			try
			{
				lock.wait()
			}
			catch( InterruptedException ex ) {}
		}
	}
}