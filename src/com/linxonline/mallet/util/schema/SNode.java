package com.linxonline.mallet.util.schema ;

/**
	Allows the developer to define a meta data-structure.
	This can be used by other systems to determine how 
	a bytes stream is potentially constructed.

	It does not store the data itself, it represents what 
	structure data could be in.
*/
public abstract class SNode
{
	private SNode parent = null ;

	/**
		Return the variable's type.
	*/
	public abstract Type getType() ;

	protected void setParent( SNode _parent )
	{
		parent = _parent ;
	}

	public SNode getParent()
	{
		return parent ;
	}
}