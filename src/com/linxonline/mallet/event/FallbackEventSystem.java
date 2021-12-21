package com.linxonline.mallet.event ;

import java.util.List ;

public class FallbackEventSystem implements IEventSystem
{
	@Override
	public void setIntercept( final IIntercept _intercept ) {}

	@Override
	public void addHandler( final IEventHandler _handler ) {}

	@Override
	public void removeHandler( final IEventHandler _handler ) {}

	@Override
	public void setFilter( final EventType _type, final IEventFilter _filter ) {}

	@Override
	public void addEvent( final Event<?> _event ) {}

	@Override
	public void addEvents( final List<Event<?>> _events ) {}

	@Override
	public void sendEvents() {}

	@Override
	public boolean hasEvents()
	{
		return false ;
	}

	@Override
	public void reset() {}
}
