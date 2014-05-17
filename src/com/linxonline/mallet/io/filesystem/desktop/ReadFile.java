package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.ResourceCallback ;
import com.linxonline.mallet.util.logger.Logger ;

public class ReadFile
{
	/**
		Blocks calling Thread.
		Returns entire file within byte array
	**/
	public static byte[] getRaw( final String _path )
	{
		try
		{
			final File file = new File( _path ) ;
			final FileInputStream is = new FileInputStream( file ) ;
			final byte[] stream = read( is, 0, ( int )file.length() ) ;
			is.close() ;
			return stream ;
		}
		catch( IOException ex )
		{
			Logger.println( "Failed to Read Stream.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}
	}

	/**
		Blocks calling Thread.
		Returns entire file as a string
	**/
	public static String getString( final String _path )
	{
		return new String( getRaw( _path ) ) ;
	}

	public static boolean getString( final String _file, final int _length, final ResourceCallback _callback )
	{
		if( _length == 0 )
		{
			final Thread thread = new Thread()
			{
				public void run()
				{
					_callback.resourceAsString( ReadFile.getString( _file ) ) ;
					_callback.end() ;
				}
			} ;
			thread.start() ;
		}
		else
		{
			final Thread thread = new Thread()
			{
				final String[] strings = new String[_length] ;
				public void run()
				{
					try
					{
						final FileInputStream is = new FileInputStream( _file ) ;
						final InputStreamReader isr = new InputStreamReader( is ) ;
						final BufferedReader br = new BufferedReader( isr ) ;

						int i = 0 ;
						while( ( strings[i++] = br.readLine() ) != null )
						{
							if( i >= strings.length )
							{
								for( int j = 0; j < strings.length; ++j )
								{
									_callback.resourceAsString( strings[j] ) ;
								}
								i = 0 ;
							}
						}
					}
					catch( IOException ex )
					{
						Logger.println( "Failed to Read Stream.", Logger.Verbosity.MAJOR ) ;
					}

					_callback.end() ;
				}
			} ;
			thread.start() ;
		}
		return true ;
	}

	public static byte[] read( final InputStream _stream, final int _offset, final int _length ) throws IOException
	{
		final byte[] bytes = new byte[_length] ;

		int offset = _offset ;
		int numRead = 0 ;
		while( offset < bytes.length &&
			( numRead = _stream.read( bytes, offset, bytes.length - offset ) ) >= 0 ) 
		{
			offset += numRead;
		}

		return bytes ;
	}
}
