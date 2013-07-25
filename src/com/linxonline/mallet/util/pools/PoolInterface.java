package com.linxonline.mallet.util.pools ;

/**
	Provides a genric interface for pooling objects.
*/
public interface PoolInterface<T>
{
	public T get() ;							// Return the object the pool is meant to provide

	public int size() ;						// How many objects located in the pool
	public void trimTo( final int _size ) ;	// Trim the pool down to the specified size
}