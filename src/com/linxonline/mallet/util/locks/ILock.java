package com.linxonline.mallet.util.locks ;

public interface ILock
{
	public boolean isLocked() ;

	public void unlock() ;

	public void lock() ;
}
