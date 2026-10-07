package com.linxonline.mallet.input ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.time.ElapsedTimer ;

/*==============================================================*/
// InputState is used to create a hierarchical structure of     //
// inputs.														//
// This enables fine control over large groups of Input 		//
// Handlers														//
/*==============================================================*/
public class InputState implements IInputHandler
{
	private final List<IInputHandler> handlers = MalletList.<IInputHandler>newList() ;
	private long timestamp = 0L ;

	public InputState() {}

	public final void addInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			System.out.println( "Input Handler already exists.." ) ;
			return ;
		}

		handlers.add( _handler ) ;
	}

	public final void removeInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			System.out.println( "Input Handler doesn't exist.." + _handler.toString() ) ;
			return ;
		}

		handlers.remove( _handler ) ;
	}

	public void update( final IInputSystem _system )
	{
		_system.passInputs( timestamp, this ) ;
		timestamp = ElapsedTimer.currentTimeMillis() ;
	}

	@Override
	public final InputEvent.Action passInputEvent( final InputEvent _event )
	{
		final int handlerSize = handlers.size() ;
		for( int j = 0; j < handlerSize; ++j )
		{
			final IInputHandler handler = handlers.get( j ) ;
			if( handler.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Remove the Input Handlers and reset them.
	*/
	public final void clearHandlers()
	{
		handlers.clear() ;
	}

	private final boolean exists( final IInputHandler _handler )
	{
		return handlers.contains( _handler ) ;
	}
}
