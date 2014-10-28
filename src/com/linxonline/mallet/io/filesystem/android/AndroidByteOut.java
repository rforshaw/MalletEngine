package com.linxonline.mallet.io.filesystem.android ;

import java.io.OutputStream ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.ByteOutStream ;

public class AndroidByteOut implements ByteOutStream
{
	private final OutputStream output ;

	public AndroidByteOut( final OutputStream _output )
	{
		assert _output != null ;
		output = _output ;
	}
	
	public int writeBytes( final byte[] _stream, final int _offset, final int _length )
	{
		try
		{
			output.write( _stream, _offset, _length ) ;
			return _length ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return -1 ;
		}
	}

	public boolean close()
	{
		try
		{
			output.flush() ;
			output.close() ;
			return true ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}
}