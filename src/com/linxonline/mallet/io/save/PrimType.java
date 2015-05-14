package com.linxonline.mallet.io.save ;

import java.lang.Class ;
import java.lang.reflect.Field ;

enum PrimType
{
	CHAR( Character.TYPE, char[].class ),
	BYTE( Byte.TYPE, byte[].class ),
	INT( Integer.TYPE, int[].class ),
	SHORT( Short.TYPE, short[].class ),
	LONG( Long.TYPE, long[].class ),
	FLOAT( Float.TYPE, float[].class ),
	DOUBLE( Double.TYPE, double[].class ),
	BOOLEAN( Boolean.TYPE, boolean[].class ),
	STRING( String.class, String[].class ),
	UNKNOWN( null, null ) ;

	private final Class type ;
	private final Class arrayType ;

	private PrimType( final Class _class, final Class _arrayClass )
	{
		type = _class ;
		arrayType = _arrayClass ;
	}

	public static PrimType getType( final String _type )
	{
		for( final PrimType prim : PrimType.values() )
		{
			if( _type.equals( prim.name() ) == true )
			{
				return prim ;
			}
		}

		return UNKNOWN ;
	}

	public static PrimType getType( final Field _field )
	{
		return getType( _field.getType() ) ;
	}

	public static PrimType getType( final Class _class )
	{
		for( final PrimType prim : PrimType.values() )
		{
			if( _class.equals( prim.type ) == true ||
				_class.equals( prim.arrayType ) == true )
			{
				return prim ;
			}
		}

		return UNKNOWN ;
	}
}