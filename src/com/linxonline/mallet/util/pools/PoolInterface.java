package com.linxonline.mallet.util.pools ;

public interface PoolInterface<T>
{
	public T get() ;

	public int size() ;
	public void trimTo( final int _size ) ;
}