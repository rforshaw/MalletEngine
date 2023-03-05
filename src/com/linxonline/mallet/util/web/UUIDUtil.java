package com.linxonline.mallet.util ;

import java.util.UUID ;

public class UUIDUtil
{
	private UUIDUtil() {}

	public static UUID randomUUID()
	{
		return UUID.randomUUID() ;
	}

	public static UUID createUUID( final long _mostSigBits, final long _leastSigBits )
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( digits( _mostSigBits >> 32, 8 ) ) ;
		builder.append( '_' ) ;

		builder.append( digits( _mostSigBits >> 16, 4 ) ) ;
		builder.append( '_' ) ;

		builder.append( digits( _mostSigBits, 4 ) ) ;
		builder.append( '_' ) ;

		builder.append( digits( _leastSigBits >> 48, 4 ) ) ;
		builder.append( '_' ) ;

		builder.append( digits( _leastSigBits, 12 ) ) ;

		return createUUID( builder.toString() ) ;
	}

	public static UUID createUUID( final String _value )
	{
		return UUID.fromString( _value ) ;
	}

	private static String digits( final long _val, final int _digits )
	{
		final long hi = 1L << ( _digits * 4 ) ;
		return Long.toHexString( hi | ( _val & ( hi - 1 ) ) ).substring( 1 ) ;
	}
}
