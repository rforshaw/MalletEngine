package com.linxonline.mallet.util.locks ;

/**
	Interface to allowing a Thread to be locked.
**/
public interface LockInterface
{
	/**
		Call this if you want all Threads that are blocked on this Lock to resume.
	**/
	public void unlock() ;
	
	/**
		Call this if you want the current running Thread to be blocked.
	**/
	public void lock() ;
}