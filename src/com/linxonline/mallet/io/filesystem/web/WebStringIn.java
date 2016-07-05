package com.linxonline.mallet.io.filesystem.web ;

import java.io.* ;

import com.linxonline.mallet.io.filesystem.* ;

public class WebStringIn implements StringInStream
{
	private BufferedReader reader ;

	public WebStringIn() {}

	public WebStringIn( final byte[] _input )
	{
		assert _input != null ;
		set( _input ) ;
	}

	public void set( final byte[] _input )
	{
		if( reader == null )
		{
			reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( _input ) ) ) ;
		}
	}

	public String readLine()
	{
		if( reader == null )
		{
			return "" ;
		}

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