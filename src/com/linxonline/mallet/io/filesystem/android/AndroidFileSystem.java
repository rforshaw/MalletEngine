package com.linxonline.mallet.io.filesystem.android ;

import java.util.* ;
import java.util.zip.* ;
import java.io.* ;

import android.content.Context ;
import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.android.* ;

public class AndroidFileSystem implements FileSystem
{
	private Context context = null ;
	private AssetManager assetManager = null ;
	private HashMap<String, DataFile> resources = new HashMap<String, DataFile>() ;
	private static String ROOT_DIRECTORY = "base" ;

	public AndroidFileSystem( final Context _context )
	{
		init( _context ) ;
	}

	public void init( final Context _context )
	{
		context = _context ;
		assetManager = context.getAssets() ;
		initJSONConstructors(); 
	}

	protected void initJSONConstructors()
	{
		AndroidJSONObject.init() ;
		AndroidJSONArray.init() ;
	}

	@Override
	public void scanBaseDirectory()
	{
		if( context == null )
		{
			System.out.println( "Failed TO ACQUIRE CONTEXT" ) ;
			return ;
		}

		traverseFiles( ROOT_DIRECTORY ) ;
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
				if( isZipFile( _path ) == true )
				{
					mapZipToResources( _path ) ;
				}
				else
				{
					resources.put( _path, new DataFile( _path ) ) ;
				}
			}
		}
		catch( IOException _ex )
		{
			_ex.printStackTrace() ;
		}
	}

	@Override
	public byte[] getResourceRaw( final String _file )
	{
		if( resources.containsKey( _file ) == true )
		{
			return streamBytes( resources.get( _file ) ) ;
		}

		return null ;
	}

	@Override
	public String getResourceAsString( final String _file )
	{
		final byte[] resource = getResourceRaw( _file ) ;
		if( resource != null )
		{
			return new String( resource ) ;
		}

		return null ;
	}

	/**
		Doesn't block calling Thread, when resource has started reading,
		it'll call ResourceCallback.
		Return boolean informs whether it successfully started reading.

		Using a length of zero will result in the entire file/stream being returned.
		Specifying a length greater than zero will determine the maximum amount of bytes/lines
		that are read and then passed to the callback. If the end of the stream/file does not reach
		the length size then it is still returned.
	*/
	@Override
	public boolean getResourceRaw( final String _file, final int _length, final ResourceCallback _callback )
	{
		return false ;
	}

	@Override
	public boolean getResourceAsString( final String _file, final int _length, final ResourceCallback _callback )
	{
		return false ;
	}

	/**
		Blocks calling Thread
	**/
	@Override
	public boolean writeResourceAsString( final String _file, final String _data )
	{
		return false ;
	}

	@Override
	public boolean writeResourceRaw( final String _file, final byte[] _data )
	{
		return false ;
	}

	@Override
	public boolean exist( final String _file )
	{
		return false ;
	}

	@Override
	public boolean delete( final String _file )
	{
		return false ;
	}

	/**
		Create the directory structure represented by _path.
	*/
	public boolean makeDirectories( final String _path )
	{
		return false ;
	}

	public boolean isFile( final String _file )
	{
		return false ;
	}

	public boolean isDirectory( final String _path )
	{
		return false ;
	}

	/**
		TODO: Needs to be improved to handle random '.' locations.
	**/
	public static String getExtension( final String _file )
	{
		final int pos = _file.lastIndexOf( "." ) ;
		return _file.substring( pos + 1 ).toUpperCase() ;
	}

	public static boolean isZipFile( final String _file )
	{
		return getExtension( _file ).equals( "ZIP" ) ;
	}

	private byte[] streamBytes( final DataFile _file )
	{
		try
		{
			if( _file.zipPath != null )
			{
				final InputStream is = assetManager.open( _file.filePath ) ;
				final ZipInputStream zis = new ZipInputStream( is ) ;

				ZipEntry entry = null ;
				while( ( entry = zis.getNextEntry() ) != null )
				{
					final String zipPath = _file.zipPath ;
					if( zipPath.equals( entry.getName() ) == true )
					{
						final int length = ( int )entry.getSize() ;
						return read( zis, length ) ;
					}
				}
			}
		}
		catch( ZipException _ex )
		{
			System.out.println( "Failed to access Zip" ) ;
		}
		catch( IOException _ex )
		{
			System.out.println( "Failed to read Zip" ) ;
		}

		try
		{
			final String path = _file.filePath ;
			final InputStream is = assetManager.open( path ) ;
			return read( is, is.available() ) ;
		}
		catch( IOException _ex )
		{
			_ex.printStackTrace() ;
		}

		System.out.println( "File Not Found" ) ;
		return null ;
	}

	private static byte[] read( final InputStream _stream, final int _length ) throws IOException
	{
		final byte[] bytes = new byte[_length] ;

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

	private void mapZipToResources( final String _file )
	{
		try
		{
			final InputStream is = assetManager.open( _file ) ;
			final ZipInputStream zis = new ZipInputStream( is ) ;

			ZipEntry entry = null ;
			while( ( entry = zis.getNextEntry() ) != null )
			{
				if( entry.isDirectory() == false )
				{
					final String path = entry.getName() ;
					final String basePath = ROOT_DIRECTORY + File.separator + path ;
					final DataFile data = new DataFile( _file, path ) ;
					resources.put( basePath, data ) ;
				}
			}

			zis.close() ;
			is.close() ;
		}
		catch( ZipException _ex ) {}
		catch( IOException _ex ) {}
	}

	private class DataFile
	{
		String filePath = null ;
		String zipPath = null ;

		DataFile( final String _filePath, final String _zipPath )
		{
			filePath = _filePath ;
			zipPath = _zipPath ;
		}

		DataFile( final String _filePath )
		{
			filePath = _filePath ;
		}
	}
}