package com.linxonline.mallet.event ;

import java.util.List ;

public interface IEventSystem extends IAddEvent
{
	public void addHandler( final IEventHandler _handler ) ;
	public void removeHandler( final IEventHandler _handler ) ;

	public void setFilter( final EventType _type, final IEventFilter _filter ) ;

	public void addEvent( final Event<?> _event ) ;
	public void addEvents( final List<Event<?>> _events ) ;

	public void sendEvents() ;

	public boolean hasEvents() ;
	public void reset() ;
}
