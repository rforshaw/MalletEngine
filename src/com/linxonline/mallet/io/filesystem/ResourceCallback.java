package com.linxonline.mallet.io.filesystem ;

public interface ResourceCallback
{
	public final static int STOP = -1 ;													// Stop processing the file
	public final static int RETURN_ALL = 0 ;											// Return the remaining Strings or bytes that have yet to be processed.

	public int resourceAsString( final String[] _resource, final int _length ) ;		// Called when reading a string resource, return STOP to stop reading, RETURN_ALL, or int > 0.
	public int resourceRaw( final byte[] _resource, final int _length ) ;				// Called when reading a byte resource, return STOP to stop reading, RETURN_ALL, or int > 0.

	public void start( final long _fileSize ) ; 										// Called when reading has started, passes in the file's byte length
	public void end() ;																	// Called when the file/stream has reached the absolute end.
}