package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

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
}
