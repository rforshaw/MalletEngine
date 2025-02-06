package com.linxonline.mallet.util.tools ;

import java.security.MessageDigest ;
import java.security.NoSuchAlgorithmException ;

public final class ChecksumGenerator
{
	private final static byte[] EMPTY = new byte[0] ;

	private MessageDigest md ;

	public ChecksumGenerator() {}

	public byte[] generate( final String _to )
	{
		return generate( _to.getBytes() ) ;
	}

	public byte[] generate( final byte[] _to )
	{
		try
		{
			createDigest() ;
			return md.digest( _to ) ;
		}
		catch( final Exception ex )
		{
			return EMPTY ;
		}
	}

	public byte[] generate( final String[] _to )
	{
		try
		{
			createDigest() ;

			for( int i = 0; i < _to.length; ++i )
			{
				final byte[] d = _to[i].getBytes() ;
				md.update( d ) ;
			}

			return md.digest() ;
		}
		catch( final Exception ex )
		{
			return EMPTY ;
		}
	}

	private void createDigest() throws NoSuchAlgorithmException
	{
		if( md == null )
		{
			md = MessageDigest.getInstance( "MD5" ) ;
		}
	}
}
