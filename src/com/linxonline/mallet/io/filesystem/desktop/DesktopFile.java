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
			return ( DesktopByteIn )toClose.add( new DesktopByteIn( new FileInputStream( file ) )) ;
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
			return ( DesktopStringIn )toClose.add( new DesktopStringIn( new FileInputStream( file ) ) ) ;
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

	public boolean getStringInCallback( final StringInCallback _callback, final int _length )
	{
		return ReadFile.getString( getStringInStream(), _callback, _length ) ;
	}

	public ByteOutStream getByteOutStream()
	{
		try
		{
			return ( DesktopByteOut )toClose.add( new DesktopByteOut( new FileOutputStream( file ) ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public StringOutStream getStringOutStream()
	{
		try
		{
			return ( DesktopStringOut )toClose.add( new DesktopStringOut( new BufferedWriter( new FileWriter( file ) ) ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
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
			System.out.println( "Failed to create directories." ) ;
			return false ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _dest ) ;
		if( stream == null )
		{
			System.out.println( "Unable to acquire file stream for: " + _dest ) ;
			return false ;
		}

		final ByteInStream in = getByteInStream() ;
		final ByteOutStream out = stream.getByteOutStream() ;
		if( out == null )
		{
			return null ;
		}

		int length = 0 ;
		final byte[] buffer = new byte[48] ;

		while( ( length = in.readBytes( buffer, 0, buffer.length ) ) != -1 )
		{
			out.writeBytes( buffer, 0, length ) ;
		}

		in.close() ;
		out.close() ;

		return true ;
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
