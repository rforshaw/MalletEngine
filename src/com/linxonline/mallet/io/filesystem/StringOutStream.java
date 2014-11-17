package com.linxonline.mallet.io.filesystem ;

public interface StringOutStream extends Close
{
	/**
		Return a line of text from the file.
		Will return nul; when at the end of file.
	*/
	public boolean writeLine( final String _line ) ;

	public boolean close() ;
}