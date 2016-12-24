package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.ArrayList ;

public class Utility
{
	public static <T> List<T> newArrayList()
	{
		return Utility.<T>newArrayList( 10 ) ;
	}

	public static <T> List<T> newArrayList( final int _initialCapacity )
	{
		return new ArrayList<T>( _initialCapacity ) ;
	}
}
