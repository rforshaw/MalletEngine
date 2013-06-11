package com.linxonline.mallet.event ;

import java.util.Collection ;
import java.util.ArrayList ;
import java.util.HashMap ;

public final class EventSystem implements AddEventInterface
{
	// Replace HashMap with something easier to traverse.
	private final HashMap<String, EventQueue> eventQueues = new HashMap<String, EventQueue>() ;
	private final ArrayList<EventHandler> handlers = new ArrayList<EventHandler>() ;
	private final ArrayList<EventHandler> toBeRemoved = new ArrayList<EventHandler>() ;

	public EventSystem()
	{
		// Guarantee an ALL_EVENT_TYPES Queue.
		eventQueues.put( Event.ALL_EVENT_TYPES[0], new EventQueue() ) ;
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
				eventQueues.put( type, new EventQueue() ) ;
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
		Removes the EventHandler now, no questions asked.
	**/
	public void removeHandlersNow()
	{
		final int size = toBeRemoved.size() ;
		for( int i = 0; i < size; ++i )
		{
			remove( toBeRemoved.get( i ) ) ;
		}
		toBeRemoved.clear() ;
	}

	public final void addEvent( final Event _event )
	{
		final String key = _event.getEventType() ;
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
		final Collection<EventQueue> queues = eventQueues.values() ;
		for( final EventQueue queue : queues )
		{
			queue.update() ;
		}
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
		final Collection<EventQueue> queues = eventQueues.values() ;
		for( final EventQueue queue : queues )
		{
			queue.clearHandlers() ;
		}
	}

	public final void clearEvents()
	{
		final Collection<EventQueue> queues = eventQueues.values() ;
		for( final EventQueue queue : queues )
		{
			queue.clearEvents() ;
		}
	}

	public final boolean hasEvents()
	{
		return eventQueues.get( Event.ALL_EVENT_TYPES[0] ).hasEvents() ;
	}

	private final boolean exists( final EventHandler _handler )
	{
		return handlers.contains( _handler ) ;
	}
}
