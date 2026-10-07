package com.linxonline.mallet.input ;

/**
	Interface to allow the implementation of custom Input Systems.
	Because the Mallet Engine aims to support more than just the 
	default Java input listeners, it must be able to process inputs 
	from other SDK's, for instance, iOS and Android.
**/
public interface IInputSystem
{
	/**
		Pass the corresponding inputevents to _handler that are greater
		than _from.
		The timestamp _from should be in milliseconds.
	*/
	public void passInputs( final long _from, final IInputHandler _handler ) ;

	public void clearInputs() ;
}
