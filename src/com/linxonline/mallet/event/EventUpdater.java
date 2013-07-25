package com.linxonline.mallet.event ;

/**
	Used by the Audio, Renderer & Animation systems.
	Deals with the same Event updating code that they all need.
*/
public abstract class EventUpdater implements EventHandler
{
	private final EventMessenger messenger = new EventMessenger() ;
	protected AddEventInterface eventSystem = null ;					// Used to pass Events to designated EventSystem.

	/**
		Process the passed in event
	*/
	protected abstract void useEvent( final Event _event ) ;

	protected void updateEvents()
	{
		messenger.refreshEvents() ;
		final int eventSize = messenger.size() ;

		for( int i = 0; i < eventSize; ++i )
		{
			useEvent( messenger.getAt( i ) ) ;
		}
	}

	@Override
	public void processEvent( final Event _event )
	{
		// Add events recieved to the messenger for later processing
		messenger.addEvent( _event ) ;
	}
	
	@Override
	public void passEvent( final Event _event )
	{
		// If we are passing an Event, we'll want to know if the EventSystem is null,
		// So crash!
		eventSystem.addEvent( _event ) ;
	}
}