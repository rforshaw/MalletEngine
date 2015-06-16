package com.linxonline.mallet.input ;

public interface InputHandler
{
	public void setInputAdapterInterface( final InputAdapterInterface _adapter ) ;

	/**
		Usually called by either an InputSystem or InputState.
		The return Action informs the caller whether to 
		continue propagating the InputEvent to other handlers 
		or to consume the InputEvent.
	*/
	public InputEvent.Action passInputEvent( final InputEvent _event ) ;
}