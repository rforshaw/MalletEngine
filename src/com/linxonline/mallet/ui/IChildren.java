package com.linxonline.mallet.ui ;

import java.util.List ;

/**
	Extend this class if the implementation is 
	expected to have children.

	This should only be used to allow other external 
	classes to access, add or remove elements.
*/
public interface IChildren
{
	/**
		Add _element to the class it is now its child.
	*/
	public <T extends UIElement> T addElement( final T _element ) ;

	/**
		Populate the list with the class's child elements.
		The elements returned are not copies.
	*/
	public void getElements( final List<UIElement> _elements ) ;

	/**
		Flag an element to be removed from the class.
		Element should be removed on the next update cycle.
	*/
	public void removeElement( final UIElement _element ) ;
}
