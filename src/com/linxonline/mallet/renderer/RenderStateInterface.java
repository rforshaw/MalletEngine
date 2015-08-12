package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

public interface RenderStateInterface<T, V>		// T = ID, V = data
{
	public void add( T _id, V _data ) ;
	public void insert( T _id, V _data ) ;

	public void remove( T _id ) ;

	public void retireCurrentState() ;
	public void clear() ;

	public void sort() ;

	public ArrayList<V> getContent() ;

	public V getData( T _id ) ;
}