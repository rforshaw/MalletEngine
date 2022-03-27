package com.linxonline.mallet.io.filesystem ;

public interface StringOutStream extends AutoCloseable
{
	public boolean writeLine( final String _line ) ;
}
