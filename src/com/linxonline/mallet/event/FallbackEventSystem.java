package com.linxonline.mallet.event ;

public class FallbackEventSystem implements IEventSystem
{
	@Override
	public void addEventHandler( final IEventHandler _handler ) {}

	@Override
	public void removeEventHandler( final IEventHandler _handler ) {}

	@Override
	public void setEventFilter( final EventType _type, final IEventFilter _filter ) {}

	@Override
	public void removeHandlersNow() {}

	@Override
	public void addEvent( final Event<?> _event ) {}

	@Override
	public void update() {}

	@Override
	public String getName()
	{
		return "FALLBACK_EVENT_SYSTEM" ;
	}

	@Override
	public void clearHandlers() {}

	@Override
	public void clearEvents() {}

	@Override
	public boolean hasEvents()
	{
		return false ;
	}
}
