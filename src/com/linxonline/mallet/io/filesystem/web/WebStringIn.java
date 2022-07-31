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

	@Override
	public void close() throws Exception
	{
		reader.close() ;
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
