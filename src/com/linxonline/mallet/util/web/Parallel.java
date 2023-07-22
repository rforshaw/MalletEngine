package com.linxonline.mallet.util ;

import java.util.List ;

/**
	Web doesn't really have the concept of parallel processing
	unless we start using worker-threads.
*/
public final class Parallel
{
	private final static int MINIMUM_BATCH_SIZE = 50 ;	// Not needed but lets stay inline with android/desktop.

	private Parallel() {}

	public static void run( final IRun _run )
	{
		_run.run() ;
	}

	public static <T> void forEach( final T[] _array, final IRangeRun<T> _run )
	{
		forEach( _array, 0, _array.length, MINIMUM_BATCH_SIZE, _run ) ;
	}

	public static <T> void forEach( final T[] _array, final int _batchSize, final IRangeRun<T> _run )
	{
		forEach( _array, 0, _array.length, _batchSize, _run ) ;
	}

	public static <T> void forEach( final T[] _array, final int _start, final int _end, final int _batchSize, final IRangeRun<T> _run )
	{
		for( int i = _start; i < _end; ++i )
		{
			_run.run( i, _array[i] ) ;
		}
	}

	public static <T> void forEach( final List<T> _list, final IRangeRun<T> _run )
	{
		forEach( _list, 0, _list.size(), MINIMUM_BATCH_SIZE, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final int _batchSize, final IRangeRun<T> _run )
	{
		forEach( _list, 0, _list.size(), _batchSize, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final int _start, final int _end, final int _batchSize, final IRangeRun<T> _run )
	{
		for( int i = _start; i < _end; ++i )
		{
			_run.run( i, _list.get( i ) ) ;
		}
	}

	public static <T> void forBatch( final List<T> _list, final IBatchRun<T> _run )
	{
		forBatch( _list, 0, _list.size(), MINIMUM_BATCH_SIZE, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final int _batchSize, final IBatchRun<T> _run )
	{
		forBatch( _list, 0, _list.size(), _batchSize, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final int _start, final int _end, final int _batchSize, final IBatchRun<T> _run )
	{
		_run.run( _start, _end, _list ) ;
	}

	public interface IRun
	{
		public void run() ;
	}

	public interface IRangeRun<T>
	{
		public void run( final int _index, final T _item ) ;
	}

	public interface IBatchRun<T>
	{
		public void run( final int _start, final int _end, final List<T> _batch ) ;
	}
}
