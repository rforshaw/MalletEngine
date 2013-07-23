package com.linxonline.mallet.util.locks ;

/**
	Interface to allowing a Thread to be locked.
	Allows systems that don't support notify() & wait().
	For example, j2objc for iOS.
**/
public interface MultiLockInterface extends LockInterface
{
	/**
		allows the lock to be reset to default values.
	**/
	public void reset() ;

	/**
		Allows an object to show interest with the lock.
		If an object holds interest and lock() is called 
		then the lock will wait until all interest is lost.
		Remove interest by calling unlock().
	**/
	public void interest() ;
}