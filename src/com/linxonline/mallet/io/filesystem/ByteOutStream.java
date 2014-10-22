package com.linxonline.mallet.io.filesystem ;

public interface ByteOutStream
{
	public int writeBytes( final byte[] _stream, final int _length ) ;

	public boolean close() ;
}