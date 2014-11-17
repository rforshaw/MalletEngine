package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.FileWriter ;
import java.io.BufferedWriter ;
import java.io.FileNotFoundException ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.* ;

public class DesktopFile implements FileStream
{
	private final CloseStreams toClose = new CloseStreams() ;
	private final File file ;

	public DesktopFile( final File _file )
	{
		assert _file != null ;
		file = _file ;
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			final ByteInStream stream = new DesktopByteIn( new FileInputStream( file ) ) ;
			toClose.add( stream ) ;
			return stream ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}
	}

	public StringInStream getStringInStream()
	{
		try
		{
			final StringInStream stream = new DesktopStringIn( new FileInputStream( file ) ) ;
			toClose.add( stream ) ;
			return stream ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}
	}

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length )
	{
		return ReadFile.getRaw( getByteInStream(), _callback, _length ) ;
	}

	public boolean getByteInCallback( final StringInCallback _callback, final int _length )
	{
		return ReadFile.getString( getStringInStream(), _callback, _length ) ;
	}

	public ByteOutStream getByteOutStream()
	{
		try
		{
			final ByteOutStream stream = new DesktopByteOut( new FileOutputStream( file ) ) ;
			toClose.add( stream ) ;
			return stream ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}
	}

	public StringOutStream getStringOutStream()
	{
		try
		{
			final StringOutStream stream = new DesktopStringOut( new BufferedWriter( new FileWriter( file ) ) ) ;
			toClose.add( stream ) ;
			return stream ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}
		catch( IOException ex )
		{
			return null ;
		}
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
		return file.isFile() ;
	}

	public boolean isDirectory()
	{
		return file.isDirectory() ;
	}

	public boolean exists()
	{
		return file.exists() ;
	}

	/**
		Delete the File repreented by this File Stream.
		This also includes deleting folders.
	*/
	public boolean delete()
	{
		return deleteRecursive( file ) ;
	}

	private static boolean deleteRecursive( final File _file )
	{
		if( _file.exists() == false )
		{
			return false ;
		}

		boolean ret = true ;
		if( _file.isDirectory() == true )
		{
			for( final File file : _file.listFiles() )
			{
				ret = ret && deleteRecursive( file ) ;
			}
		}

		return ret && _file.delete();
	}
	
	/**
		Create the Directory structure represented 
		by this File Stream.
	*/
	public boolean mkdirs()
	{
		return file.mkdirs() ;
	}

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return file.length() ;
	}

	/**
		Close all the stream input/output that has 
		been returned and close them.
	*/
	public boolean close()
	{
		return toClose.close() ;
	}
}