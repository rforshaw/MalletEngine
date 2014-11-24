package com.linxonline.mallet.io.filesystem.android ;

import java.io.InputStream ;
import java.io.IOException ;
import android.content.res.AssetManager ;

import com.linxonline.mallet.io.filesystem.* ;

public class AndroidFile implements FileStream
{
	private final CloseStreams toClose = new CloseStreams() ;
	private final String file ;

	public AndroidFile( final String _file )
	{
		assert _file != null ;
		file = _file ;
	}

	public ByteInStream getByteInStream()
	{
		/*try
		{
			return null ;//new AndroidByteIn( asset.open( file ) ) ;
		}
		catch( IOException ex )
		{
			return null ;
		}*/
		return null ;
	}

	public StringInStream getStringInStream()
	{
		/*try
		{
			return null ;//new AndroidStringIn( asset.open( file ) ) ;
		}
		catch( IOException ex )
		{
			return null ;
		}*/
		return null ;
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
		/*try
		{
			return new AndroidByteOut( new FileOutputStream( file ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}*/
		return null ;
	}

	public StringOutStream getStringOutStream()
	{
		/*try
		{
			return new AndroidStringOut( new BufferedWriter( new FileWriter( file ) ) ) ;
		}
		catch( FileNotFoundException ex )
		{
			return null ;
		}
		catch( IOException ex )
		{
			return null ;
		}*/
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
		return false ;
	}

	public boolean isDirectory()
	{
		return false ;
	}

	public boolean exists()
	{
		return false ;
	}

	/**
		Delete the File repreented by this File Stream.
		This also includes deleting folders.
	*/
	public boolean delete()
	{
		return false ;//deleteRecursive( file ) ;
	}

	/*private static boolean deleteRecursive( final File _file )
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
	}*/

	/**
		Create the Directory structure represented 
		by this File Stream.
	*/
	public boolean mkdirs()
	{
		return false ;//file.mkdirs() ;
	}

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		/*try
		{
			final InputStream stream = asset.open( file ) ;
			final int length = stream.available() ;
			stream.close() ;
			return length ;
		}
		catch( IOException ex )
		{
			return 0L ;
		}*/
		return 0L ;
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