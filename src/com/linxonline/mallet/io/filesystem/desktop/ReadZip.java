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
			
			final byte[] buffer = new byte[length] ;
			final int readNum = ReadFile.read( is, buffer ) ;
			
			is.close() ;
			zipFile.close() ;
			return buffer ;
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
		final String path ;
		final String zipPath ;
		int toReadNum ;

		public RawThread( final String _path, final String _zipPath, final int _length, final ResourceCallback _callback )
		{
			callback = _callback ;
			toReadNum = _length ;
			path = _path ;
			zipPath = _zipPath ;
		}

		public void run()
		{
			try
			{
				final ZipFile zipFile = new ZipFile( path ) ;
				final ZipEntry entry = zipFile.getEntry( zipPath ) ;

				final int fileLength = ( int )entry.getSize() ;
				callback.start( fileLength ) ;

				final InputStream is = zipFile.getInputStream( entry ) ;

				int offset = 0 ;
				while( offset < fileLength && toReadNum > ResourceCallback.STOP )
				{
					// Set length to the amount of bytes to read in next
					toReadNum = ( toReadNum == ResourceCallback.RETURN_ALL ) ? ( fileLength - offset ) : toReadNum ;

					final byte[] buffer = new byte[toReadNum] ;
					final int readNum = ReadFile.read( is, buffer ) ;
					offset += readNum ;

					toReadNum = callback.resourceRaw( buffer, readNum ) ;
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
		private final ResourceCallback callback ;
		private final String path ;
		private final String zipPath ;
		private int toReadNum ;

		public StringThread( final String _path, final String _zipPath, final int _length, final ResourceCallback _callback )
		{
			toReadNum = _length ;
			callback = _callback ;
			path = _path ;
			zipPath = _zipPath ;
		}

		public void run()
		{
			try
			{
				final ZipFile zipFile = new ZipFile( path ) ;
				final ZipEntry entry = zipFile.getEntry( zipPath ) ;

				final int fileLength = ( int )entry.getSize() ;
				callback.start( fileLength ) ;

				final ArrayList<String> strings = new ArrayList<String>() ;

				final InputStream is = zipFile.getInputStream( entry ) ;
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

			callback.end() ;
		}
	}
}
