package com.linxonline.mallet.event ;

import java.util.List ;

import com.linxonline.mallet.util.Logger ;

/**
	Use NullEventController.FALLBACK when you don't want null pointers
	for your IEventController variables.
*/
public final class NullEventController implements IEventController
{
	public final static NullEventController FALLBACK = new NullEventController() ;

	@Override
	public void processEvent( final Event<?> _event ) {}

	@Override
	public void passEvent( final Event<?> _event )
	{
		Logger.println( "Attempting to call event-controller without it being initialised.", Logger.Verbosity.MAJOR ) ;
	}

	@Override
	public List<EventType> getWantedEventTypes( final List<EventType> _fill )
	{
		return _fill ;
	}

	@Override
	public void setAddEventInterface( final IAddEvent _addInterface ) {}

	@Override
	public void update() {}

	@Override
	public void clearEvents() {}
}
