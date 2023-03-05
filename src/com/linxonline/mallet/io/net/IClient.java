package com.linxonline.mallet.io.net ;

public interface IClient
{
	public boolean send( final IOutStream _out ) ;
	public InStream receive( final InStream _stream  ) ;
}
