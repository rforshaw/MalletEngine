package com.linxonline.mallet.util ;

import java.util.List ;

public final class Parallel
{
	private final static int MINIMUM_BATCH_SIZE = 50 ;
	private final static ParallelState STATE = new ParallelState( "GLOBAL_PARALLEL", MINIMUM_BATCH_SIZE, 6 ) ;

	private Parallel() {}

	public static void run( final IRun _run )
	{
		STATE.run( _run ) ;
	}

	public static <T> void forEach( final T[] _array, final IRangeRun<T> _run )
	{
		STATE.forEach( _array, _run ) ;
	}

	public static <T> void forEach( final T[] _array, final int _batchSize, final IRangeRun<T> _run )
	{
		STATE.forEach( _array, _batchSize, _run ) ;
	}

	public static <T> void forEach( final T[] _array, final int _start, final int _end, final int _batchSize, final IRangeRun<T> _run )
	{
		STATE.forEach( _array, _start, _end, _batchSize, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final IRangeRun<T> _run )
	{
		STATE.forEach( _list, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final int _batchSize, final IRangeRun<T> _run )
	{
		STATE.forEach( _list, _batchSize, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final int _start, final int _end, final int _batchSize, final IRangeRun<T> _run )
	{
		STATE.forEach( _list, _start, _end, _batchSize, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final IListRun<T> _run )
	{
		STATE.forBatch( _list, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final int _batchSize, final IListRun<T> _run )
	{
		STATE.forBatch( _list, _batchSize, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final int _start, final int _end, final int _batchSize, final IListRun<T> _run )
	{
		STATE.forBatch( _list, _start, _end, _batchSize, _run ) ;
	}

	public static <T> void forBatch( final T[] _array, final IArrayRun<T> _run )
	{
		STATE.forBatch( _array, _run ) ;
	}

	public static <T> void forBatch( final T[] _array, final int _batchSize, final IArrayRun<T> _run )
	{
		STATE.forBatch( _array, _batchSize, _run ) ;
	}

	public static <T> void forBatch( final T[] _array, final int _start, final int _end, final int _batchSize, final IArrayRun<T> _run )
	{
		STATE.forBatch( _array, _start, _end, _batchSize, _run ) ;
	}

	public interface IRun
	{
		public void run() ;
	}

	public interface IRangeRun<T>
	{
		public void run( final int _index, final T _item ) ;
	}

	public interface IListRun<T>
	{
		public void run( final int _start, final int _end, final List<T> _batch ) ;
	}

	public interface IArrayRun<T>
	{
		public void run( final int _start, final int _end, final T[] _batch ) ;
	}
}
