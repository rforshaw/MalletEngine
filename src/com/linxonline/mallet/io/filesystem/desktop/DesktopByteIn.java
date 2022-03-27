package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.InputStream ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.ByteInStream ;

public class DesktopByteIn implements ByteInStream
{
	private final InputStream stream ;

	public DesktopByteIn( final InputStream _stream )
	{
		stream = _stream ;
	}

	public int readBytes( final byte[] _buffer, final int _offset, final int _length )
	{
		try
		{
			int toRead = _length ;
			int off = 0 ;
			while( toRead > 0 )
			{
				final int read = stream.read( _buffer, _offset + off, toRead ) ;
				if( read == -1 )
				{
					break ;
				}

				off += read ;
				toRead -= read ;
			}

			return off ;
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
