package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.event.* ;

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
	protected final EventController stateController = new EventController( id.toString() ) ;		// Used to talk to GameState
	protected final EventController backendController = new EventController( id.toString() ) ;		// Used to talk to GLDefaultSystem

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
		initBackendEventProcessors( getBackendEventController() ) ;
		initStateEventProcessors( getStateEventController() ) ;
	}

	/**
		Override to add Event Processors to the component's
		Backend Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public void initBackendEventProcessors( final EventController _controller ) {}

	/**
		Override to add Event Processors to the component's
		State Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public void initStateEventProcessors( final EventController _controller ) {}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		_events.add( new Event<EventController>( "ADD_BACKEND_EVENT", getBackendEventController() ) ) ;
		_events.add( new Event<EventController>( "ADD_GAME_STATE_EVENT", getStateEventController() ) ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( new Event<EventController>( "REMOVE_BACKEND_EVENT", getBackendEventController() )  ) ;
		_events.add( new Event<EventController>( "REMOVE_GAME_STATE_EVENT", getStateEventController() )  ) ;
	}

	public void update( final float _dt )
	{
		super.update( _dt ) ;
		backendController.update() ;
		stateController.update() ;
	}

	public EventController getBackendEventController()
	{
		return backendController ;
	}

	public EventController getStateEventController()
	{
		return stateController ;
	}

	/**
		Convienience method to EventController's passEvent.
	**/
	public void passBackendEvent( final Event _event )
	{
		backendController.passEvent( _event ) ;
	}

	/**
		Convienience method to EventController's passEvent.
	**/
	public void passStateEvent( final Event _event )
	{
		stateController.passEvent( _event ) ;
	}
}
