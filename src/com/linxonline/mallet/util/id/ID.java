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

	public int nameID = -1 ;
	public int groupID = -1 ;

	public ID( final String _name, final String _group )
	{
		name = _name ;
		group = _group ;

		nameID = name.hashCode() ;
		groupID = group.hashCode() ;
	}

	public ID( final String _name, final int _nameID, 
				final String _group, final int _groupID )
	{
		name = _name ;
		group = _group ;

		nameID = _nameID ;
		groupID = _groupID ;
	}

	public void setName( final String _name )
	{
		name = _name ;
		nameID = _name.hashCode() ;
	}

	public void setGroup( final String _group )
	{
		group = _group ;
		groupID = _group.hashCode() ;
	}

	public boolean isName( final String _name ) { return name.equals( _name ) ; }

	public boolean isNameID( final int _nameID ) { return nameID == _nameID ; }

	public boolean isGroup( final String _group ) { return group.equals( _group ) ; }

	public boolean isGroupID( final int _groupID ) { return groupID == _groupID ; }
	
	public String toString()
	{
		return name + ":" + group ;
	}
}