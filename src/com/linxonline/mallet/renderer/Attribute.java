package com.linxonline.mallet.renderer ;

public final class Attribute
{
	// The name this attribute is associated to within the program.
	public final String name ;
	public final Shape.Attribute type ;

	public Attribute( final String _name, final Shape.Attribute _type )
	{
		name = _name ;
		type = _type ;
	}

	public boolean isCompatible( final Attribute _attr )
	{
		return name.equals( _attr.name ) && type == _attr.type ;
	}

	@Override
	public String toString()
	{
		return name + " " + type.toString() ;
	}
}
