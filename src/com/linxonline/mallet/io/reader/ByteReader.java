package com.linxonline.mallet.io.reader ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.ByteInStream ;
import com.linxonline.mallet.io.filesystem.FileStream ;

/**
	Convience methods to read byte streams.
**/
public class ByteReader
{
	private ByteReader() {}

	/**
		Blocking, returns null if failed to read file, other wise returns byte array.
	*/
	public static byte[] readBytes( final String _file )
	{
		final FileStream file = GlobalFileSystem.getFile( _file ) ;
		if( file.exists() == false )
		{
			System.out.println( "File: " + _file + " doesn't exist.." ) ;
			return null ;
		}

		final int size = ( int )file.getSize() ;
		final byte[] buffer = new byte[size] ;

		try( final ByteInStream in = file.getByteInStream() )
		{
			int offset = 0 ;
			while( offset >= 0 && offset < size )
			{
				offset += in.readBytes( buffer, offset, size - offset ) ;
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
		}

		return buffer ;
	}
}
