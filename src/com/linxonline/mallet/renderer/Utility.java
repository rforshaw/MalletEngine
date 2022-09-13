package com.linxonline.mallet.renderer ;

public final class Utility
{
	private int index = 0 ;

	public Utility() {}

	public synchronized int getGlobalIndex()
	{
		return index++ ;
	}
}
