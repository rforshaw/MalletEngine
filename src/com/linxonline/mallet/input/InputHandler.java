package com.linxonline.mallet.input ;

public interface InputHandler
{
	public void setInputAdapterInterface( final InputAdapterInterface _adapter ) ;
	public void passInputEvent( final InputEvent _event ) ;
}