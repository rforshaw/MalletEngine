package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public abstract class InputComponent extends Component
									 implements InputHandler
{
	private Entity.ReadyCallback destroy = null ;
	protected final InputMode mode ;

	public InputComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public InputComponent( final Entity _parent, Entity.AllowEvents _allow )
	{
		this( _parent, _allow, InputMode.WORLD ) ;
	}

	public InputComponent( final Entity _parent, final InputMode _mode )
	{
		this( _parent, Entity.AllowEvents.YES, _mode ) ;
	}

	public InputComponent( final Entity _parent, Entity.AllowEvents _allow, final InputMode _mode )
	{
		super( _parent, _allow ) ;
		mode = _mode ;
	}

	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		destroy = _callback ;
		super.readyToDestroy( _callback ) ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		Event<InputHandler> event ;
		switch( mode )
		{
			case UI    : event = new Event<InputHandler>( "ADD_GAME_STATE_UI_INPUT", this ) ; break ;
			case WORLD : event = new Event<InputHandler>( "ADD_GAME_STATE_WORLD_INPUT", this ) ; break ;
			default    : return ;
		}
		_events.add( event ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		super.passFinalEvents( _events ) ;
		switch( mode )
		{
			case UI    : _events.add( new Event<InputHandler>( "REMOVE_GAME_STATE_UI_INPUT", this ) ) ;    break ;
			case WORLD : _events.add( new Event<InputHandler>( "REMOVE_GAME_STATE_WORLD_INPUT", this ) ) ; break ;
			default    : return ;
		}
	}

	/**
		Extend function if you wish to determine whether to 
		Consume or Propagate an Input Event.
		Consuming an InputEvent is benificial for UIs, is it will 
		prevent the InputEvent from being processed by other InputHandlers.
	*/
	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( destroy == null )
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

	@Override
	public void reset() {}

	public static enum InputMode
	{
		UI,
		WORLD
	}
}
