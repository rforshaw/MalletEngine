package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Collections ;

public class MalletList
{
	public final static <V> List<V> newList()
	{
		return MalletList.<V>newList( 10 ) ;
	}

	public final static <V> List<V> newList( final int _initialCapacity )
	{
		return new ArrayList<V>( _initialCapacity ) ;
	}

	public final static <V> List<V> newList( final Collection<? extends V> _collection )
	{
		return new ArrayList<V>( _collection ) ;
	}
	
	public final static <V> List<V> newList( final V[] _array )
	{
		final ArrayList<V> list = new ArrayList( _array.length ) ;
		Collections.addAll( list, _array ) ;
		return list ;
	}

	public static <T> T[] concat( final T[] _lhs, final T ... _rhs )
	{
		final T[] result = Arrays.copyOf( _lhs, _lhs.length + _rhs.length ) ;
		System.arraycopy( _rhs, 0, result, _lhs.length, _rhs.length ) ;
		return result ;
	}
}
