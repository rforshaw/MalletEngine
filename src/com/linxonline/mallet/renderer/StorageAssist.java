package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.schema.SNode ;
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
	public static Storage create( final String _id, final SNode _var, final int _capacity )
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
		Return the size of the passed in storage block.
	*/
	public static int size( final Storage _storage )
	{
		return assist.size( _storage ) ;
	}

	public static void setBool( final Storage _storage, final int _index, final SNode _node, boolean _val )
	{
		assist.setBool( _storage, _index, _node, _val ) ;
	}

	public static void setInt( final Storage _storage, final int _index, final SNode _node, int _val )
	{
		assist.setInt( _storage, _index, _node, _val ) ;
	}

	public static void setFlt( final Storage _storage, final int _index, final SNode _node, float _val )
	{
		assist.setFlt( _storage, _index, _node, _val ) ;
	}

	public static boolean getBool( final Storage _storage, final int _index, final SNode _node )
	{
		return assist.getBool( _storage, _index, _node ) ;
	}

	public static int getInt( final Storage _storage, final int _index, final SNode _node )
	{
		return assist.getInt( _storage, _index, _node ) ;
	}

	public static float getFlt( final Storage _storage, final int _index, final SNode _node )
	{
		return assist.getFlt( _storage, _index, _node ) ;
	}

	public interface Assist
	{
		public Storage create( final String _id, final SNode _node, final int _capacity ) ;
		public Storage get( final String _id ) ;

		public int size( final Storage _storage ) ;

		public void setBool( final Storage _storage, final int _index, final SNode _node, boolean _val ) ;
		public void setInt( final Storage _storage, final int _index, final SNode _node, int _val ) ;
		public void setFlt( final Storage _storage, final int _index, final SNode _node, float _val ) ;

		public boolean getBool( final Storage _storage, final int _index, final SNode _node ) ;
		public int getInt( final Storage _storage, final int _index, final SNode _node ) ;
		public float getFlt( final Storage _storage, final int _index, final SNode _node ) ;
	}
}
