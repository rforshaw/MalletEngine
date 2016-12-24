package com.linxonline.mallet.event ;

import java.util.List ;

public interface EventHandler extends EventHandlerMeta
{
	public void processEvent( final Event<?> _event ) ;

	/**
		Pass an Event to the Event System
	*/
	public void passEvent( final Event<?> _event ) ;

	/**
		Allow the implementation to clear any references 
		it may hold.
		This function is called when the EventHandler is 
		removed from an Event System.
	*/
	public void reset() ;

	/**
		Informs the Event System what Event Types
		this handler wants passed to it.
	*/
	public List<EventType> getWantedEventTypes() ;
}
