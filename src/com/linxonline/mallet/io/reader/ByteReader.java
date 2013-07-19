package com.linxonline.mallet.io.reader ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.ResourceCallback ;

/**
	Convience methods to read byte streams.
**/
public class ByteReader
{
	private ByteReader() {}

	/**
		Blocking, returns null if failed to read file, other wise returns byte array.
	**/
	public static byte[] readBytes( final String _file )
	{
		return GlobalFileSystem.getResourceRaw( _file ) ;
	}

	/**
		Not blocking, returns true if begun reading, false otherwise.
		Streams the bytes through the callback as they are read.
	**/
	public static boolean readBytes( final String _file, final ResourceCallback _callback )
	{
		return GlobalFileSystem.getResourceRaw( _file, _callback ) ;
	}
}
