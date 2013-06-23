package com.linxonline.mallet.event ;

public abstract class EventUpdater implements EventHandler
{
	private final EventMessenger messenger = new EventMessenger() ;

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
		messenger.addEvent( _event ) ;
	}
}