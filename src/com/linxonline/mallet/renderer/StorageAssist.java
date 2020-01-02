package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.schema.IVar ;
import com.linxonline.mallet.maths.* ;

public final class StorageAssist
{
	private static Assist assist ;

	private StorageAssist() {}

	public static void setAssist( final Assist _assist )
	{
		assist = _assist ;
	}

	/**
		Create a Storage block in which objects 
		can be added to.
	*/
	public static Storage create( final String _id, final IVar _var, final int _capacity )
	{
		return assist.create( _id, _var, _capacity ) ;
	}

	/**
		Return the Storage block with the specified id.
	*/
	public static Storage get( final String _id )
	{
		return assist.get( _id ) ;
	}

	/**
		Set the size of the storage block.
	*/
	public static int size( final Storage _storage, final int _size )
	{
		return assist.size( _storage, _size ) ;
	}

	/**
		Return the size of the passed in storage block.
	*/
	public static int size( final Storage _storage )
	{
		return assist.size( _storage ) ;
	}

	public static int setAt( final Storage _storage, final int _index, Object _obj )
	{
		return assist.setAt( _storage, _index, _obj ) ;
	}

	public Object getAt( final Storage _storage, final int _index )
	{
		return assist.getAt( _storage, _index ) ;
	}

	public interface Assist
	{
		public Storage create( final String _id, final IVar _var, final int _capacity ) ;
		public Storage get( final String _id ) ;

		public int size( final Storage _storage, final int _size ) ;
		public int size( final Storage _storage ) ;

		public int setAt( final Storage _storage, final int _index, Object _obj ) ;
		public Object getAt( final Storage _storage, final int _index ) ;
	}
}
