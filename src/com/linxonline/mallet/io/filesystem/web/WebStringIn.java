package com.linxonline.mallet.io.filesystem.web ;

import java.io.* ;

import com.linxonline.mallet.io.filesystem.* ;

public class WebStringIn implements StringInStream
{
	private final BufferedReader reader ;

	public WebStringIn( final byte[] _input )
	{
		assert _input != null ;
		reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( _input ) ) ) ;
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