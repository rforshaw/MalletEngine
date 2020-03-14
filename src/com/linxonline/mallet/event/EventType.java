package com.linxonline.mallet.event ;

import java.util.Map ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.Iterator ;

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
	private final static Map<String, EventType> eventTypes = MalletMap.<String, EventType>newMap() ;
	private static int incrementID = 0 ;

	private final String type ;
	private final int id ;

	private EventType( final String _type, final int _id )
	{
		type = _type ;
		id = _id ;
	}

	public int getID()
	{
		return id ;
	}

	public String toString()
	{
		return type ;
	}

	/**
		Return the EventType corresponding to the String 
		passed in. Create an EventType if one doesn't exist.
	*/
	public static EventType get( final String _type )
	{
		synchronized( eventTypes )
		{
			final EventType type = eventTypes.get( _type ) ;
			if( type != null )
			{
				return type ;
			}

			final EventType newType = new EventType( _type, incrementID++ ) ;
			eventTypes.put( _type, newType ) ;
			return newType ;
		}
	}

	public static class Lookup<T> implements Iterable<T>
	{
		private T fallback = null ;
		private ArrayList<T> types = new ArrayList<T>() ;

		public Lookup() {}

		public Lookup( T _fallback )
		{
			fallback = _fallback ;
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

		@Override
		public Iterator<T> iterator() 
		{ 
			return new LookupIterator( types.iterator() ) ; 
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
	
	private static class LookupIterator<T> implements Iterator<T>
	{
		private final Iterator<T> iter ;
		private T next = null ;

		public LookupIterator( Iterator<T> _iter )
		{
			iter = _iter ;
		}

		@Override
		public boolean hasNext()
		{
			while( iter.hasNext() )
			{
				next = iter.next() ;
				if( next != null )
				{
					return true ;
				}
			}

			return false ;
		}

		@Override
		public T next()
		{
			return next ;
		}
	}
}
