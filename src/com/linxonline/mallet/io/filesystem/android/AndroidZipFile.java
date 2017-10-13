package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;

public class AndroidZipFile implements FileStream
{
	private final CloseStreams toClose = new CloseStreams() ;
	private final ZipInputStream stream ;
	private final ZipEntry zipEntry ;

	public AndroidZipFile( final AndroidFileSystem.ZipPath _path, final AssetManager _asset ) throws IOException
	{
		assert _path != null ;

		stream = new ZipInputStream( _asset.open( _path.getZipPath() ) ) ;
		zipEntry = AndroidZipFile.getZipEntry( _path.filePath, stream ) ;
	}

	public ByteInStream getByteInStream()
	{
		return ( AndroidByteIn )toClose.add( new AndroidByteIn( stream )
		{
			public boolean close()
			{
				final boolean success = super.close() ;
				try
				{
					stream.close() ;
					return success ;
				}
				catch( IOException ex )
				{
					ex.printStackTrace() ;
					return false ;
				}
			}
		} ) ;
	}

	public StringInStream getStringInStream()
	{
		return ( AndroidStringIn )toClose.add( new AndroidStringIn( stream )
		{
			public boolean close()
			{
				final boolean success = super.close() ;
				try
				{
					stream.close() ;
					return success ;
				}
				catch( IOException ex )
				{
					ex.printStackTrace() ;
					return false ;
				}
			}
		} ) ;
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
		Copy the File Stream to the requested location.
		This should only work if the File Stream is a file.
	*/
	public boolean copyTo( final String _dest )
	{
		return false ;
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

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return zipEntry.getSize() ;
	}

	/**
		Close all the stream input/output that has 
		been returned and close them.
	*/
	public boolean close()
	{
		return toClose.close() ;
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
