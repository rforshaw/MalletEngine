package com.linxonline.mallet.io.filesystem ;

public interface ByteInStream
{
	/**
		Request to read _length of bytes from the file.
		Returns the actual amount of bytes it managed to read.
		Will return -1 when nothing else can be read.
	*/
	public int readBytes( final byte[] _stream, final int _offset, final int _length ) ;

	public boolean close() ;
}