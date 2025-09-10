package com.linxonline.mallet.renderer ;

/**
	Handler for accessing and modifying storage objects.

	Storage objects can be created and attached to program 
	handles. A storage object can be used to store data 
	that can be later used by a shader.
*/
public final class Storage implements IUpdateState
{
	private final static Utility utility = new Utility() ;

	private final int index = utility.getGlobalIndex() ;
	private final IData data ;

	public Storage( final IData _data )
	{
		data = _data ;
	}

	public IData getData()
	{
		return data ;
	}

	public int index()
	{
		return index ;
	}

	public void requestUpdate()
	{
		StorageAssist.update( this ) ;
	}

	public interface ISerialise
	{
		public int writeInt( final int _offset, final int _val ) ;

		public int writeFloat( final int _offset, final float _val ) ;
		public int writeFloats( final int _offset, final float[] _val ) ;

		public int writeVec2( final int _offset, final float _x, final float _y ) ;
		public int writeVec3( final int _offset, final float _x, final float _y, final float _z ) ;
		public int writeVec4( final int _offset, final float _x, final float _y, final float _z, final float _w ) ;
	}
	
	/**
		If you have a complex schema for a Storage object 
		there is a chance you'll want to upload the data set 
		with as few operations as possible.
	*/
	public interface IData
	{
		/**
			Return the length of the data stream in bytes.
			This is used by the storage object to construct 
			and appropriately sized buffer.
		*/
		public int getLength() ;

		/**
			Write out the data into the passed in 
			serialise object.
		*/
		public void serialise( ISerialise _out ) ;
	}
}
