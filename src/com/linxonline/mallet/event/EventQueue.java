package com.linxonline.mallet.event ;

import java.util.ArrayList ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

/**
	Add events of a particular type to the queue.
	EventQueue operates with a double-buffer, the front
	is what can currently be processed, while the back
	is where new events are added, a call to swap is
	required to bring the back buffer to the front
	for processing.

	The EventState that owns the buffer can trigger
	the swap.
*/
public final class EventQueue<T>
{
	private final EventType type ;
	private ArrayList<Event<T>> back = new ArrayList<Event<T>>() ;
	private ArrayList<Event<T>> front = new ArrayList<Event<T>>() ;

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock() ;
	private final ReentrantReadWriteLock.ReadLock rLock = lock.readLock() ;
	private final ReentrantReadWriteLock.WriteLock wLock = lock.writeLock() ;

	protected EventQueue( final EventType _type )
	{
		type = _type ;
	}

	public void add( final T _val )
	{
		add( Event.<T>create( type, _val ) ) ;
	}

	/**
	* Add the passed in event to the queue, will
	* be able to process in the next update cycle.
	*/
	public void add( final Event<T> _event )
	{
		try
		{
			wLock.lock() ;
			back.add( _event ) ;
		}
		finally
		{
			wLock.unlock() ;
		}
	}

	protected void clear()
	{
		try
		{
			wLock.lock() ;

			front.clear() ;
			back.clear() ;
		}
		finally
		{
			wLock.unlock() ;
		}
	}

	protected void swap()
	{
		try
		{
			wLock.lock() ;

			if( !front.isEmpty() )
			{
				front.clear() ;
			}

			if( back.isEmpty() )
			{
				return ;
			}

			final ArrayList<Event<T>> temp = front ;
			front = back ;
			back = temp ;
		}
		finally
		{
			wLock.unlock() ;
		}
	}

	/**
	* Pass the current events to the passed in _process.
	*/
	public void process( final Event.IProcess<? super T> _process )
	{
		try
		{
			rLock.lock() ;

			if( front.isEmpty() )
			{
				return ;
			}

			final int size = front.size() ;
			for( int i = 0; i < size; ++i )
			{
				final Event<T> event = front.get( i ) ;
				_process.process( event.getVariable() ) ;
			}
		}
		finally
		{
			rLock.unlock() ;
		}
	}
}
