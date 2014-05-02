package com.linxonline.mallet.io.filesystem ;

public interface ResourceCallback
{
	public void resourceAsString( final String _resource ) ;		// Called when reading a string resource
	public void resourceRaw( final byte[] _resource ) ;				// Called when reading a byte resource

	public void end() ;		// Called when the file/stream has reached the absolute end.
}