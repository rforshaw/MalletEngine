package com.linxonline.mallet.io.filesystem ;

public interface ResourceCallback
{
	public void resourceAsString( final String _resource ) ;
	public void resourceRaw( final byte[] _resource ) ;
}