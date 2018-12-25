package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;

public class AndroidZipFile implements FileStream
{
	private final CloseStreams toClose = new CloseStreams() ;
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
		return ( AndroidByteIn )toClose.add( new AndroidByteIn( stream )
		{
			@Override
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
			@Override
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

		final ByteInStream in = getByteInStream() ;
		final ByteOutStream out = stream.getByteOutStream() ;
		if( out == null )
		{
			return false ;
		}

		int length = 0 ;
		final byte[] buffer = new byte[48] ;

		while( ( length = in.readBytes( buffer, 0, buffer.length ) ) != -1 )
		{
			out.writeBytes( buffer, 0, length ) ;
		}

		close( in ) ;
		stream.close() ;

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

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return zipEntry.getSize() ;
	}

	/**
		Close a specific stream without closing other 
		active streams.
	*/
	public boolean close( final Close _close )
	{
		return toClose.remove( _close ) ;
	}

	/**
		Close all the stream input/output that has 
		been returned and close them.
	*/
	public boolean close()
	{
		return toClose.close() ;
	}

	@Override
	public String toString()
	{
		return path.toString() ;
	}

	/**
		There is a chance that a user may access resources 
		and forget to close them.
		To ensure that they are at least closed at some point 
		within the life cycle of the application we will log 
		an error and close them on object finalize().
	*/
	@Override
	protected void finalize() throws Throwable
	{
		if( toClose.isEmpty() == false )
		{
			Logger.println( this + " accessed without closing.", Logger.Verbosity.MAJOR ) ;
			close() ;
		}
		super.finalize() ;
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
