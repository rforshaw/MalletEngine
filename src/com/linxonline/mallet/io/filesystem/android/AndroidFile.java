package com.linxonline.mallet.io.filesystem.android ;

import java.io.File ;
import java.io.FileWriter ;
import java.io.BufferedWriter ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.FileNotFoundException ;

import android.os.Environment ;
import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;

public class AndroidFile implements FileStream
{
	public enum StorageType
	{
		Internal,
		External
	}

	private final static String EXTERNAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + '/' ;

	private final File file ;

	public AndroidFile( final String _file, final StorageType _type )
	{
		assert _file != null ;
		switch( _type )
		{
			case Internal : file = new File( _file ) ;                 break ;
			case External :
			default       : file = new File( EXTERNAL_PATH + _file ) ; break ;
		}
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			return new AndroidByteIn( new FileInputStream( file ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public StringInStream getStringInStream()
	{
		try
		{
			return new AndroidStringIn( new FileInputStream( file ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			ex.printStackTrace() ;
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
			return new AndroidByteOut( new FileOutputStream( file ) ) ;
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
			return new AndroidStringOut( new BufferedWriter( new FileWriter( file ) ) ) ;
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

		while( ( length = in.readBytes( buffer, 0, buffer.length ) ) > 0 )
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

	@Override
	public String toString()
	{
		return file.toString() ;
	}
}
