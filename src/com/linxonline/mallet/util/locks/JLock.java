package com.linxonline.mallet.util.locks ;

/**
	Thread locking implemented via Java's notifyAll() and wait() functionality.
**/
public class JLock implements LockInterface
{
	private final Object lock = new Object() ;

	@Override
	public void unlock()
	{
		synchronized( lock )
		{
			lock.notifyAll() ;
		}
	}

	@Override
	public void lock()
	{
		synchronized( lock )
		{
			try { lock.wait() ; }
			catch( InterruptedException ex ) {}
		}
	}
}