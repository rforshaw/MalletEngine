package com.linxonline.mallet.input ;

import java.util.List ;

import com.linxonline.mallet.util.Utility ;

/**
	Accumulate InputEvents until it is appropriate to 
	process them.

	InputController is a convience class, built to deal with 
	correctly handling inputs.

	InputEvents can be passed outside the objects typical update().
	Processing an InputEvent can therefore cause unusual logic problems.
**/
public abstract class InputController implements InputHandler
{
	private final List<InputEvent> inputs = Utility.<InputEvent>newArrayList() ;

	public InputController() {}

	public void update()
	{
		final int size = inputs.size() ;
		for( int i = 0; i < size; ++i )
		{
			processInputEvent( inputs.get( i ) ) ;
		}
		inputs.clear() ;
	}

	public abstract void processInputEvent( final InputEvent _input ) ;

	/**
		Extend function if you wish to determine whether to 
		Consume or Propagate an Input Event.
		Consuming an InputEvent is benificial for UIs, is it will 
		prevent the InputEvent from being processed by other InputHandlers. 
	*/
	@Override
	public InputEvent.Action passInputEvent( final InputEvent _input )
	{
		inputs.add( _input ) ;
		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public void reset()
	{
		inputs.clear() ;
	}
}
