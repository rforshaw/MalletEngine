package com.linxonline.mallet.io.save.state ;

import com.linxonline.mallet.io.filesystem.ByteOutStream ;
import com.linxonline.mallet.io.filesystem.ByteInStream ;

/**
	DataSet allows the developer to quickly save and load data.

	It is intended to be used for networking/checkpoints/game-saving.

	Identify variables that must be saved on a per Entity/State/Component 
	level and extend DataSet to store these variables.

	Use DataOut and DataIn to write/read in the implemented DataSet.
	Implement DataOut and DataIn multiple times for different styles 
	of saving/reading the DataSet. For example a network implementation my 
	used a fixed byte array, for efficient transfer of data, while saving 
	to the harddrive may use a more friendly JSON implementation for debugging 
	purposes.

	These interfaces should be used in conjunction with DataConverter.
*/
public interface DataSet
{
	/**
		Implement DataOut for writing out a specific 
		implementation of DataSet.

		If you implement DataOut you must create an equivalent 
		DataIn that is able to read DataOut output.
	*/
	public static interface DataOut<T extends DataSet>
	{
		public boolean write( final T _out ) ;
	}

	/**
		Implement DataIn for reading in a specific 
		implementation of DataSet.

		If you implement Datain you must create an equivalent 
		DataOut that is able to write output DataIn can read.
	*/
	public static interface DataIn<T extends DataSet>
	{
		public boolean read( final T _in ) ;
	}
}
