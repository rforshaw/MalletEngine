package com.linxonline.mallet.io.filesystem ;

public interface StringInStream
{
	/**
		Return a line of text from the file.
		Will return null when at the end of file.
	*/
	public String readLine() ;

	public boolean close() ;
}