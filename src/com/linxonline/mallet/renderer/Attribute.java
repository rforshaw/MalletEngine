package com.linxonline.mallet.renderer ;

public final class Attribute
{
	// The name this attribute is associated to within the program.
	public final String name ;
	public final Shape.Attribute type ;
	// A Program must map 1:1 to the geometry it wishes to use,
	// but that doesn't mean all the attributes defined by
	// the geometry need to be used.
	public final boolean ignore ;

	public Attribute( final String _name, final Shape.Attribute _type )
	{
		this( _name, _type, false ) ;
	}

	public Attribute( final String _name, final Shape.Attribute _type, final boolean _ignore )
	{
		name = _name ;
		type = _type ;
		ignore = _ignore ;
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
