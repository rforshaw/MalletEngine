package com.linxonline.mallet.input ;

public interface InputHandler
{
	/**
		Usually called by either an InputSystem or InputState.
		The return Action informs the caller whether to 
		continue propagating the InputEvent to other handlers 
		or to consume the InputEvent.
	*/
	public InputEvent.Action passInputEvent( final InputEvent _event ) ;

		/**
		Allow the implementation to clear any references 
		it may hold.
		This function is called when the InputHandler is 
		removed from an Input System or Input State.
	*/
	public void reset() ;
}
