package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.BufferedReader ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.* ;

public class DesktopStringIn implements StringInStream
{
	private final BufferedReader reader ;

	public DesktopStringIn( final InputStream _input )
	{
		assert _input != null ;
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

	@Override
	public String toString()
	{
		try
		{
			reader.reset() ;
			final StringBuilder builder = new StringBuilder() ;
			String line = null ;
			while( ( line = readLine() ) != null )
			{
				builder.append( line ) ;
			}
			return builder.toString() ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return super.toString() ;
		}
	}
}
