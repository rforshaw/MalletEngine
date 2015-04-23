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
	private ArrayList<InputEvent> inputs = new ArrayList<InputEvent>() ;
	private InputEvent event = null ;
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
	public final void passInputEvent( final InputEvent _event )
	{
		if( inputs.size() > MAX_QUEUE_THRESHOLD )
		{
			System.out.println( "InputState: INPUT EVENT, THRESHHOLD REACHED" ) ;
			inputs.clear() ;
		}

		inputs.add( _event ) ;
	}

	@Override
	public final void update()
	{
		final int size = inputs.size() ;
		final int handlerSize = handlers.size() ;

		for( int i = 0; i < size; ++i )
		{
			event = inputs.get( i ) ;
			for( int j = 0; j < handlerSize; ++j )
			{
				handler = handlers.get( j ) ;
				handler.passInputEvent( event ) ;
			}
		}

		inputs.clear() ;
	}

	public final boolean hasInputs()
	{
		return !( inputs.isEmpty() ) ;
	}

	@Override
	public final void clearHandlers()
	{
		handlers.clear() ;
	}

	@Override
	public final void clearInputs()
	{
		inputs.clear() ;
	}

	private final boolean exists( InputHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}
}
