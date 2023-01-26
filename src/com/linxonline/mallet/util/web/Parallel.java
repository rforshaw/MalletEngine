package com.linxonline.mallet.util ;

import java.util.List ;

/**
	Web doesn't really have the concept of parallel processing
	unless we start using worker-threads.
*/
public final class Parallel
{
	private Parallel() {}

	public static void run( final IRun _run )
	{
		_run.run() ;
	}

	public static <T> void forEach( final T[] _array, final IRangeRun<T> ... _run )
	{
		for( int i = 0; i < _array.length; ++i )
		{
			_run[0].run( i, _array[i] ) ;
		}
	}

	public static <T> void forEach( final List<T> _list, final IRangeRun<T> ... _run )
	{
		final int size = _list.size() ;
		for( int i = 0; i < size; ++i )
		{
			_run[0].run( i, _list.get( i ) ) ;
		}
	}

	public interface IRun
	{
		public void run() ;
	}

	public interface IRangeRun<T>
	{
		public void run( final int _index, final T _item ) ;
	}
}
