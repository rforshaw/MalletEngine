package com.linxonline.mallet.event ;

import java.util.List ;

public interface IEventHandler
{
	public void processEvent( final Event<?> _event ) ;

	/**
		Pass an Event to the Event System
	*/
	public void passEvent( final Event<?> _event ) ;

	/**
		Populate the passed in array with the even-types
		the handler is interested in.
	*/
	public List<EventType> getWantedEventTypes( final List<EventType> _fill ) ;
}
