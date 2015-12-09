package com.linxonline.mallet.util.sort ;

import java.util.ArrayList ;

public class OrderedInsert
{
	public static <T extends SortInterface> ArrayList<T> insert( final T _toInsert, final ArrayList<T> _list )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; i++ )
		{
			final T toCompare = _list.get( i ) ;
			if( _toInsert.sortValue() <= toCompare.sortValue() )
			{
				_list.add( i, _toInsert ) ;		// Insert at index location
				break ;
			}
		}

		_list.add( _toInsert ) ;
		return _list ;
	}
}