package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.entity.Entity ;
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
	protected final EventController stateController ;		// Used to talk to GameState
	protected final EventController backendController ;		// Used to talk to GLDefaultSystem

	public EventComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public EventComponent( final Entity _parent, final Entity.AllowEvents _allow )
	{
		this( _parent, _allow, 5, 5 ) ;
	}

	public EventComponent( final Entity _parent,
						   final Entity.AllowEvents _allow,
						   final int _stateCapacity,
						   final int _backendCapacity )
	{
		super( _parent, _allow ) ;
		stateController = createStateEventController() ;
		backendController = createBackendEventController() ;
	}

	/**
		Override to add Event Processors to the component's
		State Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public EventController createStateEventController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		return createController( _processors ) ;
	}

	/**
		Override to add Event Processors to the component's
		Backend Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public EventController createBackendEventController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		return createController( _processors ) ;
	}
	
	private static EventController createController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		if( _processors == null )
		{
			return new EventController() ;
		}

		if( _processors.length == 0 )
		{
			return new EventController() ;
		}

		return new EventController( _processors ) ;
	}

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
