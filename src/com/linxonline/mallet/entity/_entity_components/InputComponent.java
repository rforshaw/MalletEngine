package com.linxonline.mallet.entity ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;

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