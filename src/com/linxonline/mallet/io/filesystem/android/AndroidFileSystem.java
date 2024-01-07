package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.Context ;
import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

/**
	Provides access to the filesystem on desktop platforms.
	This currently includes: Linux, Windows, & Mac.
*/
public class AndroidFileSystem implements FileSystem
{
	private final Map<String, List<ZipPath>> mapZip = MalletMap.<String, List<ZipPath>>newMap() ;
	private final Map<String, String> mapAssets = MalletMap.<String, String>newMap() ;

	private final Context context ;
	private final AssetManager assetManager ;

	public AndroidFileSystem( final Context _context )
	{
		context = _context ;
		assetManager = context.getAssets() ;
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

		traverseFiles( _directory, _directory + '/' ) ;

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

	private void traverseFiles( final String _path, final String _directory )
	{
		try
		{
			final String[] list = assetManager.list( _path ) ;
			if( list != null )
			{
				final String dir = _path + "/" ;
				for( final String path : list )
				{
					traverseFiles( dir + path, dir ) ;
				}
			}

			// Must be a file. Unless it's a directory with nothing in it...
			if( list.length == 0 )
			{
				//System.out.println( "IS A FILE: " + _path ) ;
				if( isZip( _path ) == true )
				{
					final List<ZipPath> paths = generateZipPaths( _path ) ;
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

	private List<ZipPath> generateZipPaths( final String _file )
	{
		final File file = new File( _file ) ;
		final String zipName = file.getName() ;
		final String zipPath = file.getParent() ;

		final List<ZipPath> paths = MalletList.<ZipPath>newList() ;

		try
		{
			final ZipInputStream stream = new ZipInputStream( assetManager.open( _file ) ) ;
			ZipEntry entry = null ;
			while( ( entry = stream.getNextEntry() ) != null )
			{
				paths.add( new ZipPath( zipName, zipPath, entry ) ) ;
			}

			stream.close() ;
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
		final List<ZipPath> zips = mapZip.get( strip( _path ) ) ;
		if( zips != null )
		{
			try
			{
				return new AndroidZipFile( _path, zips, assetManager ) ;
			}
			catch( final IOException ex )
			{
				Logger.println( "Failed retrieve: " + _path + " from zip file.", Logger.Verbosity.MINOR ) ;
				return null ;
			}
		}
		else if( mapAssets.containsKey( _path ) == true )
		{
			return new AndroidAssetFile( _path, assetManager ) ;
		}

		if( _path.indexOf( getHomeDirectory( "" ) ) > -1 )
		{
			return new AndroidFile( _path, AndroidFile.StorageType.Internal ) ;
		}

		return new AndroidFile( _path, AndroidFile.StorageType.External ) ;
	}

	@Override
	public String getHomeDirectory( final String _projectName )
	{
		// Project name is not needed for Android
		// As the Internal Storage is solely for the application.
		return context.getFilesDir().getAbsolutePath() + '/' ;
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
