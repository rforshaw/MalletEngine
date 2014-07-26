package com.linxonline.mallet.event ;

import java.util.ArrayList ;

public interface EventFilter
{
	/**
		Filter the events into an optimised array.
		
		returns optimised array, _populate if defined should be filled with optimised events,
		else create a new array and return that.
	**/
	public ArrayList<Event> filter( final EventMessenger _messenger, ArrayList<Event<?>> _populate ) ;
}