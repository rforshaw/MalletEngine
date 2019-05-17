package com.linxonline.mallet.io.filesystem ;

public interface ByteInStream extends Close
{
	/**
		Request to read _length of bytes from the file into 
		_stream starting at the _offset.
		Returns the actual amount of bytes it managed to read.
		Will return -1 when nothing else can be read.
	*/
	public int readBytes( final byte[] _stream, final int _offset, final int _length ) ;
}
