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
		assert _input != null ;
		reader = new BufferedReader( new InputStreamReader( _input ) ) ;
	}

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

	public boolean close()
	{
		try
		{
			reader.close() ;
			return true ;
		}
		catch( IOException ex )
		{
			System.out.println( "Byte In - Failed to close Input Stream." ) ;
			return false ;
		}
	}
}