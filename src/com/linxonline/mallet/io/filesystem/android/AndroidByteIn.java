package com.linxonline.mallet.io.filesystem.android ;

import java.io.InputStream ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.ByteInStream ;

public class AndroidByteIn implements ByteInStream
{
	private final InputStream stream ;

	public AndroidByteIn( final InputStream _stream )
	{
		stream = _stream ;
	}

	public int readBytes( final byte[] _buffer, final int _offset, final int _length )
	{
		try
		{
			return stream.read( _buffer, _offset, _length ) ;
		}
		catch( IOException ex )
		{
			return -1 ;
		}
	}

	public InputStream getInputStream()
	{
		return stream ;
	}

	public boolean close()
	{
		try
		{
			stream.close() ;
			return true ;
		}
		catch( IOException ex )
		{
			System.out.println( "Byte In - Failed to close Input Stream." ) ;
			return false ;
		}
	}
}