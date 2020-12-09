package com.linxonline.mallet.util.locks ;

public class Condition
{
	private int criteria ;

	public Condition( final int _criteria )
	{
		criteria = _criteria ;
	}

	public synchronized void reset( final int _criteria )
	{
		criteria = _criteria  ;
	}

	public synchronized boolean isMet()
	{
		return criteria <= 0 ;
	}

	public synchronized void met()
	{
		--criteria ;
	}
}
