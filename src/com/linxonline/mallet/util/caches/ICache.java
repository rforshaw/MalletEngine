package com.linxonline.mallet.util.caches ;

/**
	Provides a genric interface for pooling/caching objects.
*/
public interface ICache<T extends Cacheable>
{
	public T get() ;							// Return an object
	public void reclaim( final T _obj ) ;		// Allow the pool to reclaim the object so it can be used again

	public int size() ;							// How many objects located in the pool
	public void trimTo( final int _size ) ;		// Trim the pool down to the specified size
}
