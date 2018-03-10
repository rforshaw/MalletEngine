package com.linxonline.mallet.event ;

import java.lang.ref.WeakReference ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

public final class EventSystem implements IEventSystem
{
	/**
		If the EventQueue does not have an event filter it 
		will fallback to no filtering.
	*/
	private static final IEventFilter NO_FILTERING = new IEventFilter()
	{
		public List<Event<?>> filter( final List<Event<?>> _events, List<Event<?>> _populate )
		{
			_populate.addAll( _events ) ;
			return _populate ;
		}
	} ;

	private final String name ;
	private final List<EventQueue> queues = MalletList.<EventQueue>newList() ;
	private final EventQueue allQueue ;

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
		allQueue = getEventQueue( EventType.ALL ) ;
	}

	/**
		Add EventHandler to handlers list & search through 
		the EventHandlers EventTypes, placing it in the correct 
		lists, so Events can be filtered correctly.

		EventHandler is stored as a WeakReference make sure to remove 
		it from the system before destroying the handler.
	**/
	@Override
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

		final int size = types.size() ;
		for( int i = 0; i < size; i++ )
		{
			final EventType type = types.get( i ) ;
			final EventQueue que = getEventQueue( type ) ;
			que.addEventHandler( weakHandler ) ;
		}
	}

	/**
		Removes the EventHandler in the next update().
	**/
	@Override
	public final void removeEventHandler( final IEventHandler _handler )
	{
		final int index = exists( _handler ) ;
		if( index >= 0 )
		{
			toBeRemoved.add( handlers.get( index ) ) ;
		}
	}

	@Override
	public final void setEventFilter( final EventType _type, final IEventFilter _filter )
	{
		final EventQueue que = getEventQueue( _type ) ;
		que.setEventFilter( _filter ) ;
	}

	/**
		Remove the EventHandlers queued for removal now.
	**/
	@Override
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
		// We don't want to add an event flagged ALL
		// multiple times into the same queue.
		final EventQueue queue = getEventQueue( _event.getEventType() ) ;
		if( queue != allQueue )
		{
			queue.addEvent( _event ) ;
		}

		// We always want to pass the Event to ALL
		// queue irrespective of its EventType.
		allQueue.addEvent( _event ) ;
	}

	@Override
	public final void update()
	{
		removeHandlersNow() ;
		final int size = queues.size() ;
		for( int i = 0; i < size; ++i )
		{
			queues.get( i ).update() ;
		}
	}
	
	private void remove( final WeakReference<IEventHandler> _handler )
	{
		final IEventHandler handler = _handler.get() ;
		final List<EventType> types = handler.getWantedEventTypes() ;
		if( types.isEmpty() )
		{
			// Due to legacy we must assume that a types size of 0, 
			// represents EventType.ALL, must be specially removed.
			allQueue.removeEventHandler( _handler ) ;
			return ;
		}

		final int size = types.size() ;
		for( int i = 0; i < size; i++ )
		{
			final EventQueue queue = getEventQueue( types.get( i ) ) ;
			queue.removeEventHandler( _handler ) ;
		}

		handlers.remove( _handler ) ;
		handler.reset() ;
	}

	/**
		Return the Event Systems name, not considered unique.
	*/
	@Override
	public String getName()
	{
		return name ;
	}

	@Override
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

	@Override
	public final void clearEvents()
	{
		final int size = queues.size() ;
		for( int i = 0; i < size; ++i )
		{
			queues.get( i ).clearEvents() ;
		}
	}

	@Override
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

		final EventQueue que = new EventQueue( _type ) ;
		queues.add( que ) ;

		return que ;
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

	private class EventQueue
	{
		private final EventType name ;
		private final List<WeakReference<IEventHandler>> handlers = MalletList.<WeakReference<IEventHandler>>newList();
		private final List<Event<?>> optimisedEvents = MalletList.<Event<?>>newList() ;
		private final SwapList<Event<?>> messenger = new SwapList<Event<?>>() ;

		private IEventFilter filter = NO_FILTERING ;

		public EventQueue( final EventType _name )
		{
			name = _name ;
		}
		
		public void addEventHandler( final WeakReference<IEventHandler> _handler )
		{
			handlers.add( _handler ) ;
		}

		public void removeEventHandler( final WeakReference<IEventHandler> _handler )
		{
			handlers.remove( _handler ) ;
		}

		public void setEventFilter( final IEventFilter _filter )
		{
			filter = ( _filter != null ) ? _filter : NO_FILTERING ;
		}

		public void addEvent( final Event<?> _event )
		{
			messenger.add( _event ) ;
		}

		public void update()
		{
			messenger.swap() ;
			final List<Event<?>> events = messenger.getActiveList() ;
			if( events.isEmpty() )
			{
				// There are no events to pass to handlers
				return ;
			}

			filter.filter( events, optimisedEvents ) ;

			final int handlerSize = handlers.size() ;
			final int eventSize = optimisedEvents.size() ;
			for( int i = 0; i < handlerSize; ++i )
			{
				final WeakReference<IEventHandler> weak = handlers.get( i ) ;
				final IEventHandler handler = weak.get() ;
				if( handler == null )
				{
					Logger.println( "Event handler destroyed without being removed from " + EventSystem.this.getName(), Logger.Verbosity.MAJOR ) ;
					continue ;
				}

				for( int j = 0; j < eventSize; j++ )
				{
					final Event<?> event = optimisedEvents.get( j ) ;
					handler.processEvent( event ) ;
				}
			}

			optimisedEvents.clear() ;
		}

		public boolean isType( final EventType _type )
		{
			return ( _type != null && _type == name ) ;
		}

		public int size()
		{
			return messenger.size() ;
		}
		
		public void clearHandlers()
		{
			handlers.clear() ;
		}
		
		public void clearEvents()
		{
			messenger.clear() ;
			optimisedEvents.clear() ;
		}
		
		public boolean hasEvents()
		{
			return messenger.isEmpty() == false ;
		}

		public String toString()
		{
			final StringBuffer buffer = new StringBuffer() ;
			buffer.append( "[ Event Queue: " + name + ", " ) ;
			for( final WeakReference<IEventHandler> weakHandler : handlers )
			{
				final IEventHandler handler = weakHandler.get() ;
				buffer.append( handler.getName() + ", " ) ;
			}
			buffer.append( "]" ) ;
			return buffer.toString() ;
		}
	}
}
