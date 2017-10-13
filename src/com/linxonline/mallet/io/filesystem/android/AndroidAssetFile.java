package com.linxonline.mallet.io.filesystem.android ;

import java.io.File ;
import java.io.InputStream ;
import java.io.IOException ;
import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;

public class AndroidAssetFile implements FileStream
{
	private final CloseStreams toClose = new CloseStreams() ;
	private final String file ;
	private final AssetManager asset ;

	public AndroidAssetFile( final String _file, final AssetManager _asset )
	{
		assert _file != null ;
		assert _asset != null ;

		file = _file ;
		asset = _asset ;
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			return ( ByteInStream )toClose.add( new AndroidByteIn( asset.open( file ) ) ) ;
		}
		catch( IOException ex )
		{
			return null ;
		}
	}

	public StringInStream getStringInStream()
	{
		try
		{
			return ( StringInStream )toClose.add( new AndroidStringIn( asset.open( file ) ) ) ;
		}
		catch( IOException ex )
		{
			return null ;
		}
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
		return true ;
	}

	public boolean isDirectory()
	{
		return false ;
	}

	public boolean isReadable()
	{
		return true ;
	}

	public boolean isWritable()
	{
		return false ;
	}

	public boolean exists()
	{
		return true ;
	}

	/**
		Can't delete a file located within the 
		AssetManager. It is read only.
	*/
	public boolean delete()
	{
		return false ;
	}

	/**
		Cannot create directories within the 
		AssetManager. It is read only.
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
		try
		{
			final InputStream stream = asset.open( file ) ;
			final int length = stream.available() ;
			stream.close() ;
			return length ;
		}
		catch( IOException ex )
		{
			return 0L ;
		}
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
		return file ;
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
}
