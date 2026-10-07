package com.linxonline.mallet.util ;

import java.util.List ;

public final class QuickSort
{
	private QuickSort() {}

	/**
		Recursive Quicksort algorithm.
		Used to sort RenderContainers based on their LAYER.
		-10, -4, 0, 1, 5, 11,
	*/
	public static <T> List<T> quicksort( final List<T> _contents, final ICompare<T> _compare )
	{
		final int size = _contents.size() ;
		if( size <= 1 )
		{
			return _contents ;
		}

		return sort( _contents, _compare )  ;
	}

	public static <T> T[] quicksort( final T[] _contents , final ICompare<T> _compare)
	{
		final int size = _contents.length ;
		if( size <= 1 )
		{
			return _contents ;
		}

		final List<T> array = MalletList.<T>newList( size ) ;
		for( int i = 0; i < size; ++i )
		{
			array.add( _contents[i] ) ;
		}

		return sort( array, _compare ).toArray( _contents ) ;
	}

	private static <T> List<T> sort( final List<T> _contents, final ICompare<T> _compare )
	{
		int size = _contents.size() ;
		final T pivot = _contents.remove( size / 2 ) ;

		List<T> less = MalletList.<T>newList() ;
		List<T> greater = MalletList.<T>newList() ;

		--size ;

		for( int i = 0; i < size; i++ )
		{
			final T s = _contents.get( i ) ;
			if( _compare.compare( s, pivot ) <= 0 )
			{
				less.add( s ) ;
			}
			else
			{
				greater.add( s ) ;
			}
		}

		less = quicksort( less, _compare ) ;
		greater = quicksort( greater, _compare ) ;

		less.add( pivot ) ;
		less.addAll( greater ) ;
		return less ;
	}
}
