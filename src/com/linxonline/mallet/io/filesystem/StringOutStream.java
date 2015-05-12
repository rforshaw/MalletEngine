package com.linxonline.mallet.io.filesystem ;

public interface StringOutStream extends Close
{
	public boolean writeLine( final String _line ) ;

	public boolean close() ;
}