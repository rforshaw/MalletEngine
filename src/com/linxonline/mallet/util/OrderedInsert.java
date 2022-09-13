package com.linxonline.mallet.util ;

import java.util.List ;

public final class OrderedInsert
{
	private OrderedInsert() {}

	/**
		Insert the value at the requested location specified 
		by sortValue().
	*/
	public static <T extends ISort> List<T> insert( final T _toInsert, final List<T> _list )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; i++ )
		{
			final T toCompare = _list.get( i ) ;
			if( _toInsert.sortValue() <= toCompare.sortValue() )
			{
				_list.add( i, _toInsert ) ;		// Insert at index location
				return _list ;
			}
		}

		_list.add( _toInsert ) ;
		return _list ;
	}
}
