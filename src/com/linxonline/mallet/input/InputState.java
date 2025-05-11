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
								   IInputHandler
{
	private final List<IInputHandler> handlers = MalletList.<IInputHandler>newList() ;

	private boolean hasInputs = false ;
	private IInputHandler handler = null ;

	public InputState() {}

	@Override
	public final void addInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			System.out.println( "Input Handler already exists.." ) ;
			return ;
		}

		handlers.add( _handler ) ;
	}

	@Override
	public final void removeInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			System.out.println( "Input Handler doesn't exist.." + _handler.toString() ) ;
			return ;
		}

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
		// now inputs are directly transferred to IInputHandlers.
		// When update is called we reset hasInputs as the 
		// IInputHandlers will be processing the inputs soon.
		hasInputs = false ;
	}

	public final boolean hasInputs()
	{
		return hasInputs ;
	}

	/**
		Remove the Input Handlers and reset them.
	*/
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

	private final boolean exists( final IInputHandler _handler )
	{
		return handlers.contains( _handler ) ;
	}
}
