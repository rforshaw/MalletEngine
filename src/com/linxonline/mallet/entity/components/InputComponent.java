package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public abstract class InputComponent extends Component
									 implements IInputHandler
{
	private Entity.ReadyCallback destroy = null ;
	private boolean acceptInputs = true ;

	public InputComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public InputComponent( final Entity _parent, Entity.AllowEvents _allow )
	{
		super( _parent, _allow ) ;
	}

	public void acceptInputs( final boolean _accept )
	{
		acceptInputs = _accept ;
	}

	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		destroy = _callback ;
		super.readyToDestroy( _callback ) ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		super.passInitialEvents( _events ) ;
		_events.add( Event.<IInputHandler>create( "ADD_GAME_STATE_INPUT", this ) ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		_events.add( Event.<IInputHandler>create( "REMOVE_GAME_STATE_INPUT", this ) ) ;
	}

	/**
		Extend function if you wish to determine whether to 
		Consume or Propagate an Input Event.
		Consuming an InputEvent is benificial for UIs, is it will 
		prevent the InputEvent from being processed by other IInputHandlers.
	*/
	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( destroy == null && acceptInputs == true )
		{
			processInputEvent( _event ) ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Extend function to implement custom input logic.
		Ensure logic is not CPU intensive.
		Input events are processed every render call, to ensure 
		visual responsiveness to user demands.
	*/
	protected void processInputEvent( final InputEvent _input ) {}
}
