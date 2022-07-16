package com.linxonline.mallet.event ;

/**
	See EventController for an implementation of IEventController.
	
*/
public interface IEventController extends IEventHandler
{
	/**
		Called by the GameState before being added to the
		event-system, it allows the controller to send events.
	*/
	public void setAddEventInterface( final IAddEvent _addInterface ) ;

	/**
		Called to process the events it's accumulated from
		the event-system.
	*/
	public void update() ;

	public void clearEvents() ;
}
