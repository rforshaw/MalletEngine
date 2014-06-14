package com.linxonline.mallet.event ;

import java.util.Collection ;
import java.util.ArrayList ;
import java.util.HashMap ;

public final class EventSystem implements AddEventInterface
{
	// Replace HashMap with something easier to traverse.
	private final String name ;
	private final HashMap<String, EventQueue> eventQueues = new HashMap<String, EventQueue>() ;
	private final ArrayList<EventQueue> queues = new ArrayList<EventQueue>() ;

	private final ArrayList<EventHandler> handlers = new ArrayList<EventHandler>() ;
	private final ArrayList<EventHandler> toBeRemoved = new ArrayList<EventHandler>() ;

	public EventSystem()
	{
		name = "NONE" ;
		// Guarantee an ALL_EVENT_TYPES Queue.
		addEventQueue( Event.ALL_EVENT_TYPES[0], new EventQueue( "ALL" ) ) ;
	}

	public EventSystem( final String _name )
	{
		name = _name ;
		// Guarantee an ALL_EVENT_TYPES Queue.
		addEventQueue( Event.ALL_EVENT_TYPES[0], new EventQueue( "ALL" ) ) ;
	}

	/**
		Add EventHandler to handlers list & search through 
		the EventHandlers EventTypes, placing it in the correct 
		lists, so Events can be filtered correctly.
	**/
	public final void addEventHandler( final EventHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			System.out.println( "Already Exists" ) ;
			return ;
		}

		final String[] types = _handler.getWantedEventTypes() ;
		for( final String type : types )
		{
			if( eventQueues.containsKey( type ) == false )
			{
				addEventQueue( type, new EventQueue( type ) ) ;
			}

			eventQueues.get( type ).addEventHandler( _handler ) ;
		}

		handlers.add( _handler ) ;
	}

	/**
		Removes the EventHandler in the next update().
	**/
	public final void removeEventHandler( final EventHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		toBeRemoved.add( _handler ) ;
	}

	/**
		Remove the EventHandlers queued for removal now.
	**/
	public void removeHandlersNow()
	{
		final int size = toBeRemoved.size() ;
		if( size > 0 )
		{
			for( int i = 0; i < size; ++i )
			{
				remove( toBeRemoved.get( i ) ) ;
			}
			toBeRemoved.clear() ;
		}
	}

	public final void addEvent( final Event _event )
	{
		final String key = _event.getEventType() ;
		//System.out.println( name + " " + key ) ;
		if( eventQueues.containsKey( key ) == true )
		{
			eventQueues.get( key ).addEvent( _event ) ;
		}

		if( eventQueues.containsKey( Event.ALL_EVENT_TYPES[0] ) == true )
		{
			eventQueues.get( Event.ALL_EVENT_TYPES[0] ).addEvent( _event ) ;
		}
	}

	public final void update()
	{
		removeHandlersNow() ;
		final int size = queues.size() ;
		for( int i = 0; i < size; ++i )
		{
			queues.get( i ).update() ;
		}
	}

	protected void addEventQueue( final String _queueName, final EventQueue _queue )
	{
		eventQueues.put( _queueName, _queue ) ;
		queues.add( _queue ) ;
	}
	
	private void remove( final EventHandler _handler )
	{
		final String[] types = _handler.getWantedEventTypes() ;
		for( final String type : types )
		{
			if( eventQueues.containsKey( type ) == true )
			{
				eventQueues.get( type ).removeEventHandler( _handler ) ;
			}
		}
		handlers.remove( _handler ) ;
	}
	
	public final void clearHandlers()
	{
		handlers.clear() ;
		toBeRemoved.clear() ;

		final int size = queues.size() ;
		for( int i = 0; i < size; ++i )
		{
			queues.get( i ).clearHandlers() ;
		}
	}

	public final void clearEvents()
	{
		final int size = queues.size() ;
		for( int i = 0; i < size; ++i )
		{
			queues.get( i ).clearEvents() ;
		}
	}

	public final boolean hasEvents()
	{
		return eventQueues.get( Event.ALL_EVENT_TYPES[0] ).hasEvents() ;
	}

	private final boolean exists( final EventHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}
}
