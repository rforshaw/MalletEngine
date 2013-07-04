package com.linxonline.mallet.util.locks ;

import java.util.HashMap ;

/**
	Allows you to create a Lock that has global access.
	This should be used carefully, as you may cause systems to become 
	tightly coupled.
**/
public class Locks
{
	private static Locks instance = null ;
	private final HashMap<String, LockInterface> locks = new HashMap<String, LockInterface>() ;

	private Locks() {}

	public synchronized static Locks getLocks()
	{
		if( instance == null ) { instance = new Locks() ; }
		return instance ;
	}

	public synchronized void addLock( final String _key, final LockInterface _lock )
	{
		if( locks.containsKey( _key ) == false )
		{
			locks.put( _key, _lock ) ;
		}
	}

	public synchronized void removeLock( final String _key )
	{
		if( locks.containsKey( _key ) == true )
		{
			locks.remove( _key ) ;
		}
	}

	public synchronized LockInterface getLock( final String _key )
	{
		return locks.get( _key ) ;
	}
}