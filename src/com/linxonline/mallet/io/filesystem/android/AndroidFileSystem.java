package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.Context ;
import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.io.formats.json.android.* ;

/**
	Provides access to the filesystem on desktop platforms.
	This currently includes: Linux, Windows, & Mac.
*/
public class AndroidFileSystem implements FileSystem
{
	private final HashMap<String, ZipPath> mapZip = new HashMap<String, ZipPath>() ;
	private final HashMap<String, String> mapAssets = new HashMap<String, String>() ;

	private final Context context ;
	private final AssetManager assetManager ;

	public AndroidFileSystem( final Context _context )
	{
		initJSONConstructors() ;

		context = _context ;
		assetManager = context.getAssets() ;
	}

	/**
		Allows reading and parsing JSON formatted files.
		Mallet Engine provides a wrapper around a platform 
		JSON library.
	*/
	protected void initJSONConstructors()
	{
		AndroidJSONObject.init() ;
		AndroidJSONArray.init() ;
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
		if( context == null )
		{
			System.out.println( "Failed TO ACQUIRE CONTEXT" ) ;
			return false ;
		}

		traverseFiles( _directory ) ;
		return true ;
	}

	private void traverseFiles( final String _path )
	{
		try
		{
			final String[] list = assetManager.list( _path ) ;
			if( list != null )
			{
				final String dir = _path + "/" ;
				for( final String path : list )
				{
					traverseFiles( dir + path ) ;
				}
			}

			// Must be a file. Unless it's a directory with nothing in it...
			if( list.length == 0 )
			{
				//System.out.println( "IS A FILE: " + _path ) ;
				if( isZip( _path ) == true )
				{
					//System.out.println( "ZIP" ) ;
					generateZipPaths( _path ) ;
				}
				else
				{
					//System.out.println( "ASSET" ) ;
					generateAssetPath( _path ) ;
				}
			}
		}
		catch( IOException _ex )
		{
			_ex.printStackTrace() ;
		}
	}

	private void generateAssetPath( final String _file )
	{
		mapAssets.put( _file, _file ) ;
	}

	private static ArrayList<ZipPath> generateZipPaths( final String _file )
	{
		final File file = new File( _file ) ;
		final String zipName = file.getName() ;
		final String zipPath = file.getParent() ;

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

	private static boolean isZip( final String _file )
	{
		return getExtension( _file ).equals( "ZIP" ) ;
	}

	private static String getExtension( final String _name )
	{
		final int pos = _name.lastIndexOf( "." ) ;
		return _name.substring( pos + 1 ).toUpperCase() ;
	}

	@Override
	public FileStream getFile( final String _path )
	{
		System.out.println( "GET FILE: " + _path ) ;

		if( mapZip.containsKey( _path ) == true )
		{
			try
			{
				return new AndroidZipFile( mapZip.get( _path ) ) ;
			}
			catch( final IOException ex )
			{
				return null ;
			}
		}
		else if( mapAssets.containsKey( _path ) == true )
		{
			return new AndroidAssetFile( _path, assetManager ) ;
		}

		return new AndroidFile( _path ) ;
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