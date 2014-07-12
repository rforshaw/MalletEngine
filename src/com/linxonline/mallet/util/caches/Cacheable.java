package com.linxonline.mallet.util.caches ;

/**
	The aim of the Cacheable interface is to allow classes 
	that have implemented the Pool Interface to better control 
	the objects they retain.
	As it stands the Cacheable interface standardises reseting 
	the class's variables to default values, as if it was a 
	default constructor.
*/
public interface Cacheable
{
	/**
		Reset the object to its default values.
	*/
	public void reset() ;
}