package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.io.save.Reference ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public abstract class InputComponent extends Component implements InputHandler
{
	protected @Reference InputAdapterInterface inputAdapter = null ;
	protected ArrayList<InputEvent> inputs = new ArrayList<InputEvent>() ;

	public InputComponent()
	{
		super( "INPUT", "INPUTCOMPONENT" ) ;
	}

	public InputComponent( final String _name )
	{
		super( _name, "INPUTCOMPONENT" ) ;
	}
	
	public InputComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		inputAdapter = _adapter ;
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		final Event<InputHandler> event = new Event<InputHandler>( "ADD_GAME_STATE_INPUT", this ) ;
		_events.add( event ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		final Event<InputHandler> event = new Event<InputHandler>( "REMOVE_GAME_STATE_INPUT", this ) ;
		_events.add( event ) ;
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
}