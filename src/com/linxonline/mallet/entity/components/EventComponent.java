package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.save.Save ;

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
	protected @Save final EventController eventController = new EventController( id.toString() ) ;
	protected @Save boolean sendToEntity = false ;			// Allows Events to be passed onto rest of Entity

	public EventComponent()
	{
		super( "EVENT", "EVENTCOMPONENT" ) ;
		initEventProcessor() ;
	}

	public EventComponent( final String _name )
	{
		super( _name, "EVENTCOMPONENT" ) ;
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

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		final Event<EventController> event = new Event<EventController>( "ADD_GAME_STATE_EVENT", getEventController() ) ;
		_events.add( event ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		final Event<EventController> event = new Event<EventController>( "REMOVE_GAME_STATE_EVENT", getEventController() ) ;
		_events.add( event ) ;
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
		eventController.addEventProcessor( new EventProcessor( "ROUTER" )
		{
			@Override
			public void processEvent( final Event _event )
			{
				if( sendToEntity == true )
				{
					// Sends the Event to the Entity's Internal Message pool
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