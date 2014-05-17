package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;

public class DesktopFileSystem implements FileSystem
{
	private HashMap<String, DataFile> resources = new HashMap<String, DataFile>() ;
	private static String ROOT_DIRECTORY = "base" ;

	public DesktopFileSystem() {}

	/**
		Scans the ROOT_DIRECTORY directory and maps it to the HashMap.
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
	**/
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

		return new String( attemptMapResource( _file ) ) ;
	}

	public boolean getResourceRaw( final String _file, final int _length, final ResourceCallback _callback )
	{
		return false ;
	}

	public boolean getResourceAsString( final String _file, final int _length, final ResourceCallback _callback )
	{
		return ReadFile.getString( _file, _length, _callback ) ;
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

	public boolean writeResourceRaw( final String _file, final byte[] _data )
	{
		return false ;
	}

	public boolean doesResourceExist( final String _file )
	{
		return false ;
	}
	
	public boolean deleteResource( final String _file )
	{
		// Only delete Resources that are mapped
		if( resources.containsKey( _file ) == true )
		{
			DataFile data = resources.get( _file ) ;
			if( data.zipPath == null )				// Don't delete if contained witihin Zip
			{
				final File file = new File( data.filePath ) ;
				resources.remove( _file ) ;
				return file.delete() ;
			}
		}

		return false ;
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
	
	private static byte[] streamBytes( final DataFile _file )
	{
		if( _file.isZipped == false )
		{
			return ReadFile.getRaw( _file.filePath ) ;
		}
		else if( _file.isZipped == true )
		{
			return ReadZip.getRaw( _file.filePath, _file.zipPath ) ;
		}

		System.out.println( "File Not Found" ) ;
		return null ;
	}

	private static void write( final String _file, final String _data ) throws IOException
	{
		final FileWriter stream = new FileWriter( _file ) ;
		final BufferedWriter out = new BufferedWriter( stream ) ;
		out.write( _data ) ;
		out.close() ;
	}

	private static byte[] read( final InputStream _stream, final long _length ) throws IOException
	{
		final byte[] bytes = new byte[( int )_length] ;

		int offset = 0 ;
		int numRead = 0 ;
		while( offset < bytes.length &&
			( numRead = _stream.read( bytes, offset, bytes.length - offset ) ) >= 0 ) 
		{
			offset += numRead;
		}
		
		_stream.close() ;
		return bytes ;
	}
	
	private class DataFile
	{
		boolean isZipped = false ;
		String filePath ;
		String zipPath ;

		DataFile( final String _filePath, final String _zipPath, final boolean _isZipped )
		{
			isZipped = _isZipped ;
			filePath = _filePath ;
			zipPath = _zipPath ;
		}
	}
}