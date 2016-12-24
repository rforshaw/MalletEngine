package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.ArrayList ;

public class MalletList
{
	public static <V> List<V> newList()
	{
		return MalletList.<V>newList( 10 ) ;
	}

	public static <V> List<V> newList( final int _initialCapacity )
	{
		return new ArrayList<V>( _initialCapacity ) ;
	}
}
