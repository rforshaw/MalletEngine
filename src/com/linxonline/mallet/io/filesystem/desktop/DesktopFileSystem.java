package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.io.formats.json.desktop.* ;

/**
	Provides access tot he filesystem on desktop platforms.
	This currently includes: Linux, Windows, & Mac.
*/
public class DesktopFileSystem implements FileSystem
{
	private HashMap<String, DataFile> resources = new HashMap<String, DataFile>() ;
	private static String ROOT_DIRECTORY = "base" ;

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
		Scans the ROOT_DIRECTORY directory and map it to the HashMap.
		Supports Zip files & uncompressed files located within the 
		ROOT_DIRECTORY.
	**/
	public void scanBaseDirectory()
	{
		final ArrayList<File> files = new ArrayList<File>() ;
		final DesktopFileTraversal traversal = new DesktopFileTraversal()
		{
			public void foundFile( final File _file )
			{
				files.add( _file ) ;
			}
		} ;

		traversal.traverse( new File( ROOT_DIRECTORY ) ) ;

		for( final File file : files )
		{
			if( isZipFile( file ) == true )
			{
				mapZipToResources( file ) ;
				continue ;
			}

			mapToResources( file ) ;
		}
	}

	/**
		Return a byte array of the specified file.
		If _file hasn't been mapped, then it will 
		be mapped, before loading the bytes.
	*/
	public byte[] getResourceRaw( final String _file )
	{
		final DataFile file = resources.get( _file ) ;
		if( file != null )
		{
			if( file.isZipped == false )
			{
				return ReadFile.getRaw( file.filePath ) ;
			}
			else if( file.isZipped == true )
			{
				return ReadZip.getRaw( file.filePath, file.zipPath ) ;
			}
		}

		return attemptMapResource( _file ) ;
	}

	/**
		Return a String of the specified file.
		If _file hasn't been mapped, then it will 
		be mapped, before loading the bytes.
	*/
	public String getResourceAsString( final String _file )
	{
		final DataFile file = resources.get( _file ) ;
		if( file != null )
		{
			if( file.isZipped == false )
			{
				return ReadFile.getString( file.filePath ) ;
			}
			else if( file.isZipped == true )
			{
				return ReadZip.getString( file.filePath, file.zipPath ) ;
			}
		}

		final byte[] data = attemptMapResource( _file ) ; 
		return ( data != null ) ? new String( data ) : null ;
	}

	public boolean getResourceRaw( final String _file, final int _length, final ResourceCallback _callback )
	{
		final DataFile file = resources.get( _file ) ;
		if( file != null )
		{
			if( file.isZipped == false )
			{
				return ReadFile.getRaw( _file, _length, _callback ) ;
			}
			else if( file.isZipped == true )
			{
				return ReadZip.getRaw( file.filePath, file.zipPath, _length, _callback ) ;
			}
		}

		Logger.println( "File not found.", Logger.Verbosity.MAJOR ) ;
		return false ;
	}

	public boolean getResourceAsString( final String _file, final int _length, final ResourceCallback _callback )
	{
		final DataFile file = resources.get( _file ) ;
		if( file != null )
		{
			if( file.isZipped == false )
			{
				return ReadFile.getString( _file, _length, _callback ) ;
			}
			else if( file.isZipped == true )
			{
				return ReadZip.getString( file.filePath, file.zipPath, _length, _callback ) ;
			}
		}

		Logger.println( "File not found.", Logger.Verbosity.MAJOR ) ;
		return false ;
	}

	private byte[] attemptMapResource( final String _file )
	{
		final File createFile = new File( _file ) ;
		if( createFile.isFile() == true )
		{
			mapToResources( createFile ) ;
			return getResourceRaw( _file ) ;
		}

		System.out.println( "Failed to Find Resource: " + _file ) ;
		return null ;
	}

	public boolean writeResourceAsString( final String _file, final String _data )
	{
		try
		{
			write( _file, _data ) ;
		}
		catch( IOException _ex )
		{
			return false ;
		}

		return true ;
	}

	/**
		Write the byte stream to the spcified file location.
		Does not support writing byte streams to a zip container.
	*/
	public boolean writeResourceRaw( final String _file, final byte[] _data )
	{
		try
		{
			write( _file, _data ) ;
		}
		catch( IOException _ex )
		{
			return false ;
		}

		return true ;
	}

	/**
		Check to see whether the path specified exists.
		Does not check paths within archieved files.
	*/
	public boolean exist( final String _path )
	{
		return new File( _path ).exists() ;
	}

	/**
		Delete a resource that has been mapped to 
		the local filesystem. Does not delete resources 
		contained within an archive.
	*/
	public boolean delete( final String _path )
	{
		// Only delete Resources that are mapped
		if( resources.containsKey( _path ) == true )
		{
			DataFile data = resources.get( _path ) ;
			if( data.zipPath == null )				// Don't delete if contained witihin Zip
			{
				final File file = new File( data.filePath ) ;
				resources.remove( _path ) ;
				return file.delete() ;
			}
		}

		return false ;
	}

	public boolean makeDirectories( final String _path )
	{
		return new File( _path ).mkdirs() ;
	}

	public boolean isFile( final String _file )
	{
		return new File( _file ).isFile() ;
	}

	public boolean isDirectory( final String _path )
	{
		return new File( _path ).isDirectory() ;
	}

	private void mapToResources( final File _file )
	{
		final String path = _file.getPath() ;
		final String key = path.replace( File.separatorChar, '/' ) ;	// Games rely on '/' separator

		resources.put( key, new DataFile( path, null, false ) ) ;
	}

	private void mapZipToResources( final File _file )
	{
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
						final String path = entry.getName() ;
						final DataFile data = new DataFile( _file.getPath(), path, true ) ;
						resources.put( ROOT_DIRECTORY + File.separator + path, data ) ;
					}
				}
			}

			zipFile.close() ;
		}
		catch( ZipException _ex ) {}
		catch( IOException _ex ) {}
	}

	/**
		TODO: Needs to be improved to handle random '.' locations.
		Returns the extension in UpperCase.
	**/
	public static String getExtension( final File _file )
	{
		final String name = _file.getName() ;
		final int pos = name.lastIndexOf( "." ) ;
		return name.substring( pos + 1 ).toUpperCase() ;
	}

	public static boolean isZipFile( final File _file )
	{
		return getExtension( _file ).equals( "ZIP" ) ;
	}

	private static void write( final String _file, final String _data ) throws IOException
	{
		final FileWriter stream = new FileWriter( _file ) ;
		final BufferedWriter out = new BufferedWriter( stream ) ;
		out.write( _data ) ;
		out.close() ;
	}

	private static void write( final String _file, final byte[] _data ) throws IOException
	{
		final FileOutputStream stream = new FileOutputStream( _file ) ;
		stream.write( _data ) ;
		stream.flush() ;
		stream.close() ;
	}

	private class DataFile
	{
		public final boolean isZipped ;
		public final String filePath ;
		public final String zipPath ;

		DataFile( final String _filePath, final String _zipPath, final boolean _isZipped )
		{
			isZipped = _isZipped ;
			filePath = _filePath ;
			zipPath = _zipPath ;
		}
	}
}