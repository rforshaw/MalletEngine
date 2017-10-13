package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;

public class DesktopZipFile implements FileStream
{
	private CloseStreams toClose = new CloseStreams() ;
	private final ZipFile zipFile ;
	private final ZipEntry zipEntry ;

	public DesktopZipFile( final DesktopFileSystem.ZipPath _path ) throws IOException
	{
		assert _path != null ;
		zipFile = new ZipFile( _path.getZipPath() ) ;
		zipEntry = zipFile.getEntry( _path.filePath ) ;
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			final ByteInStream stream = new DesktopByteIn( zipFile.getInputStream( zipEntry ) )
			{
				public boolean close()
				{
					boolean success = super.close() ;
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
			} ;

			toClose.add( stream ) ;
			return stream ;
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
			final StringInStream stream = new DesktopStringIn( zipFile.getInputStream( zipEntry ) )
			{
				public boolean close()
				{
					boolean success = super.close() ;
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
			} ;

			toClose.add( stream ) ;
			return stream ;
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

		in.close() ;
		out.close() ;

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

	public boolean close()
	{
		return toClose.close() ;
	}
}
