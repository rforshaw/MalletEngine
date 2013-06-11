package com.linxonline.mallet.entity ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.system.* ;

/**
	Allows a Component to send or recieve Events during runtime.
	
	An Event Component recieves Events from the Game State it resides
	within. By overriding processEvent() you can manipulate Events it 
	is registered to view.
	
	You can also use the EventComponent as a filter of sorts before injecting 
	the Game State events into the Entity's internal messaging system.
**/
public class EventComponent extends Component
{
	protected EventController eventController = new EventController() ;
	protected boolean sendToEntity = false ;			// Allows Events to be passed onto rest of Entity

	public EventComponent()
	{
		super( "EVENT", "EVENTCOMPONENT" ) ;
		initEventProcessor() ;
	}

	public EventComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
		initEventProcessor() ;
	}

	public void setSendToEntityComponents( final boolean _send )
	{
		sendToEntity = _send ;
	}

	public void update( final float _dt )
	{
		super.update( _dt ) ;
		eventController.update() ;
	}

	public EventController getEventController()
	{
		return eventController ;
	}
	
	protected void initEventProcessor()
	{
		eventController.addEventProcessor( new EventProcessor()
		{
			@Override
			public void processEvent( final Event _event )
			{
				if( sendToEntity == true )
				{
					// Sends the Event to the Entity's Message pool
					componentEvents.passEvent( _event ) ;
				}
			}
		} ) ;
	}

	/**
		Convienience method to EventController's passEvent.
	**/
	public void passEvent( final Event _event )
	{
		eventController.passEvent( _event ) ;
	}
}