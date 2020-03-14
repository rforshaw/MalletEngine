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
	private final int capacity ;
	private final EventType.Lookup<EventQueue> queues = new EventType.Lookup<EventQueue>() ;

	private final List<WeakReference<IEventHandler>> handlers ;
	private List<WeakReference<IEventHandler>> toBeAdded ;
	private List<WeakReference<IEventHandler>> toBeRemoved ;

	public EventSystem()
	{
		this( null ) ;
	}

	public EventSystem( final String _name )
	{
		this( _name, 100 ) ;
	}

	/**
		Give the EventSystem a name and an initial capacity.
		This sets the expected number of handlers the system 
		intends to service.
	*/
	public EventSystem( final String _name, final int _handlerCapacity )
	{
		name = ( _name != null ) ? _name : "NONE" ;
		capacity = _handlerCapacity ;

		handlers = MalletList.<WeakReference<IEventHandler>>newList( capacity ) ;
		toBeAdded = MalletList.<WeakReference<IEventHandler>>newList( capacity ) ;
		toBeRemoved = MalletList.<WeakReference<IEventHandler>>newList( capacity ) ;
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
			Logger.println( _handler + "already exists within " + name, Logger.Verbosity.MAJOR ) ;
			return ;
		}

		final WeakReference<IEventHandler> weakHandler = new WeakReference<IEventHandler>( _handler ) ;
		toBeAdded.add( weakHandler ) ;
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

	public void addHandlersNow()
	{
		final int size = toBeAdded.size() ;
		for( int i = 0; i < size; ++i )
		{
			final WeakReference<IEventHandler> weakHandler = toBeAdded.get( i ) ;
			final IEventHandler handler = weakHandler.get() ;
			if( handler == null )
			{
				// As quickly as it was added, it was destroyed.
				continue ;
			}

			handlers.add( weakHandler ) ;

			final List<EventType> types = handler.getWantedEventTypes() ;
			for( final EventType type : types )
			{
				final EventQueue que = getEventQueue( type ) ;
				que.addEventHandler( weakHandler ) ;
			}
		}
		toBeAdded.clear() ;
		
		if( size > capacity )
		{
			toBeAdded = MalletList.<WeakReference<IEventHandler>>newList( capacity ) ;
		}
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
		
		if( size > capacity )
		{
			toBeRemoved = MalletList.<WeakReference<IEventHandler>>newList( capacity ) ;
		}
	}

	@Override
	public final void addEvent( final Event<?> _event )
	{
		// We don't want to add an event flagged ALL
		// multiple times into the same queue.
		final EventQueue queue = getEventQueue( _event.getEventType() ) ;
		queue.addEvent( _event ) ;
	}

	@Override
	public final void update()
	{
		addHandlersNow() ;
		removeHandlersNow() ;

		for( final EventQueue que : queues )
		{
			que.update() ;
		}
	}
	
	private void remove( final WeakReference<IEventHandler> _handler )
	{
		final IEventHandler handler = _handler.get() ;
		final List<EventType> types = handler.getWantedEventTypes() ;

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

		for( final EventQueue que : queues )
		{
			que.clearHandlers() ;
		}
	}

	@Override
	public final void clearEvents()
	{
		for( final EventQueue que : queues )
		{
			que.clearEvents() ;
		}
	}

	@Override
	public final boolean hasEvents()
	{
		for( final EventQueue que : queues )
		{
			if( que.hasEvents() == true )
			{
				return true ;
			}
		}
		return false ;
	}

	private EventQueue getEventQueue( final EventType _type )
	{
		EventQueue que = queues.get( _type ) ;
		if( que == null )
		{
			que = new EventQueue( _type, capacity ) ;
			queues.add( _type, que ) ;
		}

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
		buffer.append( "[Event System: " ) ;
		buffer.append( getName() ) ;
		buffer.append( "]" ) ;
		return buffer.toString() ;
	}

	private class EventQueue
	{
		private final int capacity ;
		private final EventType type ;
		private final List<WeakReference<IEventHandler>> handlers ;
		private List<Event<?>> optimisedEvents = MalletList.<Event<?>>newList() ;
		private final SwapList<Event<?>> messenger = new SwapList<Event<?>>() ;

		private IEventFilter filter = NO_FILTERING ;

		public EventQueue( final EventType _type, final int _handlerCapacity )
		{
			this( _type, _handlerCapacity, 10 ) ;
		}

		public EventQueue( final EventType _type, final int _handlerCapacity, final int _messengerCapacity )
		{
			capacity = _messengerCapacity ;
			type = _type ;

			handlers = MalletList.<WeakReference<IEventHandler>>newList( _handlerCapacity ) ;
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
			final List<Event<?>> events = messenger.swap() ;
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
			if( eventSize > capacity )
			{
				optimisedEvents = MalletList.<Event<?>>newList( capacity ) ;
			}
		}

		public boolean isType( final EventType _type )
		{
			return ( _type != null && _type == type ) ;
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
				buffer.append( handler + ", " ) ;
			}
			buffer.append( "]" ) ;
			return buffer.toString() ;
		}
	}
}
