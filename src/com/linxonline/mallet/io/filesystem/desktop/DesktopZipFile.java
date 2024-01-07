package com.linxonline.mallet.io.filesystem.desktop ;

import java.io.* ;
import java.util.zip.* ;
import java.util.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;

public class DesktopZipFile implements FileStream
{
	private final String path ;
	private final ZipFile[] zipFiles ;
	private final ZipEntry[] zipEntries ;

	public DesktopZipFile( final String _path, final List<DesktopFileSystem.ZipPath> _zips ) throws IOException
	{
		path = _path ;

		// It's possible that there are multiple zip files
		// that each override the same file or directory.
		// We are only interested in multiple versions of a directory.
		final int size = ( _zips.get( 0 ).isDirectory ) ? _zips.size() : 1 ;

		zipFiles = new ZipFile[size] ;
		zipEntries = new ZipEntry[size] ;

		for( int i = 0; i < size; ++i )
		{
			final DesktopFileSystem.ZipPath zip = _zips.get( i ) ;
			zipFiles[i] = new ZipFile( zip.getZipPath() ) ;
			zipEntries[i] = zipFiles[i].getEntry( zip.filePath ) ;
		}
	}

	public ByteInStream getByteInStream()
	{
		try
		{
			return new DesktopByteIn( zipFiles[0].getInputStream( zipEntries[0] ) )
			{
				@Override
				public void close() throws Exception
				{
					super.close() ;
					zipFiles[0].close() ;
				}
			} ;
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
			return new DesktopStringIn( zipFiles[0].getInputStream( zipEntries[0] ) )
			{
				@Override
				public void close() throws Exception
				{
					super.close() ;
					zipFiles[0].close() ;
				}
			} ;
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
		return !isDirectory() ;
	}

	public boolean isDirectory()
	{
		return zipEntries[0].isDirectory() ;
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

	public String[] list()
	{
		final HashSet<String> names = new HashSet<String>() ;

		for( final ZipEntry zipEntry : zipEntries )
		{
			final String currentPath = zipEntry.getName() ;

			for( final ZipFile zipFile : zipFiles )
			{
				final Enumeration<? extends ZipEntry> entries = zipFile.entries() ;
				while( entries.hasMoreElements() == true )
				{
					final ZipEntry entry = entries.nextElement() ;
					final String name = entry.getName() ;
					if( name.equals( currentPath ) == true )
					{
						// Skip you can't be a directory or a file
						// of yourself.
						continue ;
					}

					if( name.startsWith( currentPath ) == false )
					{
						// We are only interested in paths that follow
						// the same hierarchy as our current path.
						continue ;
					}

					final int firstIndex = name.indexOf( '/', currentPath.length() ) ;
					final int lastIndex = name.lastIndexOf( '/' ) ;

					if( firstIndex == -1 )
					{
						// It's a file
						names.add( name.substring( currentPath.length(), name.length() ) ) ;
						continue ;
					}

					if( firstIndex == lastIndex &&
						entry.isDirectory() == true )
					{
						// It's a child directory of the current path.
						// We aren't interested in grand children.
						names.add( name.substring( currentPath.length(), name.length() - 1 ) ) ;
						continue ;
					}
				}
			}
		}

		final DesktopFile file = new DesktopFile( new File( path ) ) ;
		if( file.exists() && file.isDirectory() )
		{
			for( final String path : file.list() )
			{
				names.add( path ) ;
			}
		}

		return names.toArray( new String[0] ) ;
	}

	/**
		Return the File size of this FileStream.
	*/
	public long getSize()
	{
		return zipEntries[0].getSize() ;
	}

	@Override
	public String toString()
	{
		return zipFiles[0].toString() ;
	}
}
