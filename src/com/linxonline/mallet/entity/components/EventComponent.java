package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

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
	protected final EventController eventController = new EventController( id.toString() ) ;

	public EventComponent()
	{
		this( "EVENT", "EVENTCOMPONENT" ) ;
	}

	public EventComponent( final String _name )
	{
		this( _name, "EVENTCOMPONENT" ) ;
	}

	public EventComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
		initStateEventProcessors( getEventController() ) ;
	}

	/**
		Override to add Event Processors to the component's
		State Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public void initStateEventProcessors( final EventController _controller ) {}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		final Event<EventController> event = new Event<EventController>( "ADD_GAME_STATE_EVENT", getEventController() ) ;
		_events.add( event ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( new Event<EventController>( "REMOVE_GAME_STATE_EVENT", getEventController() )  ) ;
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

	/**
		Convienience method to EventController's passEvent.
	**/
	public void passEvent( final Event _event )
	{
		eventController.passEvent( _event ) ;
	}
}
