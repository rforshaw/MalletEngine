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
		return new UUID( _mostSigBits, _leastSigBits ) ;
	}

	public static UUID createUUID( final String _value )
	{
		return UUID.fromString( _value ) ;
	}
}
