package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.ResourceCallback ;
import com.linxonline.mallet.util.logger.Logger ;

public class ReadZip
{
	private static AssetManager assetManager ;

	public static void setAssetManager( final AssetManager _manager )
	{
		assetManager = _manager ;
	}

	public static byte[] getRaw( final String _path, final String _zipPath )
	{
		try
		{
			final InputStream is = assetManager.open( _path ) ;
			final ZipInputStream zis = new ZipInputStream( is ) ;

			// By returning the ZipEntry the zis will be at the entry to read it.
			final ZipEntry entry = getZipEntry( _zipPath, zis ) ;
			byte[] buffer = null ; 
			
			if( entry != null )
			{
				buffer = new byte[( int )entry.getSize()] ;
				final int readNum = ReadFile.read( zis, buffer ) ;
			}

			zis.close() ;
			is.close() ;

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

	private static ZipEntry getZipEntry( final String _zipPath, final ZipInputStream _zis ) throws IOException
	{
		ZipEntry entry ;
		while( ( entry = _zis.getNextEntry() ) != null )
		{
			if( _zipPath.equals( entry.getName() ) == true )
			{
				return entry ;
			}
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
				final InputStream is = assetManager.open( path ) ;
				final ZipInputStream zis = new ZipInputStream( is ) ;

				// By returning the ZipEntry the zis will be at the entry to read it.
				final ZipEntry entry = getZipEntry( zipPath, zis ) ;
				if( entry != null )
				{
					final int fileLength = ( int )entry.getSize() ;
					callback.start( fileLength ) ;

					int offset = 0 ;
					while( offset < fileLength && toReadNum > ResourceCallback.STOP )
					{
						// Set length to the amount of bytes to read in next
						toReadNum = ( toReadNum == ResourceCallback.RETURN_ALL ) ? ( fileLength - offset ) : toReadNum ;

						final byte[] buffer = new byte[toReadNum] ;
						final int readNum = ReadFile.read( zis, buffer ) ;
						offset += readNum ;

						toReadNum = callback.resourceRaw( buffer, readNum ) ;
					}
				}

				zis.close() ;
				is.close() ;
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
				final InputStream is = assetManager.open( path ) ;
				final ZipInputStream zis = new ZipInputStream( is ) ;

				// By returning the ZipEntry the zis will be at the entry to read it.
				final ZipEntry entry = getZipEntry( zipPath, zis ) ;
				if( entry != null )
				{
					final int fileLength = ( int )entry.getSize() ;
					callback.start( fileLength ) ;

					final ArrayList<String> strings = new ArrayList<String>() ;
					final InputStreamReader isr = new InputStreamReader( zis ) ;
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
				}

				zis.close() ;
				is.close() ;
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
