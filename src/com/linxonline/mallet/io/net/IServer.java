package com.linxonline.mallet.io.net ;

public interface IServer
{
	public boolean send( final Address _address, final IOutStream _out ) ;
	public InStream receive( final InStream _stream ) ;
}
