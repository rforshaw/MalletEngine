package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public abstract class InputComponent extends Component implements InputHandler
{
	protected InputAdapterInterface inputAdapter = null ;
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

	public void passInputEvent( final InputEvent _event )
	{
		synchronized( inputs )
		{
			inputs.add( _event ) ;
		}
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