package com.linxonline.mallet.io.filesystem.android ;

import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.BufferedReader ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.* ;

public class AndroidStringIn implements StringInStream
{
	private final BufferedReader reader ;

	public AndroidStringIn( final InputStream _input )
	{
		reader = new BufferedReader( new InputStreamReader( _input ) ) ;
	}

	@Override
	public String readLine()
	{
		try
		{
			return reader.readLine() ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	@Override
	public void close() throws Exception
	{
		reader.close() ;
	}
}
