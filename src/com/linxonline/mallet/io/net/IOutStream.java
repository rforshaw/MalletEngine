package com.linxonline.mallet.io.net ;

import com.linxonline.mallet.io.serialisation.Serialise ;

public interface IOutStream
{
	/**
		Return the length of the packet in bytes.
	*/
	public int getLength() ;

	/**
		Write out the data into the passed in 
		serialise object.
	*/
	public void serialise( Serialise.Out _out ) ;
}
