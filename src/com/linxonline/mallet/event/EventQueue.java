package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public class EventQueue
{
	private final EventType name ;
	private final List<IEventHandler> handlers = MalletList.<IEventHandler>newList();
	private final List<IEventFilter> filters = MalletList.<IEventFilter>newList() ;
	private final List<Event<?>> optimisedEvents = MalletList.<Event<?>>newList() ;
	private final EventMessenger messenger = new EventMessenger() ;

	public EventQueue( final EventType _name )
	{
		name = _name ;
	}
	
	public void addEventHandler( final IEventHandler _handler )
	{
		handlers.add( _handler ) ;
	}

	public void removeEventHandler( final IEventHandler _handler )
	{
		handlers.remove( _handler ) ;
	}

	public void addEventFilter( final IEventFilter _filter )
	{
		filters.add( _filter ) ;
	}

	public void removeEventFilter( final IEventFilter _filter )
	{
		filters.remove( _filter ) ;
	}

	public void addEvent( final Event<?> _event )
	{
		messenger.addEvent( _event ) ;
	}

	public void update()
	{
		messenger.refreshEvents() ;
		final int filterSize = filters.size() ;
		if( filterSize > 0 )
		{
			for( int i = 0; i < filterSize; ++i )
			{
				filters.get( i ).filter( messenger, optimisedEvents ) ;
			}
		}
		else
		{
			final List<Event<?>> events = messenger.getEvents() ;
			final int size = events.size() ;
			if( size > 0 )
			{
				optimisedEvents.addAll( events ) ;
			}
		}

		final int handlerSize = handlers.size() ;
		while( optimisedEvents.size() > 0 )				// Reduce chance of sending the same event twice.
		{
			final Event<?> event = optimisedEvents.get( 0 ) ;
			optimisedEvents.remove( 0 ) ;
			for( int j = 0; j < handlerSize; ++j )
			{
				handlers.get( j ).processEvent( event ) ;
			}
		}
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
		messenger.clearEvents() ;
		optimisedEvents.clear() ;
	}
	
	public boolean hasEvents()
	{
		return messenger.hasEvents() ;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer() ;
		buffer.append( "[ Event Queue: " + name + ", " ) ;
		for( final IEventHandler handler : handlers )
		{
			buffer.append( handler.getName() + ", " ) ;
		}
		buffer.append( "]" ) ;
		return buffer.toString() ;
	}
}
