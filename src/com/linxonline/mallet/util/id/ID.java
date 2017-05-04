package com.linxonline.mallet.util.id ;

/**
	Convenience class to provide name and group-name
	identifiers for objects.
	Many of the classes in Mallet Engine require the 
	ability to identify one object from another.
*/
public class ID
{
	public String name ;
	public String group ;

	public ID( final String _name, final String _group )
	{
		name = _name ;
		group = _group ;
	}

	public void setName( final String _name )
	{
		name = _name ;
	}

	public void setGroup( final String _group )
	{
		group = _group ;
	}

	public boolean isName( final String _name ) { return name.equals( _name ) ; }

	public boolean isGroup( final String _group ) { return group.equals( _group ) ; }

	public String toString()
	{
		return name + ":" + group ;
	}
}
