package com.linxonline.mallet.event ;

import java.util.Collection ;
import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

public final class EventSystem implements IEventSystem
{
	// Replace Map with something easier to traverse.
	private final String name ;
	private final Map<EventType, EventQueue> eventQueues = MalletMap.<EventType, EventQueue>newMap() ;
	private final List<EventQueue> queues = MalletList.<EventQueue>newList() ;
	private final EventQueue allQueue = new EventQueue( Event.ALL_EVENT_TYPES ) ;

	private final List<IEventHandler> handlers = MalletList.<IEventHandler>newList() ;
	private final List<IEventHandler> toBeRemoved = MalletList.<IEventHandler>newList() ;

	public EventSystem()
	{
		this( "NONE" ) ;
	}

	public EventSystem( final String _name )
	{
		name = _name ;
		// Guarantee an ALL_EVENT_TYPES Queue.
		addEventQueue( Event.ALL_EVENT_TYPES, allQueue ) ;
	}

	/**
		Add EventHandler to handlers list & search through 
		the EventHandlers EventTypes, placing it in the correct 
		lists, so Events can be filtered correctly.
	**/
	public final void addEventHandler( final IEventHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			Logger.println( _handler.getName() + "already exists within " + name, Logger.Verbosity.MAJOR ) ;
			return ;
		}

		handlers.add( _handler ) ;

		final List<EventType> types = _handler.getWantedEventTypes() ;
		if( types.isEmpty() == true || types.contains( Event.ALL_EVENT_TYPES ) == true )
		{
			// Due to legacy we must assumme that a types size of 0, 
			// represents a developer wishing to recieve all Events.
			// If the types contains ALL_EVENT_TYPES then only add it 
			// to the ALL_EVENT_TYPES EventQueue.
			allQueue.addEventHandler( _handler ) ;
			return ;
		}

		if( types.isEmpty() == false )
		{
			final int size = types.size() ;
			for( int i = 0; i < size; i++ )
			{
				final EventType type = types.get( i ) ;
				if( eventQueues.containsKey( type ) == false )
				{
					addEventQueue( type, new EventQueue( type ) ) ;
				}

				eventQueues.get( type ).addEventHandler( _handler ) ;
			}
		}
	}

	/**
		Removes the EventHandler in the next update().
	**/
	public final void removeEventHandler( final IEventHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		toBeRemoved.add( _handler ) ;
	}

	public final void addEventFilter( final EventType _type, final IEventFilter _filter )
	{
		assert _type != null || _filter != null ;

		if( eventQueues.containsKey( _type ) == false )
		{
			addEventQueue( _type, new EventQueue( _type ) ) ;
		}

		eventQueues.get( _type ).addEventFilter( _filter ) ;
	}

	public final void removeEventFilter( final EventType _type, final IEventFilter _filter )
	{
		assert _type != null || _filter != null ;

		if( eventQueues.containsKey( _type ) == false )
		{
			Logger.println( "Can't remove EventFilter event queue: " + _type + " does not exist.", Logger.Verbosity.MAJOR ) ;
		}

		eventQueues.get( _type ).removeEventFilter( _filter ) ;
	}
	
	/**
		Remove the EventHandlers queued for removal now.
	**/
	public void removeHandlersNow()
	{
		if( toBeRemoved.isEmpty() == false )
		{
			final int size = toBeRemoved.size() ;
			for( int i = 0; i < size; ++i )
			{
				remove( toBeRemoved.get( i ) ) ;
			}
			toBeRemoved.clear() ;
		}
	}

	@Override
	public final void addEvent( final Event<?> _event )
	{
		final EventType key = _event.getEventType() ;
		// System.out.println( name + " " + key ) ;

		final EventQueue queue = eventQueues.get( key ) ;
		// We don't want to add an event flagged ALL_EVENT_TYPES
		// multiple times into the same queue.
		if( queue != null && queue != allQueue )
		{
			//System.out.println( "Found: " + key + " Event Queue." ) ;
			queue.addEvent( _event ) ;
		}

		// We always want to pass the Event to ALL_EVENT_TYPES
		// queue irrespective of its EventType.
		allQueue.addEvent( _event ) ;
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

	private void addEventQueue( final EventType _queueName, final EventQueue _queue )
	{
		eventQueues.put( _queueName, _queue ) ;
		queues.add( _queue ) ;
	}
	
	private void remove( final IEventHandler _handler )
	{
		final List<EventType> types = _handler.getWantedEventTypes() ;
		if( types.isEmpty() == true )
		{
			// Due to legacy we must assumme that a types size of 0, 
			// represents Event.ALL_EVENT_TYPES, must be specially removed.
			eventQueues.get( Event.ALL_EVENT_TYPES ).removeEventHandler( _handler ) ;
		}

		for( final EventType type : types )
		{
			final EventQueue queue = eventQueues.get( type ) ;
			if( queue != null )
			{
				queue.removeEventHandler( _handler ) ;
			}
		}

		handlers.remove( _handler ) ;
		_handler.reset() ;				// Should clear any 
	}

	/**
		Return the Event Systems name, not considered unique.
	*/
	public String getName()
	{
		return name ;
	}

	public int getEventSize()
	{
		int size = 0 ;
		final int length = queues.size() ;
		for( int i = 0; i < length; ++i )
		{
			size += queues.get( i ).size() ;
		}

		return size ;
	}

	public int getHandlerSize()
	{
		return handlers.size() ;
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
		return eventQueues.get( Event.ALL_EVENT_TYPES ).hasEvents() ;
	}

	private final boolean exists( final IEventHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "[Event System: " + getName() ) ;
		for( final EventQueue queue : queues )
		{
			buffer.append( queue.toString() + "," ) ;
		}
		buffer.append( "]" ) ;
		return buffer.toString() ;
	}
}
