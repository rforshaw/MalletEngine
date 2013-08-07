package com.linxonline.mallet.util.sort ;

import java.util.ArrayList ;

public class QuickSort
{
	/**
		Recursive Quicksort algorithm.
		Used to sort RenderContainers based on their LAYER.
		-10, -4, 0, 1, 5, 11,
	*/
	public static <T> ArrayList<T> quicksort( ArrayList<T> _contents )
	{
		final int size = _contents.size() ;
		if( size <= 1 )
		{
			return _contents ;
		}
		else if( _contents.get( 0 ) instanceof SortInterface )
		{
			return sort( _contents ) ;
		}

		return null ;
	}

	private static <T> ArrayList<T> sort( ArrayList<T> _contents )
	{
		int size = _contents.size() ;
		final int halfSize = size / 2 ;

		final SortInterface pivot = ( SortInterface )_contents.get( halfSize ) ;
		_contents.remove( pivot ) ;

		ArrayList<T> less = new ArrayList<T>() ;
		ArrayList<T> greater = new ArrayList<T>() ;

		--size ;
		final int pivotPoint = pivot.sortValue() ;

		for( int i = 0; i < size; i++ )
		{
			final SortInterface s = ( SortInterface )_contents.get( i ) ;
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
