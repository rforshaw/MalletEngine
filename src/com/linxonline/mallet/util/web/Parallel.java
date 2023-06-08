package com.linxonline.mallet.util ;

import java.util.List ;

/**
	Web doesn't really have the concept of parallel processing
	unless we start using worker-threads.
*/
public final class Parallel
{
	private final static int MINIMUM_DATA_SIZE = 50 ;	// Not needed but lets stay inline with android/desktop.

	private Parallel() {}

	public static void run( final IRun _run )
	{
		_run.run() ;
	}

	public static <T> void forEach( final T[] _array, final IRangeRun<T> ... _run )
	{
		forEach( _array, 0, _array.length, MINIMUM_DATA_SIZE, _run ) ;
	}

	public static <T> void forEach( final T[] _array, final int _start, final int _end, final int _minimum, final IRangeRun<T> ... _run )
	{
		for( int i = _start; i < _end; ++i )
		{
			_run[0].run( i, _array[i] ) ;
		}
	}

	public static <T> void forEach( final List<T> _list, final IRangeRun<T> ... _run )
	{
		final int size = _list.size() ;
		forEach( _list, 0, size, MINIMUM_DATA_SIZE, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final int _start, final int _end, final int _minimum, final IRangeRun<T> ... _run )
	{
		for( int i = _start; i < _end; ++i )
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
