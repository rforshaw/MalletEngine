package com.linxonline.mallet.io.net ;

import com.linxonline.mallet.io.filesystem.* ;

public interface Connection
{
	public ByteInStream getByteInStream() ;
	public StringInStream getStringInStream() ;

	public boolean getByteInCallback( final ByteInCallback _callback, final int _length ) ;
	public boolean getStringInCallback( final StringInCallback _callback, final int _length ) ;

	public ByteOutStream getByteOutStream() ;
	public StringOutStream getStringOutStream() ;

	public boolean close() ;
}