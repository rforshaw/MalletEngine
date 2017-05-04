package com.linxonline.mallet.event ;

public interface IEventSystem extends IAddEvent
{
	/**
		Add EventHandler to handlers list & search through 
		the EventHandlers EventTypes, placing it in the correct 
		lists, so Events can be filtered correctly.
	**/
	public void addEventHandler( final IEventHandler _handler ) ;

	/**
		Removes the EventHandler in the next update().
	**/
	public void removeEventHandler( final IEventHandler _handler ) ;

	public void addEventFilter( final EventType _type, final IEventFilter _filter ) ;
	public void removeEventFilter( final EventType _type, final IEventFilter _filter ) ;
	/**
		Remove the EventHandlers queued for removal now.
	**/
	public void removeHandlersNow() ;

	public void addEvent( final Event<?> _event ) ;
	public void update() ;

	/**
		Return the Event Systems name, not considered unique.
	*/
	public String getName() ;

	public void clearHandlers() ;

	public void clearEvents() ;
	public boolean hasEvents() ;
}
