package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public abstract class InputComponent extends Component
									 implements InputHandler
{
	protected InputAdapterInterface inputAdapter = null ;
	protected final InputMode mode ;
	protected ArrayList<InputEvent> inputs = new ArrayList<InputEvent>() ;

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

	public InputComponent( final String _name, InputMode _mode )
	{
		this( _name, "INPUTCOMPONENT", _mode ) ;
	}

	public InputComponent( final String _name, final String _group, InputMode _mode )
	{
		super( _name, _group ) ;
		mode = _mode ;
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		inputAdapter = _adapter ;
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
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
	public void passFinalEvents( final ArrayList<Event<?>> _events )
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
		inputs.add( _event ) ;
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		final int length = inputs.size() ;
		for( int i = 0; i < length; ++i )
		{
			processInputEvent( inputs.get( i ) ) ;
		}
		inputs.clear() ;
	}

	protected void processInputEvent( final InputEvent _input ) {}

	@Override
	public void reset()
	{
		inputAdapter = null ;
		inputs.clear() ;
	}

	public static enum InputMode
	{
		UI,
		WORLD
	}
}