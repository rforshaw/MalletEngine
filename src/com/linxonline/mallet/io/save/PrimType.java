package com.linxonline.mallet.io.save ;

import java.lang.Class ;
import java.lang.reflect.Field ;

enum PrimType
{
	CHAR( Character.TYPE ),
	BYTE( Byte.TYPE ),
	INT( Integer.TYPE ),
	SHORT( Short.TYPE ),
	LONG( Long.TYPE ),
	FLOAT( Float.TYPE ),
	DOUBLE( Double.TYPE ),
	BOOLEAN( Boolean.TYPE ),
	STRING( String.class ),
	UNKNOWN( null ) ;

	private final Class type ;

	private PrimType( final Class _class )
	{
		type = _class ;
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
			if( _class.equals( prim.type ) == true )
			{
				return prim ;
			}
		}

		return UNKNOWN ;
	}
}