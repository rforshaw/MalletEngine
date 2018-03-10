package com.linxonline.mallet.event ;

import java.lang.ref.WeakReference ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

public final class EventSystem implements IEventSystem
{
	private final String name ;
	private final List<EventQueue> queues = MalletList.<EventQueue>newList() ;
	private final EventQueue allQueue = new EventQueue( EventType.ALL ) ;

	private final List<WeakReference<IEventHandler>> handlers = MalletList.<WeakReference<IEventHandler>>newList() ;
	private final List<WeakReference<IEventHandler>> toBeRemoved = MalletList.<WeakReference<IEventHandler>>newList() ;

	public EventSystem()
	{
		this( null ) ;
	}

	public EventSystem( final String _name )
	{
		name = ( _name != null ) ? _name : "NONE" ;
		// Guarantee an ALL Queue.
		addEventQueue( EventType.ALL, allQueue ) ;
	}

	/**
		Add EventHandler to handlers list & search through 
		the EventHandlers EventTypes, placing it in the correct 
		lists, so Events can be filtered correctly.
	**/
	public final void addEventHandler( final IEventHandler _handler )
	{
		if( exists( _handler ) >= 0 )
		{
			Logger.println( _handler.getName() + "already exists within " + name, Logger.Verbosity.MAJOR ) ;
			return ;
		}

		final WeakReference weakHandler = new WeakReference( _handler ) ;
		handlers.add( weakHandler ) ;

		final List<EventType> types = _handler.getWantedEventTypes() ;
		if( types.isEmpty() == true || types.contains( EventType.ALL ) == true )
		{
			// Due to legacy we must assumme that a types size of 0, 
			// represents a developer wishing to recieve all Events.
			// If the types contains ALL then only add it 
			// to the ALL EventQueue.
			allQueue.addEventHandler( weakHandler ) ;
			return ;
		}

		if( types.isEmpty() == false )
		{
			final int size = types.size() ;
			for( int i = 0; i < size; i++ )
			{
				final EventType type = types.get( i ) ;
				if( getEventQueue( type ) == null )
				{
					addEventQueue( type, new EventQueue( type ) ) ;
				}

				getEventQueue( type ).addEventHandler( weakHandler ) ;
			}
		}
	}

	/**
		Removes the EventHandler in the next update().
	**/
	public final void removeEventHandler( final IEventHandler _handler )
	{
		final int index = exists( _handler ) ;
		if( index >= 0 )
		{
			toBeRemoved.add( handlers.get( index ) ) ;
		}
	}

	public final void addEventFilter( final EventType _type, final IEventFilter _filter )
	{
		assert _type != null || _filter != null ;

		if( getEventQueue( _type ) == null )
		{
			addEventQueue( _type, new EventQueue( _type ) ) ;
		}

		getEventQueue( _type ).addEventFilter( _filter ) ;
	}

	public final void removeEventFilter( final EventType _type, final IEventFilter _filter )
	{
		assert _type != null || _filter != null ;

		if( getEventQueue( _type ) == null )
		{
			Logger.println( "Can't remove EventFilter event queue: " + _type + " does not exist.", Logger.Verbosity.MAJOR ) ;
		}

		getEventQueue( _type ).removeEventFilter( _filter ) ;
	}
	
	/**
		Remove the EventHandlers queued for removal now.
	**/
	public void removeHandlersNow()
	{
		if( toBeRemoved.isEmpty() )
		{
			return ;
		}

		final int size = toBeRemoved.size() ;
		for( int i = 0; i < size; ++i )
		{
			remove( toBeRemoved.get( i ) ) ;
		}
		toBeRemoved.clear() ;
	}

	@Override
	public final void addEvent( final Event<?> _event )
	{
		final EventType key = _event.getEventType() ;
		// System.out.println( name + " " + key ) ;

		final EventQueue queue = getEventQueue( key ) ;
		// We don't want to add an event flagged ALL
		// multiple times into the same queue.
		if( queue != null && queue != allQueue )
		{
			//System.out.println( "Found: " + key + " Event Queue." ) ;
			queue.addEvent( _event ) ;
		}

		// We always want to pass the Event to ALL
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
		queues.add( _queue ) ;
	}
	
	private void remove( final WeakReference<IEventHandler> _handler )
	{
		final IEventHandler handler = _handler.get() ;
		final List<EventType> types = handler.getWantedEventTypes() ;
		if( types.isEmpty() == true )
		{
			// Due to legacy we must assumme that a types size of 0, 
			// represents EventType.ALL, must be specially removed.
			allQueue.removeEventHandler( _handler ) ;
		}

		for( final EventType type : types )
		{
			final EventQueue queue = getEventQueue( type ) ;
			if( queue != null )
			{
				queue.removeEventHandler( _handler ) ;
			}
		}

		handlers.remove( _handler ) ;
		if( handler != null )
		{
			handler.reset() ;
		}
	}

	/**
		Return the Event Systems name, not considered unique.
	*/
	public String getName()
	{
		return name ;
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
		// allQueue receives all events so 
		// it is the only queue we need to check 
		// to see if there are events available to 
		// be processed.
		return allQueue.hasEvents() ;
	}

	private EventQueue getEventQueue( final EventType _type )
	{
		final int length = queues.size() ;
		for( int i = 0; i < length; ++i )
		{
			final EventQueue que = queues.get( i ) ;
			if( que.isType( _type ) == true )
			{
				return que ;
			}
		}

		return null ;
	}

	private final int exists( final IEventHandler _handler )
	{
		final int size = handlers.size() ;
		for( int i = 0; i < size; i++ )
		{
			final WeakReference ref = handlers.get( i ) ;
			if( ref.get() == _handler )
			{
				return i ;
			}
		}
		return -1 ;
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
