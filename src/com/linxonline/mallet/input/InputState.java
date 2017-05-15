package com.linxonline.mallet.input ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
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

public class InputState implements IInputSystem,
								   InputHandler
{
	// Prevents InputState from hording events, if it 
	// isn't removed from InputSystem
	private static final int MAX_QUEUE_THRESHOLD = 40 ;

	private final List<InputHandler> handlers = MalletList.<InputHandler>newList() ;

	private boolean hasInputs = false ;
	private InputHandler handler = null ;

	public InputState() {}

	@Override
	public final void addInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			System.out.println( "Input Handler already exists.." ) ;
			return ;
		}

		handlers.add( _handler ) ;
	}

	@Override
	public final void removeInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			System.out.println( "Input Handler doesn't exist.." ) ;
			return ;
		}

		handlers.remove( _handler ) ;
		_handler.reset() ;
	}

	@Override
	public final InputEvent.Action passInputEvent( final InputEvent _event )
	{
		hasInputs = true ;
		final int handlerSize = handlers.size() ;
		for( int j = 0; j < handlerSize; ++j )
		{
			handler = handlers.get( j ) ;
			if( handler.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
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
	public void reset()
	{
		clearInputs() ;
		// We don't want to re-add InputHandlers when a 
		// State transition happens.
		//clearHandlers() ;

		hasInputs = false ;
		handler = null ;
	}

	/**
		Remove the Input Handlers and reset them.
	*/
	@Override
	public final void clearHandlers()
	{
		final int size = handlers.size() ;
		for( int i = 0; i < size; ++i )
		{
			handlers.get( i ).reset() ;
		}
		handlers.clear() ;
	}

	@Override
	public final void clearInputs()
	{
		// Input State does not retain inputs anymore.
	}

	private final boolean exists( final InputHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}
}
