package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.logger.Logger ;

public final class ReadFile
{
	private ReadFile() {}

	public static boolean getRaw( final ByteInStream _stream, final ByteInCallback _callback, final int _length )
	{
		final RawThread thread = new RawThread( _stream, _callback, _length ) ;
		thread.start() ;
		return true ;
	}

	public static boolean getString( final StringInStream _stream, final StringInCallback _callback, final int _length )
	{
		final StringThread thread = new StringThread( _stream, _callback, _length ) ;
		thread.start() ;
		return true ;
	}

	protected static class RawThread extends Thread
	{
		private final ByteInStream stream ;
		private final ByteInCallback callback ;		// Where to return the byte stream
		private int toReadNum ;						// How many bytes to read in each iteration

		public RawThread( final ByteInStream _stream, final ByteInCallback _callback, final int _length )
		{
			callback = _callback ;
			toReadNum = _length ;
			stream = _stream ;
		}

		public void run()
		{
			callback.start() ;
			int readNum = 0 ;

			while( ( readNum > -1 ) && ( toReadNum > ByteInCallback.STOP ) )
			{
				final byte[] buffer = new byte[toReadNum] ;
				readNum = stream.readBytes( buffer, 0, toReadNum ) ;
				toReadNum = callback.readBytes( buffer, readNum ) ;
			}

			stream.close() ;
			callback.end() ;
		}
	}

	protected static class StringThread extends Thread
	{
		private final StringInStream stream ;
		private final StringInCallback callback ;
		private int toReadNum ;

		public StringThread( final StringInStream _stream, final StringInCallback _callback, final int _length )
		{
			toReadNum = _length ;
			callback = _callback ;
			stream = _stream ;
		}

		public void run()
		{
			callback.start() ;

			final ArrayList<String> strings = new ArrayList<String>() ;

			String line = null ;
			while( ( ( line = stream.readLine() ) != null ) && ( toReadNum > StringInCallback.STOP ) )
			{
				strings.add( line ) ;
				if( toReadNum == StringInCallback.RETURN_ALL )
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

			stream.close() ;
			callback.end() ;
		}
	}
}
