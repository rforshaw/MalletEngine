package com.linxonline.mallet.util.id ;

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

	public boolean isName( final String _name ) { return name.equals( _name ) ; }
	
	public boolean isNameID( final int _nameID ) { return nameID == _nameID ; }

	public boolean isGroup( final String _group ) { return group.equals( _group ) ; }

	public boolean isGroupID( final int _groupID ) { return groupID == _groupID ; }
}