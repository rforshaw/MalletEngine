package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.io.serialisation.Serialise ;

/**
	Handler for accessing and modifying storage objects.

	Storage objects can be created and attached to program 
	handles. A storage object can be used to store data 
	that can be later used by a shader.
*/
public class Storage extends ABuffer
{
	private final IData data ;

	public Storage( final IData _data )
	{
		data = _data ;
	}

	public IData getData()
	{
		return data ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.STORAGE_BUFFER ;
	}

	@Override
	public int getOrder()
	{
		return 0 ;
	}

	/**
		If you have a complex schema for a Storage object 
		there is a chance you'll want to upload the data set 
		with as few operations as possible.
	*/
	public interface IData
	{
		/**
			Return the length of the object in bytes.
			This is used by the storage object to construct 
			and appropriately sized buffer.
		*/
		public int getLength() ;

		/**
			Write out the data into the passed in 
			serialise object.
		*/
		public void serialise( Serialise.Out _out ) ;
	}
}
