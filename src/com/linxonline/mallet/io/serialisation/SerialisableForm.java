package com.linxonline.mallet.io.serialisation ;

/**
	Allows an object to be serialised.
**/
public interface SerialisableForm
{
	public boolean writeObject( final SerialiseOutput _ouput ) ;
	public boolean readObject( final SerialiseInput _input ) ;
}