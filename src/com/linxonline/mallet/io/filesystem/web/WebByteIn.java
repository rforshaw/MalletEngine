package com.linxonline.mallet.io.filesystem.web ;

import java.io.* ;

import com.linxonline.mallet.io.filesystem.ByteInStream ;

public class WebByteIn implements ByteInStream
{
	private final InputStream stream ;

	public WebByteIn( final byte[] _stream )
	{
		stream = new ByteArrayInputStream( _stream ) ;
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

	@Override
	public void close() throws Exception
	{
		stream.close() ;
	}
}
