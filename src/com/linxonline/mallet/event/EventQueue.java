package com.linxonline.mallet.event ;

import java.util.ArrayList ;

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
		synchronized( back )
		{
			back.add( _event ) ;
		}
	}

	protected void clear()
	{
		front.clear() ;
		back.clear() ;
	}

	protected void swap()
	{
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

	/**
	* Pass the current events to the passed in _process.
	*/
	public void process( final Event.IProcess<T> _process )
	{
		if( front.isEmpty() )
		{
			return ;
		}

		final int size = front.size() ;
		for( int i = 0; i < size; ++i )
		{
			_process.process( front.get( i ).getVariable() ) ;
		}
	}
}
