package com.linxonline.mallet.input ;

import java.util.ArrayList ;

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
	private final ArrayList<InputEvent> inputs = new ArrayList<InputEvent>() ;

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

	@Override
	public void passInputEvent( final InputEvent _input )
	{
		inputs.add( _input ) ;
	}
}