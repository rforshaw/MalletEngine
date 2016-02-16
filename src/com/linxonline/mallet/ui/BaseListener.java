package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.* ;

public abstract class BaseListener
{
	private EventController controller ;

	public void setEventController( final EventController _controller )
	{
		controller = _controller ;
	}

	public void sendEvent( final Event<?> _event )
	{
		if( controller != null )
		{
			controller.passEvent( _event ) ;
		}
	}
}
