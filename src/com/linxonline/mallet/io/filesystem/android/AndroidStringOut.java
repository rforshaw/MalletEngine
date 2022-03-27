package com.linxonline.mallet.io.filesystem.android ;

import java.io.BufferedWriter ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.StringOutStream ;

public class AndroidStringOut implements StringOutStream
{
	private final BufferedWriter output ;

	public AndroidStringOut( final BufferedWriter _output )
	{
		assert _output != null ;
		output = _output ;
	}

	@Override
	public boolean writeLine( final String _line )
	{
		try
		{
			output.write( _line ) ;
			return true ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
	}

	@Override
	public void close() throws Exception
	{
		output.flush() ;
		output.close() ;
	}
}
