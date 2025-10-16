package com.linxonline.mallet.event ;

import java.util.Map ;
import java.util.ArrayList ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import com.linxonline.mallet.util.MalletMap ;

/**
	The aim of Event Type is to provide faster checks 
	when processing Events.
	An EventType is created to match-up with a specific 
	keyword, all Events that use that specific keyword 
	will be given the same EventType.
	Instead of a String comparison being used, a memory 
	address comparison is done.
	This implementation still allows the developer to handle 
	Events as if their EventType was a String.
*/
public final class EventType
{
	private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock() ;
	private final static ReentrantReadWriteLock.ReadLock rLock = lock.readLock() ;
	private final static ReentrantReadWriteLock.WriteLock wLock = lock.writeLock() ;

	private final static Map<String, EventType> eventTypes = MalletMap.<String, EventType>newMap() ;
	private static int incrementID = 0 ;

	private final String type ;
	private final int id ;

	private EventType( final String _type, final int _id )
	{
		type = _type ;
		id = _id ;
	}

	public String getType()
	{
		return type ;
	}

	public int getID()
	{
		return id ;
	}

	@Override
	public int hashCode()
	{
		return id ;
	}

	@Override
	public boolean equals( final Object _c )
	{
		if( _c instanceof EventType et )
		{
			return id == et.id ;
		}

		return false ;
	}

	@Override
	public String toString()
	{
		return "[Type: " + type + ", id: " + id + "]" ;
	}

	/**
		Return the EventType corresponding to the String 
		passed in. Create an EventType if one doesn't exist.
	*/
	public static EventType get( final String _type )
	{
		try
		{
			rLock.lock() ;

			final EventType type = eventTypes.get( _type ) ;
			if( type != null )
			{
				return type ;
			}
		}
		finally
		{
			rLock.unlock() ;
		}

		try
		{
			wLock.lock() ;

			final EventType newType = new EventType( _type, incrementID++ ) ;
			eventTypes.put( _type, newType ) ;
			return newType ;
		}
		finally
		{
			wLock.unlock() ;
		}
	}

	public static class Lookup<T>
	{
		private final T fallback ;
		private final ArrayList<T> types ;

		public Lookup()
		{
			this( null ) ;
		}

		public Lookup( T _fallback )
		{
			this( 10, _fallback ) ;
		}

		public Lookup( int _capacity, T _fallback )
		{
			fallback = _fallback ;
			types = new ArrayList<T>( _capacity ) ;
		}

		public T add( final EventType _type, final T _variable )
		{
			final int id = _type.getID() ;
			ensureCapacity( id + 1 ) ;
			types.set( id, _variable ) ;
			return _variable ;
		}

		public void remove( final EventType _type )
		{
			final int id = _type.getID() ;
			ensureCapacity( id + 1 ) ;
			types.set( id, null ) ;
		}

		public T get( final EventType _type )
		{
			final int id = _type.getID() ;
			ensureCapacity( id + 1 ) ;
			return types.get( id ) ;
		}

		public void clear()
		{
			types.clear() ;
		}

		private void ensureCapacity( final int _size )
		{
			final int origSize = types.size() ;
			if( _size < origSize )
			{
				return ;
			}

			types.ensureCapacity( _size ) ;

			for( int i = origSize; i < _size; i++ )
			{
				types.add( fallback ) ;
			}
		}
	}
}
