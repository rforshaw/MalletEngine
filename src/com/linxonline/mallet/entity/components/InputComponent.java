package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public abstract class InputComponent extends Component
									 implements InputHandler
{
	private Component.ReadyCallback destroy = null ;
	protected InputAdapterInterface inputAdapter = null ;
	protected final InputMode mode ;

	public InputComponent()
	{
		this( "INPUT", "INPUTCOMPONENT", InputMode.WORLD ) ;
	}

	public InputComponent( final String _name )
	{
		this( _name, "INPUTCOMPONENT", InputMode.WORLD ) ;
	}

	public InputComponent( final String _name, final String _group )
	{
		this( _name, _group, InputMode.WORLD ) ;
	}

	public InputComponent( final String _name, final InputMode _mode )
	{
		this( _name, "INPUTCOMPONENT", _mode ) ;
	}

	public InputComponent( final String _name, final String _group, final InputMode _mode )
	{
		super( _name, _group ) ;
		mode = _mode ;
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		inputAdapter = _adapter ;
	}

	public void readyToDestroy( final Component.ReadyCallback _callback )
	{
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
	public void reset()
	{
		inputAdapter = null ;
	}

	public static enum InputMode
	{
		UI,
		WORLD
	}
}
