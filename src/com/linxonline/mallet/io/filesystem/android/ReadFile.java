package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.* ;

import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.ResourceCallback ;
import com.linxonline.mallet.util.logger.Logger ;

public class ReadFile
{
	private static AssetManager assetManager ;

	public static void setAssetManager( final AssetManager _manager )
	{
		assetManager = _manager ;
	}

	/**
		Blocks calling Thread.
		Returns entire file within byte array
	**/
	public static byte[] getRaw( final String _path )
	{
		try
		{
			final InputStream is = assetManager.open( _path ) ;
			final byte[] buffer = new byte[is.available()] ;

			final int readNum = read( is, buffer ) ;
			is.close() ;

			return buffer ;
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
		final RawThread thread = new RawThread( _path, _length, _callback ) ;
		thread.start() ;
		return true ;
	}

	public static boolean getString( final String _file, final int _length, final ResourceCallback _callback )
	{
		final StringThread thread = new StringThread( _file, _length, _callback ) ;
		thread.start() ;
		return true ;
	}

	public static int read( final InputStream _stream, final byte[] _buffer ) throws IOException
	{
		int offset = 0 ;
		int numRead = 0 ;

		while( ( offset < _buffer.length ) &&
			   ( numRead = _stream.read( _buffer, offset, _buffer.length - offset ) ) >= 0 ) 
		{
			offset += numRead;
		}

		return numRead ;
	}

	protected static class RawThread extends Thread
	{
		private final ResourceCallback callback ;		// Where to return the byte stream
		private final String path ;						// File location
		private int toReadNum ;							// How many bytes to read in each iteration

		public RawThread( final String _path, final int _length, final ResourceCallback _callback )
		{
			callback = _callback ;
			toReadNum = _length ;
			path = _path ;
		}

		public void run()
		{
			try
			{
				final InputStream is = assetManager.open( path ) ;
				final int fileLength = is.available() ;
				callback.start( fileLength ) ;

				int offset = 0 ;
				while( offset < fileLength && toReadNum > ResourceCallback.STOP )
				{
					// Set length to the amount of bytes to read in next
					toReadNum = ( toReadNum == ResourceCallback.RETURN_ALL ) ? ( fileLength - offset ) : toReadNum ;

					final byte[] buffer = new byte[toReadNum] ;
					final int readNum = read( is, buffer ) ;
					offset += readNum ;

					toReadNum = callback.resourceRaw( buffer, readNum ) ;
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
		private final ResourceCallback callback ;
		private final String path ;
		private int toReadNum ;

		public StringThread( final String _file, final int _length, final ResourceCallback _callback )
		{
			toReadNum = _length ;
			callback = _callback ;
			path = _file ;
		}

		public void run()
		{
			try
			{
				final InputStream is = assetManager.open( path ) ;
				final int fileLength = is.available() ;
				callback.start( fileLength ) ;

				final ArrayList<String> strings = new ArrayList<String>() ;

				final InputStreamReader isr = new InputStreamReader( is ) ;
				final BufferedReader br = new BufferedReader( isr ) ;
				int offset = 0 ;

				String line = null ;
				while( ( ( line = br.readLine() ) != null ) && ( toReadNum > ResourceCallback.STOP ) )
				{
					strings.add( line ) ;
					if( toReadNum == ResourceCallback.RETURN_ALL )
					{
						continue ;
					}
					else if( strings.size() >= toReadNum )
					{
						final int size = strings.size() ;
						toReadNum = callback.resourceAsString( strings.toArray( new String[size] ), size ) ;
						strings.clear() ;
					}
				}

				{
					final int size = strings.size() ;
					callback.resourceAsString( strings.toArray( new String[size] ), size ) ;
				}

				br.close() ;
				isr.close() ;
				is.close() ;
			}
			catch( IOException ex )
			{
				Logger.println( "Failed to Read Stream.", Logger.Verbosity.MAJOR ) ;
			}

			callback.end() ;
		}
	}
}
