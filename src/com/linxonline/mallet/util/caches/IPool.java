package com.linxonline.mallet.util.caches ;

public interface IPool<E>
{
	/**
		Take an element from the pool.
	*/
	public E take() ;

	/**
		Return an element back to the pool.
	*/
	public boolean reclaim( final E _element ) ;

	/**
		Allows the pool to create new elements
		based on the developers requirements.
	*/
	public interface ICreator<E>
	{
		public E create() ;
	}
}
