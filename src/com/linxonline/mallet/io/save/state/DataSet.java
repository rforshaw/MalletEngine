package com.linxonline.mallet.io.save.state ;

import com.linxonline.mallet.io.filesystem.ByteOutStream ;
import com.linxonline.mallet.io.filesystem.ByteInStream ;

public interface DataSet
{
	/**
		Write the Data Set to the Byte Out Stream.
	*/
	public boolean write( final ByteOutStream _out ) ;

	/**
		Read the Byte In Stream and populate the Data Set.
	*/
	public boolean read( final ByteInStream _in ) ;
}
