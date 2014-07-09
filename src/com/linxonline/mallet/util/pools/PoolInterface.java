package com.linxonline.mallet.util.pools ;

/**
	Provides a genric interface for pooling objects.
*/
public interface PoolInterface<T>
{
	public T get() ;							// Provide an object to use
	public void reclaim( final T _obj ) ;		// Allow the pool to reclaim the object so it can be used again

	public int size() ;							// How many objects located in the pool
	public void trimTo( final int _size ) ;		// Trim the pool down to the specified size
}