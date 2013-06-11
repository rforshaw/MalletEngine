package com.linxonline.mallet.event ;

import java.io.Serializable ;
import java.util.ArrayList ;

/*====================================================*/
// Used to prevent new events tampering with old events
// while they are being processed. Entity uses this for 
// internal messaging.
// Use in order:
// refreshEvents()
// process Events
/*====================================================*/
public final class EventMessenger implements Serializable
{
	private ArrayList<Event> newEvents = new ArrayList<Event>() ;
	private ArrayList<Event> events = new ArrayList<Event>() ;

	public EventMessenger() {}
	
	public final void addEvent( final Event _event )
	{
		newEvents.add( _event ) ;
	}

	public final Event getAt( final int _index )
	{
		return events.get( _index ) ;
	}
	
	public final ArrayList<Event> getEvents()
	{
		return events ;
	}
	
	public final int size()
	{
		return events.size() ;
	}
	
	public boolean hasEvents()
	{
		return ( events.size() > 0 ) || ( newEvents.size() > 0 ) ? true : false ;
	}
	
	public final void refreshEvents()
	{
		final ArrayList<Event> pointer = events ;

		events = newEvents ;
		newEvents = pointer ;
		newEvents.clear() ;
	}

	public final void clearEvents()
	{
		newEvents.clear() ;
		events.clear() ;
	}
}