package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

/**
	Double buffer effectively, prevents events from being added to 
	an array that is being processed, reduces the chance of an infinite loop or crash.

	Use in order:
		refreshEvents() ;
		process Events
**/
public final class EventMessenger
{
	private List<Event<?>> newEvents = MalletList.<Event<?>>newList() ;
	private List<Event<?>> events = MalletList.<Event<?>>newList() ;

	public EventMessenger() {}
	
	public final void addEvent( final Event<?> _event )
	{
		newEvents.add( _event ) ;
	}

	public final Event<?> getAt( final int _index )
	{
		return events.get( _index ) ;
	}
	
	public final List<Event<?>> getEvents()
	{
		return events ;
	}
	
	public final int size()
	{
		return events.size() ;
	}
	
	public boolean hasEvents()
	{
		return ( events.isEmpty() == false || newEvents.isEmpty() == false ) ;
	}

	/**
		Swap the buffers and clear the old active buffer.
	**/
	public final void refreshEvents()
	{
		if( events.isEmpty() == false )
		{
			events.clear() ;
		}

		if( newEvents.isEmpty() == false )
		{
			final List<Event<?>> oldEvents = events ;
			events = newEvents ;
			newEvents = oldEvents ;
		}
	}

	public final void clearEvents()
	{
		newEvents.clear() ;
		events.clear() ;
	}
}
