package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.FileWriter ;
import java.io.BufferedWriter ;
import java.io.FileNotFoundException ;
import java.io.IOException ;

import com.linxonline.mallet.io.filesystem.* ;

public final class DesktopFile implements FileStream
{
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
			return new DesktopByteIn( new FileInputStream( file ) ) ;
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
			return new DesktopStringIn( new FileInputStream( file ) ) ;
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
			return new DesktopByteOut( new FileOutputStream( file ) ) ;
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
			return new DesktopStringOut( new BufferedWriter( new FileWriter( file ) ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	/**
		Create a new file if the file does not already exist.
	*/
	public boolean create()
	{
		try
		{
			return file.createNewFile() ;
		}
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return false ;
		}
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
		return file.isFile() ;
	}

	public boolean isDirectory()
	{
		return file.isDirectory() ;
	}

	public boolean isReadable()
	{
		return file.canRead() ;
	}

	public boolean isWritable()
	{
		return file.canWrite() ;
	}

	public boolean exists()
	{
		return file.exists() ;
	}

	/**
		Delete the File represented by this File Stream.
		This also includes deleting folders.
	*/
	public boolean delete()
	{
		return deleteRecursive( file ) ;
	}

	private static boolean deleteRecursive( final File _file )
	{
		if( _file == null )
		{
			return false ;
		}

		if( _file.exists() == false )
		{
			return false ;
		}

		boolean ret = true ;
		if( _file.isDirectory() == true )
		{
			final File[] files = _file.listFiles() ;
			if( files != null )
			{
				for( final File file : files )
				{
					ret = ret && deleteRecursive( file ) ;
				}
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

	public String[] list()
	{
		return file.list() ;
	}

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return file.length() ;
	}

	@Override
	public String toString()
	{
		return file.toString() ;
	}
}
