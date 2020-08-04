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
	public static Storage create( final String _id, final int _size )
	{
		return assist.create( _id, _size ) ;
	}

	/**
		Return the Storage block with the specified id.
	*/
	public static Storage get( final String _id )
	{
		return assist.get( _id ) ;
	}

	public static Storage update( final Storage _storage )
	{
		return assist.update( _storage ) ;
	}
	
	/**
		Return the absolute size of the node for the 
		renderer being used, different implementations will 
		return a size that matches their requirement.
	*/
	public static int calculateSize( final SNode _node )
	{
		return assist.calculateSize( _node ) ;
	}
	
	public static int calculateOffset( final SNode _node )
	{
		return assist.calculateOffset( _node ) ;
	}

	public interface Assist
	{
		public Storage create( final String _id, final int _size ) ;
		public Storage get( final String _id ) ;
		public Storage update( final Storage _storage ) ;

		public int calculateSize( final SNode _node ) ;
		public int calculateOffset( final SNode _node ) ;
	}
}
