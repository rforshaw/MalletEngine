package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;

public class AndroidZipFile implements FileStream
{
	private final String path ;
	private final List<AndroidFileSystem.ZipPath> zips ;
	private final ZipInputStream[] streams ;
	private final ZipEntry[] zipEntries ;

	public AndroidZipFile( final String _path, final List<AndroidFileSystem.ZipPath> _zips, final AssetManager _asset ) throws IOException
	{
		path = _path ;
		zips = _zips ;

		// It's possible that there are multiple zip files
		// that each override the same file or directory.
		// We are only interested in multiple versions of a directory.
		final int size = ( _zips.get( 0 ).isDirectory ) ? _zips.size() : 1 ;

		streams = new ZipInputStream[size] ;
		zipEntries = new ZipEntry[size] ;

		for( int i = 0; i < size; ++i )
		{
			final AndroidFileSystem.ZipPath zip = _zips.get( i ) ;
			streams[i] = new ZipInputStream( _asset.open( zip.getZipPath() ) ) ;
			zipEntries[i] = AndroidZipFile.getZipEntry( zip.filePath, streams[i] ) ;
		}
	}

	public ByteInStream getByteInStream()
	{
		return new AndroidByteIn( streams[0] )
		{
			@Override
			public void close() throws Exception
			{
				super.close() ;
				streams[0].close() ;
			}
		} ;
	}

	public StringInStream getStringInStream()
	{
		return new AndroidStringIn( streams[0] )
		{
			@Override
			public void close() throws Exception
			{
				super.close() ;
				streams[0].close() ;
			}
		} ;
	}

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length )
	{
		return ReadFile.getRaw( getByteInStream(), _callback, _length ) ;
	}

	public boolean getStringInCallback( final StringInCallback _callback, final int _length )
	{
		return ReadFile.getString( getStringInStream(), _callback, _length ) ;
	}

	public ByteOutStream getByteOutStream()
	{
		return null ;
	}

	public StringOutStream getStringOutStream()
	{
		return null ;
	}

	/**
		Cannot create new files into a zip file.
	*/
	public boolean create()
	{
		return false ;
	}

	/**
		Copy the File Stream to the requested location.
		This should only work if the File Stream is a file.
	*/
	public boolean copyTo( final String _dest )
	{
		final FileStream destination = GlobalFileSystem.getFile( new File( _dest ).getParent() ) ;
		if( destination.exists() == false && destination.mkdirs() == false )
		{
			System.out.println( "Failed to create directories." ) ;
			return false ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _dest ) ;
		if( stream == null )
		{
			System.out.println( "Unable to acquire file stream for: " + _dest ) ;
			return false ;
		}

		try( final ByteInStream in = getByteInStream() ;
			 final ByteOutStream out = stream.getByteOutStream() )
		{
			if( out == null )
			{
				return false ;
			}

			int length = 0 ;
			final byte[] buffer = new byte[48] ;

			while( ( length = in.readBytes( buffer, 0, buffer.length ) ) > 0 )
			{
				out.writeBytes( buffer, 0, length ) ;
			}

			return true ;
		}
		catch( Exception ex )
		{
			return false ;
		}
	}

	public boolean isFile()
	{
		return !isDirectory() ;
	}

	public boolean isDirectory()
	{
		return zipEntries[0].isDirectory() ;
	}

	/**
		So long as the FileStream is not a directory it must 
		be readable - if we can read the zip but the file inside 
		is not readable then something has went horribly wrong.
	*/
	public boolean isReadable()
	{
		return isDirectory() == false ;
	}

	/**
		If the file is located within a zip it is not writable.
		getByteOutStream and getStringOutStream will return a null 
		stream reference.
	*/
	public boolean isWritable()
	{
		return false ;
	}

	public boolean exists()
	{
		return true ;
	}

	/**
		Delete the File represented by this File Stream.
		This also includes deleting folders.
	*/
	public boolean delete()
	{
		return false ;
	}

	/**
		Create the Directory structure represented 
		by this File Stream.
	*/
	public boolean mkdirs()
	{
		return false ;
	}

	public String[] list()
	{
		throw new RuntimeException( "Android Zip File does not implement list()." ) ;
	}

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return zipEntries[0].getSize() ;
	}

	@Override
	public String toString()
	{
		return zips.get( 0 ).toString() ;
	}

	/**
		Return the ZipEntry of the passed in _filePath.
		Shift the stream to the beginning of this zip.
	*/
	private static ZipEntry getZipEntry( final String _filePath, final ZipInputStream _stream ) throws IOException
	{
		ZipEntry entry = null ;
		while( ( entry = _stream.getNextEntry() ) != null )
		{
			if( entry.isDirectory() == false )
			{
				if( _filePath.equals( entry.getName() ) == true )
				{
					return entry ;
				}
			}
		}

		return null ;
	}
}
