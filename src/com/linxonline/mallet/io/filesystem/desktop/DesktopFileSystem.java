package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.io.formats.json.desktop.* ;

/**
	Provides access to the filesystem on desktop platforms.
	This currently includes: Linux, Windows, & Mac.
*/
public class DesktopFileSystem implements FileSystem
{
	private final HashMap<String, ZipPath> mapZip = new HashMap<String, ZipPath>() ;

	public DesktopFileSystem()
	{
		initJSONConstructors() ;
	}

	/**
		Allows reading and parsing JSON formatted files.
		Mallet Engine provides a wrapper around a platform 
		JSON library.
	*/
	protected void initJSONConstructors()
	{
		DesktopJSONObject.init() ;
		DesktopJSONArray.init() ;
	}

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
			public void foundFile( final File _file )
			{
				if( isZip( _file ) == true )
				{
					final ArrayList<ZipPath> paths = generateZipPaths( _file ) ;
					for( final ZipPath zip : paths )
					{
						mapZip.put( _directory + zip.filePath, zip ) ;
					}
				}
			}
		} ;

		traversal.traverse( dir ) ;
		return true ;
	}

	private static ArrayList<ZipPath> generateZipPaths( final File _file )
	{
		final String zipName = _file.getName() ;
		final String zipPath = _file.getParent() ;

		final ArrayList<ZipPath> paths = new ArrayList<ZipPath>() ;

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
					if( entry.isDirectory() == false )
					{
						paths.add( new ZipPath( zipName, zipPath, entry.getName() ) ) ;
					}
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
		if( mapZip.containsKey( _path ) == true )
		{
			try
			{
				return new DesktopZipFile( mapZip.get( _path ) ) ;
			}
			catch( final IOException ex )
			{
				return null ;
			}
		}

		return new DesktopFile( new File( _path ) ) ;
	}

	public static class ZipPath
	{
		public final String zipName ;			// Name of zip with extension
		public final String zipPath ;			// Path to zip not including the file itself
		public final String filePath ;			// Path to file within zip

		public ZipPath( final String _zipName, final String _zipPath, final String _filePath )
		{
			zipName = _zipName ;
			zipPath = _zipPath ;
			filePath = _filePath ;
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