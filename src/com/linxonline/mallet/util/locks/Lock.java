package com.linxonline.mallet.util.locks ;

public class Lock implements ILock
{
	private final Object lock = new Object() ;
	private boolean shouldLock = false ;

	@Override
	public void unlock()
	{
		synchronized( lock )
		{
			shouldLock = false ;
			lock.notifyAll() ;
		}
	}

	@Override
	public void lock()
	{
		synchronized( lock )
		{
			shouldLock = true ;
			try
			{
				while( shouldLock == true )
				{
					lock.wait() ;
				}
			}
			catch( InterruptedException ex ) {}
		}
	}
}
