package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;

public class AndroidZipFile implements FileStream
{
	private final AndroidFileSystem.ZipPath path ;
	private final ZipInputStream stream ;
	private final ZipEntry zipEntry ;

	public AndroidZipFile( final AndroidFileSystem.ZipPath _path, final AssetManager _asset ) throws IOException
	{
		assert _path != null ;

		path = _path ;
		stream = new ZipInputStream( _asset.open( path.getZipPath() ) ) ;
		zipEntry = AndroidZipFile.getZipEntry( path.filePath, stream ) ;
	}

	public ByteInStream getByteInStream()
	{
		return new AndroidByteIn( stream )
		{
			@Override
			public void close() throws Exception
			{
				super.close() ;
				stream.close() ;
			}
		} ;
	}

	public StringInStream getStringInStream()
	{
		return new AndroidStringIn( stream )
		{
			@Override
			public void close() throws Exception
			{
				super.close() ;
				stream.close() ;
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
		if( destination.mkdirs() == false )
		{
			return false ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _dest ) ;
		if( stream == null )
		{
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
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
			return false ;
		}

		return true ;
	}

	public boolean isFile()
	{
		return !zipEntry.isDirectory() ;
	}

	public boolean isDirectory()
	{
		return zipEntry.isDirectory() ;
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
		Delete the File repreented by this File Stream.
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
		return zipEntry.getSize() ;
	}

	@Override
	public String toString()
	{
		return path.toString() ;
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
