package com.linxonline.mallet.input ;

/**
	Interface to allow the implementation of custom Input Systems.
	Because the Mallet Engine aims to support more than just the 
	default Java input listeners, it must be able to process inputs 
	from other SDK's, for instance, iOS and Android.
**/
public interface IInputSystem
{
	public void addInputHandler( final InputHandler _handler ) ;
	public void removeInputHandler( final InputHandler _handler ) ;

	public void update() ;
	
	public void clearHandlers() ;
	public void clearInputs() ;
}
