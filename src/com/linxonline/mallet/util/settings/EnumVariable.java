package com.linxonline.mallet.util.settings ;

public final class EnumVariable extends AVariable
{
	public Enum value ;

	public EnumVariable()
	{
		type = AVariable.ENUM_TYPE ;
	}

	public EnumVariable( final String _name, final Enum _value )
	{
		type = AVariable.ENUM_TYPE ;
		name = _name ;
		setEnum( _value ) ;
	}

	public void setEnum( final Enum _value )
	{
		type = AVariable.ENUM_TYPE ;
		value = _value ;		// Even if _value is null set value to it.
	}
	
	public Enum getEnum()
	{
		return value ;
	}

	public Enum[] getEnumConstants()
	{
		final Class clazz = value.getDeclaringClass() ;
		Object[] enums = clazz.getEnumConstants() ;

		for( final Object e : enums )
		{
			System.out.println( "E: " + e.toString() ) ;
		}

		return ( Enum[] )enums ;
	}

	public String toString()
	{
		return value.toString() ;
	}
}
