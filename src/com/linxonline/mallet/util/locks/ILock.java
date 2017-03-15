package com.linxonline.mallet.util.locks ;

public interface ILock
{
	public void unlock() ;
	
	public void lock( final ICondition _condition ) ;
}
