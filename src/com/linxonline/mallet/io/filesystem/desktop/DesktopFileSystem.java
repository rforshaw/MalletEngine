package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.net.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

/**
	Provides access to the filesystem on desktop platforms.
	This currently includes: Linux, Windows, & Mac.
*/
public final class DesktopFileSystem implements FileSystem
{
	private final Map<String, List<ZipPath>> mapZip = MalletMap.<String, List<ZipPath>>newMap() ;

	public DesktopFileSystem() {}

	/**
		Search through the directory mapping out compressed 
		file formats that are supported.
		Currently maps zip file format.
	*/
	@Override
	public boolean mapDirectory( final String _directory )
	{
		assert _directory != null ;

		final File dir = new File( _directory ) ;
		if( dir.isDirectory() == false )
		{
			Logger.println( "Failed to map: " + _directory + " not a directory.", Logger.Verbosity.MINOR ) ;
			return false ;
		}

		final DesktopFileTraversal traversal = new DesktopFileTraversal()
		{
			@Override
			public void foundFile( final File _file )
			{
				if( isZip( _file ) == false )
				{
					return ;
				}

				final List<ZipPath> paths = generateZipPaths( _file ) ;
				for( final ZipPath zip : paths )
				{
					final String id = String.format( "%s/%s", _directory, strip( zip.filePath ) ) ;
					List<ZipPath> zipPaths = mapZip.get( id ) ;
					if( zipPaths == null )
					{
						zipPaths = MalletList.<ZipPath>newList() ;
						mapZip.put( id, zipPaths ) ;
					}

					zipPaths.add( zip ) ;
				}
			}
		} ;

		traversal.traverse( dir ) ;

		// Sort the resources using their zip-path name
		// in descending alphabetical order.
		// A developer can use this to ensure a particular zip
		// has precedence over another.
		for( List<ZipPath> paths : mapZip.values() )
		{
			paths.sort( Collections.reverseOrder( ( final ZipPath _a, final ZipPath _b ) ->
			{
				return _a.getZipPath().compareToIgnoreCase( _b.getZipPath() ) ;
			} ) ) ;
		}

		return true ;
	}

	private static List<ZipPath> generateZipPaths( final File _file )
	{
		final String zipName = _file.getName() ;
		final String zipPath = _file.getParent() ;

		final List<ZipPath> paths = MalletList.<ZipPath>newList() ;

		try
		{
			final ZipFile zipFile = new ZipFile( _file ) ;
			final Enumeration files = zipFile.entries() ;

			ZipEntry entry = null ;
			while( files.hasMoreElements() )
			{
				entry = ( ZipEntry )files.nextElement() ;
				if( entry != null )
				{
					paths.add( new ZipPath( zipName, zipPath, entry ) ) ;
				}
			}

			zipFile.close() ;
		}
		catch( ZipException _ex ) {}
		catch( IOException _ex ) {}

		return paths ;
	}

	private static boolean isZip( final File _file )
	{
		return getExtension( _file.getName() ).equals( "ZIP" ) ;
	}

	private static String getExtension( final String _name )
	{
		final int pos = _name.lastIndexOf( "." ) ;
		return _name.substring( pos + 1 ).toUpperCase() ;
	}

	@Override
	public FileStream getFile( final String _path )
	{
		final List<ZipPath> zips = mapZip.get( strip( _path ) ) ;
		if( zips != null )
		{
			try
			{
				return new DesktopZipFile( _path, zips ) ;
			}
			catch( final IOException ex )
			{
				Logger.println( "Failed retrieve: " + _path + " from zip file.", Logger.Verbosity.MINOR ) ;
				return null ;
			}
		}

		return new DesktopFile( new File( _path ) ) ;
	}

	@Override
	public String getHomeDirectory( final String _projectName )
	{
		final StringBuilder builder = new StringBuilder() ;
		builder.append( System.getProperty( "user.home" ) ) ;
		builder.append( File.separator ) ;

		final String os = System.getProperty( "os.name" ) ;
		if( os.indexOf( "mac" ) >= 0 || os.indexOf( "darwin" ) >= 0 ||
			os.indexOf( "nux" ) >= 0 )
		{
			builder.append( ".local" ) ;
			builder.append( File.separator ) ;
			builder.append( "share" ) ;
			builder.append( File.separator ) ;
		}
		else if( os.indexOf( "Win" ) >= 0 )
		{
			builder.append( "AppData" ) ;
			builder.append( File.separator ) ;
			builder.append( "Local" ) ;
			builder.append( File.separator ) ;
		}

		builder.append( "MalletEngine" ) ;
		builder.append( File.separator ) ;

		builder.append( _projectName.trim() ) ;
		builder.append( File.separator ) ;

		return builder.toString() ;
	}

	private static String strip( final String _val )
	{
		int length = _val.length() ;
		if( length < 2 )
		{
			return _val ;
		}

		if( length > 2 )
		{
			if( _val.charAt( length - 1 ) == '/' )
			{
				length -= 1 ;
			}
		}

		final boolean strip = _val.charAt( 0 ) == '.' && _val.charAt( 1 ) == '/' ;
		final int start = ( strip ) ? 2 : 0 ;
		return _val.substring( start, length ) ;
	}

	public static class ZipPath
	{
		public final String zipName ;			// Name of zip with extension
		public final String zipPath ;			// Path to zip not including the file itself
		public final String filePath ;			// Path to file within zip
		public final boolean isDirectory ;

		public ZipPath( final String _zipName, final String _zipPath, final ZipEntry _entry )
		{
			zipName = _zipName ;
			zipPath = _zipPath ;
			filePath = _entry.getName() ;
			isDirectory = _entry.isDirectory() ;
		}

		public String getZipPath()
		{
			return zipPath + '/' + zipName ;
		}

		public String toString()
		{
			final StringBuilder builder = new StringBuilder() ;
			builder.append( "[Zip Path: " + getZipPath() ) ;
			builder.append( " File Path: " + filePath + "]" ) ;
			return builder.toString() ;
		}
	}
}
