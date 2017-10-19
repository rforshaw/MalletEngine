package com.linxonline.mallet.util.locks ;

public class Lock implements ILock
{
	private final Object lock = new Object() ;

	public void unlock()
	{
		synchronized( lock )
		{
			lock.notify() ;
		}
	}

	public void lock( final ICondition _condition )
	{
		synchronized( lock )
		{
			try
			{
				while( _condition.isConditionMet() == false )
				{
					lock.wait() ;
				}
			}
			catch( InterruptedException ex ) {}
		}
	}
}
