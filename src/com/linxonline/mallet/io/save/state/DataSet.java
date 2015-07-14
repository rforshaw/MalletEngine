package com.linxonline.mallet.io.save.state ;

import com.linxonline.mallet.io.filesystem.ByteOutStream ;
import com.linxonline.mallet.io.filesystem.ByteInStream ;

public interface DataSet
{
	/**
		Write the Data Set to the Byte Out Stream.
	*/
	public <T extends DataOut>boolean write( final T _out ) ;

	/**
		Read the Byte In Stream and populate the Data Set.
		ByteInStream should be extended if the user wishes to implement 
		custom file reading.
	*/
	public <T extends DataIn> boolean read( final T _in ) ;

	/**
		Extend Data Out with the intended format.
		For example you may have a JSONDataOut.
	*/
	public static interface DataOut {}

	/**
		Extend Data In with the intended format.
		For example you may have a JSONDataIn.
	*/
	public static interface DataIn {}
}
