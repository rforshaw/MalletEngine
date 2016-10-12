package com.linxonline.mallet.io.filesystem.android ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;

public class AndroidZipFile implements FileStream
{
	private final CloseStreams toClose = new CloseStreams() ;
	private final ZipFile zipFile ;
	private final ZipEntry zipEntry ;

	public AndroidZipFile( final AndroidFileSystem.ZipPath _path ) throws IOException
	{
		assert _path != null ;
		zipFile = new ZipFile( _path.getZipPath() ) ;
		zipEntry = zipFile.getEntry( _path.filePath ) ;
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			return ( AndroidByteIn )toClose.add( new AndroidByteIn( zipFile.getInputStream( zipEntry ) )
			{
				public boolean close()
				{
					final boolean success = super.close() ;
					try
					{
						zipFile.close() ;
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
		catch( IOException ex )
		{
			ex.printStackTrace() ;
			return null ;
		}
	}

	public StringInStream getStringInStream()
	{
		try
		{
			return ( AndroidStringIn )toClose.add( new AndroidStringIn( zipFile.getInputStream( zipEntry ) )
			{
				public boolean close()
				{
					final boolean success = super.close() ;
					try
					{
						zipFile.close() ;
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
		catch( IOException ex )
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
}
