package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.Writer ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.StringOutStream ;

public class DesktopStringOut implements StringOutStream
{
	private final Writer output ;

	public DesktopStringOut( final Writer _output )
	{
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
