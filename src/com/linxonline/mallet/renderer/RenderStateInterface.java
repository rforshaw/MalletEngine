package com.linxonline.mallet.renderer ;

public interface RenderStateInterface<T, V>		// T = ID, V = data
{
	public void add( T _id, V _data ) ;
	public void insert( T _id, V _data ) ;

	public void remove( T _id ) ;

	public void copy( RenderStateInterface<T, V> _state ) ;
	public void clear() ;

	public void sort() ;

	public V getData( T _id ) ;
}