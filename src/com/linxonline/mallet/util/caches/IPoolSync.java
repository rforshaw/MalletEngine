package com.linxonline.mallet.util.caches ;

public interface IPoolSync<E>
{
	/**
		Take an element from the pool.
	*/
	public E takeSync() ;

	public E[] takeSync( final E[] _fill ) ;

	/**
		Return an element back to the pool.
	*/
	public boolean reclaimSync( final E _element ) ;

	public boolean reclaimSync( final E[] _elements ) ;
}
