package com.linxonline.mallet.input ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;

/*==============================================================*/
// InputState is used to create a Heirachical structure of      //
// inputs.														//
// It is typically added to the root Input System and 			//
// Input Handlers are added to the InputState instead of the 	//
// Input System.												//
// Thise enables fine control over large groups of Input 		//
// Handlers														//
/*==============================================================*/

public class InputState implements InputSystemInterface,
								   InputHandler
{
	// Prevents InputState from hording events, if it 
	// isn't removed from InputSystem
	private static final int MAX_QUEUE_THRESHOLD = 40 ;

	private InputAdapterInterface inputAdapter = null ;
	private ArrayList<InputHandler> handlers = new ArrayList<InputHandler>() ;

	private boolean hasInputs = false ;
	private InputHandler handler = null ;

	public InputState() {}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		inputAdapter = _adapter ;
	}

	@Override
	public final void addInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			return ;
		}

		_handler.setInputAdapterInterface( inputAdapter ) ;
		handlers.add( _handler ) ;
	}

	@Override
	public final void removeInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		_handler.setInputAdapterInterface( null ) ;
		handlers.remove( _handler ) ;
	}

	@Override
	public final InputEvent.Action passInputEvent( final InputEvent _event )
	{
		hasInputs = true ;
		final int handlerSize = handlers.size() ;
		for( int j = 0; j < handlerSize; ++j )
		{
			handler = handlers.get( j ) ;
			switch( handler.passInputEvent( _event ) )
			{
				case PROPAGATE : continue ;
				case CONSUME   : return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public final void update()
	{
		// InputState use to retain a collection of inputs
		// now inputs are directly transfered to InputHandlers.
		// When update is called we reset hasInputs as the 
		// InputHandlers will be processing the inputs soon.
		hasInputs = false ;
	}

	public final boolean hasInputs()
	{
		return hasInputs ;
	}

	@Override
	public final void clearHandlers()
	{
		handlers.clear() ;
	}

	@Override
	public final void clearInputs()
	{
		// Input State does not retain inputs anymore.
	}

	private final boolean exists( InputHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}
}
