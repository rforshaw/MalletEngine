package com.linxonline.mallet.event ;

import java.lang.ref.WeakReference ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

public class EventSystem implements IEventSystem
{
	private static final IEventFilter NO_FILTERING = new IEventFilter()
	{
		@Override
		public List<Event<?>> filter( final List<Event<?>> _events )
		{
			return _events ;
		}
	} ;
	
	private static final IIntercept FALLBACK_INTERCEPT = new IIntercept()
	{
		@Override
		public boolean allow( final Event<?> _event )
		{
			return true ;
		}
	} ;

	private final int eventCapacity ;
	private final SwapList<Event<?>> events ;
	private final EventType.Lookup<EventQueue> queues = new EventType.Lookup<EventQueue>() ;

	private IIntercept intercept = FALLBACK_INTERCEPT ;

	public EventSystem()
	{
		this( 10 ) ;
	}

	public EventSystem( int _eventCapacity )
	{
		eventCapacity = _eventCapacity ;
		events = new SwapList<Event<?>>( eventCapacity ) ;
	}

	@Override
	public void setIntercept( final IIntercept _intercept )
	{
		intercept = ( _intercept != null ) ? _intercept : FALLBACK_INTERCEPT ;
	}

	@Override
	public void addHandler( final IEventHandler _handler )
	{
		final WeakReference<IEventHandler> weakHandler = new WeakReference<IEventHandler>( _handler ) ;
		final List<EventType> types = _handler.getWantedEventTypes() ;
		for( final EventType type : types )
		{
			final EventQueue que = getQueueByType( type ) ;
			//System.out.println( "Add Handler" + _handler + " Type: " + type ) ;
			que.addHandler( weakHandler ) ;
		}
	}

	@Override
	public void removeHandler( final IEventHandler _handler )
	{
		final List<EventType> types = _handler.getWantedEventTypes() ;
		for( final EventType type : types )
		{
			final EventQueue que = getQueueByType( type ) ;
			que.removeHandler( _handler ) ;
		}
	}

	@Override
	public void setFilter( final EventType _type, final IEventFilter _filter )
	{
		final EventQueue que = getQueueByType( _type ) ;
		que.setFilter( _filter ) ;
	}

	@Override
	public void addEvent( final Event<?> _event )
	{
		if( intercept.allow( _event ) == true )
		{
			events.add( _event ) ;
		}
	}

	@Override
	public void addEvents( final List<Event<?>> _events )
	{
		events.addAll( _events ) ;
	}

	@Override
	public void sendEvents()
	{
		final List<Event<?>> toSend = events.swap() ;
		if( toSend.isEmpty() )
		{
			// There are no events to pass to handlers
			return ;
		}

		for( final Event<?> event : toSend )
		{
			final EventQueue que = getQueueByEvent( event ) ;
			que.addEvent( event ) ;
		}

		toSend.clear() ;

		for( final EventQueue que : queues )
		{
			que.sendEvents() ;
		}
	}

	@Override
	public boolean hasEvents()
	{
		return !events.isEmpty() ;
	}

	@Override
	public void reset()
	{
		queues.clear() ;
		events.clear() ;
	}

	private EventQueue getQueueByEvent( final Event<?> _event )
	{
		return getQueueByType( _event.getEventType() ) ;
	}

	private EventQueue getQueueByType( final EventType _type )
	{
		EventQueue que = queues.get( _type ) ;
		if( que == null )
		{
			que = new EventQueue( 10, eventCapacity, NO_FILTERING ) ;
			queues.add( _type, que ) ;
		}

		return que ;
	}

	private static class EventQueue
	{
		private final int handlerCapacity ;
		private final int eventCapacity ;

		private final List<WeakReference<IEventHandler>> handlers ;
		private final List<WeakReference<IEventHandler>> bufferedHandlers ;
		private List<Event<?>> events ;
		private IEventFilter filter ;

		public EventQueue( final int _handlerCapacity,
						   final int _eventCapacity,
						   final IEventFilter _filter )
		{
			handlerCapacity = _handlerCapacity ;
			eventCapacity = _eventCapacity ;

			handlers = MalletList.<WeakReference<IEventHandler>>newList( handlerCapacity ) ;
			bufferedHandlers = MalletList.<WeakReference<IEventHandler>>newList( handlerCapacity ) ;

			events = MalletList.<Event<?>>newList( eventCapacity ) ;
			filter = _filter ;
		}

		public void addHandler( final WeakReference<IEventHandler> _handler )
		{
			handlers.add( _handler ) ;
		}

		public void removeHandler( final IEventHandler _handler )
		{
			final int size = handlers.size() ;
			for( int i = 0; i < size; ++i )
			{
				final WeakReference weak = handlers.get( i ) ;
				if( _handler == weak.get() )
				{
					handlers.remove( i ) ;
					return ;
				}
			}
		}

		public void setFilter( final IEventFilter _filter )
		{
			filter = _filter ;
		}

		public void addEvent( final Event<?> _event )
		{
			events.add( _event ) ;
		}

		public void sendEvents()
		{
			if( events.isEmpty() )
			{
				// There are no events to pass to handlers
				return ;
			}

			final List<Event<?>> optimised = filter.filter( events ) ;

			bufferedHandlers.clear() ;
			bufferedHandlers.addAll( handlers ) ;

			final int handlerSize = bufferedHandlers.size() ;
			final int eventSize = optimised.size() ;
			for( int i = 0; i < handlerSize; ++i )
			{
				final WeakReference<IEventHandler> weak = bufferedHandlers.get( i ) ;
				final IEventHandler handler = weak.get() ;
				if( handler == null )
				{
					Logger.println( "Event handler destroyed without being removed from " + this, Logger.Verbosity.MAJOR ) ;
					continue ;
				}

				for( int j = 0; j < eventSize; j++ )
				{
					final Event<?> event = optimised.get( j ) ;
					handler.processEvent( event ) ;
				}
			}

			if( events.size() > eventCapacity )
			{
				events = MalletList.<Event<?>>newList( eventCapacity ) ;
			}

			events.clear() ;
		}
	}
}
