package com.linxonline.mallet.util.schema ;

/**
	Allows the developer to define a meta data-structure.
	This can be used by other systems to determine how 
	a bytes stream is potentially constructed.

	It does not store the data itself, it represents what 
	structure data could be in.
*/
public interface IVar
{
	/**
		Return the variable's type.
	*/
	public Type getType() ;
}
