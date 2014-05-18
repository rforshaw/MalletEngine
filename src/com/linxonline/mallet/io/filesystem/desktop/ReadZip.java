package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.ResourceCallback ;
import com.linxonline.mallet.util.logger.Logger ;

public class ReadZip
{
	public static byte[] getRaw( final String _path, final String _zipPath )
	{
		try
		{
			final ZipFile zipFile = new ZipFile( _path ) ;
			final ZipEntry entry = zipFile.getEntry( _zipPath ) ;
			final int length = ( int )entry.getSize() ;

			final InputStream is = zipFile.getInputStream( entry ) ;
			final byte[] stream = ReadFile.read( is, 0, length ) ;
			
			is.close() ;
			zipFile.close() ;
			return stream ;
		}
		catch( ZipException _ex )
		{
			Logger.println( "Failed to access Zip", Logger.Verbosity.MAJOR ) ;
		}
		catch( IOException _ex )
		{
			Logger.println( "Failed to read Zip", Logger.Verbosity.MAJOR ) ;
		}

		return null ;
	}

	public static String getString( final String _path, final String _zipPath  )
	{
		return new String( getRaw( _path, _zipPath ) ) ;
	}

	public static boolean getRaw( final String _path, final String _zipPath, final int _length, final ResourceCallback _callback )
	{
		final RawThread thread = new RawThread( _path, _zipPath, _length, _callback ) ;
		thread.start() ;
		return true ;
	}

	public static boolean getString( final String _path, final String _zipPath, final int _length, final ResourceCallback _callback )
	{
		final StringThread thread = new StringThread( _path, _zipPath, _length, _callback ) ;
		thread.start() ;
		return true ;
	}

	protected static class RawThread extends Thread
	{
		final ResourceCallback callback ;
		final int length ;
		final String path ;
		final String zipPath ;

		public RawThread( final String _path, final String _zipPath, final int _length, final ResourceCallback _callback )
		{
			callback = _callback ;
			length = _length ;
			path = _path ;
			zipPath = _zipPath ;
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
			callback.resourceRaw( ReadZip.getRaw( path, zipPath ) ) ;
			callback.end() ;
		}

		private void iterativeRead()
		{
			try
			{
				final ZipFile zipFile = new ZipFile( path ) ;
				final ZipEntry entry = zipFile.getEntry( zipPath ) ;
				final int fileLength = ( int )entry.getSize() ;

				final InputStream is = zipFile.getInputStream( entry ) ;

				int offset = 0 ;
				while( offset < fileLength )
				{
					callback.resourceRaw( ReadFile.read( is, offset, length ) ) ;
					offset += length ;
				}

				is.close() ;
				zipFile.close() ;
			}
			catch( ZipException ex )
			{
				Logger.println( "Failed to access Zip", Logger.Verbosity.MAJOR ) ;
			}
			catch( IOException ex )
			{
				Logger.println( "Failed to read Zip.", Logger.Verbosity.MAJOR ) ;
			}

			callback.end() ;
		}
	}

	protected static class StringThread extends Thread
	{
		final ResourceCallback callback ;
		final int length ;
		final String path ;
		final String zipPath ;

		public StringThread( final String _path, final String _zipPath, final int _length, final ResourceCallback _callback )
		{
			length = _length ;
			callback = _callback ;
			path = _path ;
			zipPath = _zipPath ;
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
			callback.resourceAsString( ReadZip.getString( path, zipPath ) ) ;
			callback.end() ;
		}
		
		private void iterativeRead()
		{
			final String[] strings = new String[length] ;
			nullStrings( strings ) ;

			try
			{
				final ZipFile zipFile = new ZipFile( path ) ;
				final ZipEntry entry = zipFile.getEntry( zipPath ) ;

				final InputStream is = zipFile.getInputStream( entry ) ;
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
				zipFile.close() ;
			}
			catch( ZipException ex )
			{
				Logger.println( "Failed to access Zip", Logger.Verbosity.MAJOR ) ;
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
