package com.linxonline.mallet.event ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

/**
	Manages EventQueues by their EventType.
	See Event for access to the Global Event State.
	Can create your own instance to manage queues for a specific purpose.
	
*/
public final class EventState
{
	private static final IIntercept FALLBACK_INTERCEPT = new IIntercept()
	{
		@Override
		public boolean allow( final Event<?> _event )
		{
			return true ;
		}
	} ;

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock() ;
	private final ReentrantReadWriteLock.ReadLock rLock = lock.readLock() ;
	private final ReentrantReadWriteLock.WriteLock wLock = lock.writeLock() ;

	private final EventType.Lookup<EventQueue> lookup = new EventType.Lookup<EventQueue>() ;
	private final ArrayList<EventQueue> list = new ArrayList<EventQueue>() ;

	private IIntercept intercept = FALLBACK_INTERCEPT ;

	public void setIntercept( final IIntercept _intercept )
	{
		intercept = ( _intercept != null ) ? _intercept : FALLBACK_INTERCEPT ;
	}

	/**
		Convenience function for adding an event to the
		required EventQueue, if the queue does not exist it
		will be created.

		For best performance grab the EventQueue with .get()
		retain the reference and call add() directly.
	*/
	public void addEvent( final Event<?> _event )
	{
		wLock.lock() ;

		try
		{
			if( !intercept.allow( _event ) )
			{
				return ;
			}

			final EventQueue exists = getRaw( _event.getEventType() ) ;
			exists.add( _event ) ;
		}
		finally
		{
			wLock.unlock() ;
		}
	}

	/**
		Convenience function for adding events to the
		required EventQueues, if the queue does not exist it
		will be created.

		For best performance grab the EventQueue with .get()
		retain the reference and call add() directly.
	*/
	public void addEvents( final List<Event<?>> _events )
	{
		final int size = _events.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Event event = _events.get( i ) ;
			if( !intercept.allow( event ) )
			{
				continue ;
			}

			final EventQueue exists = getRaw( event.getEventType() ) ;
			exists.add( event ) ;
		}
	}

	/**
		Get the event-queue associated with the event-type name.
		If the event-queue does not exist, create it.
	*/
	public <T> EventQueue<T> get( final String _type )
	{
		return get( EventType.get( _type ) ) ;
	}

	/**
		Get the event-queue associated with the event-type.
		If the event-queue does not exist, create it.
	*/
	public <T> EventQueue<T> get( final EventType _type )
	{
		return getRaw( _type ) ;
	}

	public void clear()
	{
		rLock.lock() ;

		try
		{
			final int size = list.size() ;
			for( int i = 0; i < size; ++i )
			{
				final EventQueue q = list.get( i ) ;
				q.clear() ;
			}
		}
		finally
		{
			rLock.unlock() ;
		}
	}

	/**
		Loop over all EventQueues associated with this
		state and make their baking buffer the front,
		and their front buffer the back.
	*/
	public void swap()
	{
		// It's deliberately lookup - we use lookup as the
		// sync point when using either lookup or list.
		final int size = list.size() ;
		for( int i = 0; i < size; ++i )
		{
			final EventQueue q = list.get( i ) ;
			q.swap() ;
		}
	}

	private <T> EventQueue<T> getRaw( final EventType _type )
	{
		rLock.lock() ;

		try
		{
			final EventQueue<T> exists = lookup.get( _type ) ;
			if( exists != null )
			{
				return exists ;
			}
		}
		finally
		{
			rLock.unlock() ;
		}

		final EventQueue<T> queue = new EventQueue<T>( _type ) ;
		wLock.lock() ;

		try
		{
			lookup.add( _type, queue ) ;
			list.add( queue ) ;
		}
		finally
		{
			wLock.unlock() ;
		}

		return queue ;
	}
}
