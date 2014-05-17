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

	public static boolean getRaw( final String _path, final int _length, final ResourceCallback _callback )
	{
		RawThread thread = new RawThread( _path, _length, _callback ) ;
		thread.start() ;
		return true ;
	}

	public static boolean getString( final String _file, final int _length, final ResourceCallback _callback )
	{
		final StringThread thread = new StringThread( _file, _length, _callback ) ;
		thread.start() ;
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

	protected static class RawThread extends Thread
	{
		final ResourceCallback callback ;
		final int length ;
		final String path ;

		public RawThread( final String _path, final int _length, final ResourceCallback _callback )
		{
			callback = _callback ;
			length = _length ;
			path = _path ;
		}

		public void run()
		{
			if( length <= 0 )
			{
				batchRead() ;
			}
			else
			{
				iterativeRead() ;
			}
		}

		private void batchRead()
		{
			callback.resourceRaw( ReadFile.getRaw( path ) ) ;
			callback.end() ;
		}
		
		private void iterativeRead()
		{
			try
			{
				final File file = new File( path ) ;
				final int fileLength = ( int )file.length() ;

				final FileInputStream is = new FileInputStream( file ) ;
				int offset = 0 ;

				while( offset < fileLength )
				{
					callback.resourceRaw( read( is, offset, length ) ) ;
					offset += length ;
				}
				is.close() ;
			}
			catch( IOException ex )
			{
				Logger.println( "Failed to Read Stream.", Logger.Verbosity.MAJOR ) ;
			}

			callback.end() ;
		}
	}

	protected static class StringThread extends Thread
	{
		final ResourceCallback callback ;
		final int length ;
		final String file ;

		public StringThread( final String _file, final int _length, final ResourceCallback _callback )
		{
			length = _length ;
			callback = _callback ;
			file = _file ;
		}

		public void run()
		{
			if( length <= 0 )
			{
				batchRead() ;
			}
			else
			{
				iterativeRead() ;
			}
		}

		private void batchRead()
		{
			callback.resourceAsString( ReadFile.getString( file ) ) ;
			callback.end() ;
		}
		
		private void iterativeRead()
		{
			final String[] strings = new String[length] ;
			nullStrings( strings ) ;

			try
			{
				final FileInputStream is = new FileInputStream( file ) ;
				final InputStreamReader isr = new InputStreamReader( is ) ;
				final BufferedReader br = new BufferedReader( isr ) ;

				int i = 0 ;
				while( ( strings[i++] = br.readLine() ) != null )
				{
					if( i >= strings.length )
					{
						sendStringsToCallback( strings, callback ) ;	// Send strings when limit is reached
						nullStrings( strings ) ;
						i = 0 ;
					}
				}

				br.close() ;
				isr.close() ;
				is.close() ;
			}
			catch( IOException ex )
			{
				Logger.println( "Failed to Read Stream.", Logger.Verbosity.MAJOR ) ;
			}

			sendStringsToCallback( strings, callback ) ;				// Send left over strings that didn't reach limit
			callback.end() ;
		}
		
		private void sendStringsToCallback( final String[] _strings, final ResourceCallback _callback )
		{
			for( int i = 0; i < _strings.length; ++i )
			{
				if( _strings[i] != null )
				{
					_callback.resourceAsString( _strings[i] ) ;
				}
			}
		}

		private void nullStrings( final String[] _strings )
		{
			for( int i = 0; i < _strings.length; ++i )
			{
				_strings[i] = null ;
			}
		}
	}
}
