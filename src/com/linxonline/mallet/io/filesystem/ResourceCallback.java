package com.linxonline.mallet.io.filesystem ;

public interface ResourceCallback
{
	public boolean resourceAsString( final String _resource ) ;		// Called when reading a string resource, return false to stop reading, true to continue.
	public boolean resourceRaw( final byte[] _resource ) ;			// Called when reading a byte resource, return false to stop reading, true to continue.

	public void start( final long _fileSize ) ; 					// Called when reading has started, passes in the file's byte length
	public void end() ;												// Called when the file/stream has reached the absolute end.
}