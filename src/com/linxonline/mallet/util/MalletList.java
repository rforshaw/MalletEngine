package com.linxonline.mallet.util ;

import java.util.List ;
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
}
