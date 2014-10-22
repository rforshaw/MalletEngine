package com.linxonline.mallet.io.reader ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.ByteInStream ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.filesystem.ResourceCallback ;

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
		final ByteInStream in = file.getByteInStream() ;

		final int size = ( int )file.getSize() ;
		final byte[] buffer = new byte[size] ;

		int offset = 0 ;
		while( offset >= 0 )
		{
			offset += in.readBytes( buffer, 0, size ) ;
		}

		in.close() ;
		return buffer ;
	}

	/**
		Not blocking, returns true if begun reading, false otherwise.
		Streams the bytes through the callback as they are read.
	**/
	public static boolean readBytes( final String _file, final int _length, final ResourceCallback _callback )
	{
		return false ;//GlobalFileSystem.getResourceRaw( _file, _length, _callback ) ;
	}
}
