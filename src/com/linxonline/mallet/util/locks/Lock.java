package com.linxonline.mallet.util.locks ;

public class Lock implements ILock
{
	private final Object lock = new Object() ;
	private boolean shouldLock = false ;

	@Override
	public boolean isLocked()
	{
		synchronized( lock )
		{
			return shouldLock ;
		}
	}

	@Override
	public void unlock()
	{
		System.out.println( "Unlock" ) ;
		synchronized( lock )
		{
			System.out.println( "Notify All: " + shouldLock ) ;
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
					System.out.println( "Wait: " + shouldLock ) ;
					lock.wait() ;
				}
			}
			catch( InterruptedException ex )
			{
				System.out.println( ex ) ;
			}
		}
	}
}
