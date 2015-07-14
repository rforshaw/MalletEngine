package com.linxonline.mallet.io.filesystem ;

public interface ByteOutStream extends Close
{
	/**
		Write the _length into the file starting at _offset in _stream. 
	*/
	public int writeBytes( final byte[] _stream, final int _offset, final int _length ) ;

	public boolean close() ;
}