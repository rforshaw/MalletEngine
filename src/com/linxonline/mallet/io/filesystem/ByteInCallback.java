package com.linxonline.mallet.io.filesystem ;

public interface ByteInCallback
{
	public final static int STOP = -1 ;										// Stop processing the file

	public int readBytes( final byte[] _stream, final int _length ) ;		// Called when reading a byte resource, return STOP to stop reading, RETURN_ALL, or int > 0.

	public void start() ;													// Called when reading has started
	public void end() ;														// Called when the file/stream has reached the absolute end.
}