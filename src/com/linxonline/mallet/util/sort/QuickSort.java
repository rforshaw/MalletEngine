package com.linxonline.mallet.util.sort ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public final class QuickSort
{
	private QuickSort() {}

	/**
		Recursive Quicksort algorithm.
		Used to sort RenderContainers based on their LAYER.
		-10, -4, 0, 1, 5, 11,
	*/
	public static <T extends SortInterface> List<T> quicksort( final List<T> _contents )
	{
		final int size = _contents.size() ;
		if( size <= 1 )
		{
			return _contents ;
		}

		return sort( _contents )  ;
	}

	public static <T extends SortInterface> T[] quicksort( final T[] _contents )
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

		return sort( array ).toArray( _contents ) ;
	}

	private static <T extends SortInterface> List<T> sort( final List<T> _contents )
	{
		int size = _contents.size() ;
		final int halfSize = size / 2 ;

		final SortInterface pivot = _contents.get( halfSize ) ;
		_contents.remove( pivot ) ;

		List<T> less = MalletList.<T>newList() ;
		List<T> greater = MalletList.<T>newList() ;

		--size ;
		final int pivotPoint = pivot.sortValue() ;

		for( int i = 0; i < size; i++ )
		{
			final SortInterface s = _contents.get( i ) ;
			if( s.sortValue() <= pivotPoint )
			{
				less.add( ( T )s ) ;
			}
			else
			{
				greater.add( ( T )s ) ;
			}
		}

		less = quicksort( less ) ;
		greater = quicksort( greater ) ;

		less.add( ( T )pivot ) ;
		less.addAll( greater ) ;
		return less ;
	}
}
