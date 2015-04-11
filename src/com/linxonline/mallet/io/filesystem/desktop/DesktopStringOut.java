package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.Writer ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.StringOutStream ;

public class DesktopStringOut implements StringOutStream
{
	private final Writer output ;

	public DesktopStringOut( final Writer _output )
	{
		assert _output != null ;
		output = _output ;
	}
	
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