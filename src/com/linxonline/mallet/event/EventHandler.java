package com.linxonline.mallet.event ;

import java.util.ArrayList ;

public interface EventHandler extends EventHandlerMeta
{
	public void processEvent( final Event<?> _event ) ;

	/**
		Pass an Event to the Event System
	*/
	public void passEvent( final Event<?> _event ) ;

	/**
		Informs the Event System what Event Types
		this handler wants passed to it.
	*/
	public ArrayList<EventType> getWantedEventTypes() ;
}