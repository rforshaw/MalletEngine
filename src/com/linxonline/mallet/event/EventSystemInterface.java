package com.linxonline.mallet.event ;

public interface EventSystemInterface extends AddEventInterface
{
	/**
		Add EventHandler to handlers list & search through 
		the EventHandlers EventTypes, placing it in the correct 
		lists, so Events can be filtered correctly.
	**/
	public void addEventHandler( final EventHandler _handler ) ;

	/**
		Removes the EventHandler in the next update().
	**/
	public void removeEventHandler( final EventHandler _handler ) ;

	public void addEventFilter( final EventType _type, final EventFilter _filter ) ;
	public void removeEventFilter( final EventType _type, final EventFilter _filter ) ;
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

	public int getEventSize() ;
	public int getHandlerSize() ;

	public void clearHandlers() ;

	public void clearEvents() ;
	public boolean hasEvents() ;
}
