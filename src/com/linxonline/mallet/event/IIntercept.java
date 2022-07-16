package com.linxonline.mallet.event ;

/**
	Used by the event-system to determine if an event
	should be propogated to all event-type listeners.
*/
public interface IIntercept
{
	public boolean allow( final Event<?> _event ) ;
}
