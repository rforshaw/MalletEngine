package com.linxonline.mallet.util.locks ;

public class Condition
{
	private final ILock lock ;
	private int criteria ;

	public Condition( final ILock _lock, final int _criteria )
	{
		lock = _lock ;
		criteria = _criteria ;
	}

	public synchronized void reset( final int _criteria )
	{
		criteria = _criteria  ;
	}

	public void met()
	{
		boolean conditionMet = false ;
		synchronized( this )
		{
			--criteria ;
			conditionMet = ( criteria <= 0 ) ;
		}

		if( conditionMet == true )
		{
			lock.unlock() ;
		}
	}
}
