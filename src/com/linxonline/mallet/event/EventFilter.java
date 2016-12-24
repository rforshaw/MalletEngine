package com.linxonline.mallet.event ;

import java.util.List ;

public interface EventFilter
{
	/**
		Filter the events into an optimised array.
		
		returns optimised array, _populate if defined should be filled with optimised events,
		else create a new array and return that.
	**/
	public List<Event<?>> filter( final EventMessenger _messenger, List<Event<?>> _populate ) ;
}
