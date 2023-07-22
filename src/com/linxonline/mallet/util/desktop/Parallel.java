package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.concurrent.* ;

public final class Parallel
{
	private final static int MINIMUM_BATCH_SIZE = 50 ;

	private final static LinkedBlockingQueue<IJob> jobs = new LinkedBlockingQueue<IJob>() ;
	private volatile static int workerCount = 0 ;
	static
	{
		createWorkers( 6 ) ;
	}

	private Parallel() {}

	public static void run( final IRun _run )
	{
		jobs.add( new Job( _run ) ) ;
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
		//final long startTime = System.currentTimeMillis() ;

		final int batchSize = _batchSize ;
		final int totalSize = _end - _start ;
		final int batchNum = ( totalSize + batchSize - 1 ) / batchSize ;

		if( batchNum <= 1 )
		{
			// No point creating a job if only 1 job is being used.
			for( int i = _start; i < _end; ++i )
			{
				_run.run( i, _array[i] ) ;
			}
			return ;
		}

		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			jobs.add( new ArrayJob( latch, start, end, _array, _run ) ) ;

			start = end ;
			++numCompleted ;
		}

		try
		{
			latch.await() ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
		}

		//final long endTime = System.currentTimeMillis() ;
		//System.out.println( "Time Taken: " + ( endTime - startTime ) ) ;
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
		//final long startTime = System.currentTimeMillis() ;

		final int batchSize = _batchSize ;
		final int totalSize = _end - _start ;
		final int batchNum = ( totalSize + batchSize - 1 ) / batchSize ;

		if( batchNum <= 1 )
		{
			// No point creating a job if only 1 job is being used.
			for( int i = _start; i < _end; ++i )
			{
				_run.run( i, _list.get( i ) ) ;
			}
			return ;
		}

		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			jobs.add( new ListJob( latch, start, end, _list, _run ) ) ;

			start = end ;
			++numCompleted ;
		}

		try
		{
			latch.await() ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
		}

		//final long endTime = System.currentTimeMillis() ;
		//System.out.println( "P Time Taken: " + ( endTime - startTime ) ) ;
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
		final int batchSize = _batchSize ;
		final int totalSize = _end - _start ;
		final int batchNum = ( totalSize + batchSize - 1 ) / batchSize ;

		if( batchNum <= 1 )
		{
			// No point creating a job if only 1 job is being used.
			_run.run( _start, _end, _list ) ;
			return ;
		}

		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			jobs.add( new BatchJob( latch, start, end, _list, _run ) ) ;

			start = end ;
			++numCompleted ;
		}

		try
		{
			latch.await() ;
		}
		catch( Exception ex )
		{
			ex.printStackTrace() ;
		}
	}

	private static void createWorkers( final int _num )
	{
		for( int i = 0; i < _num; ++i )
		{
			createWorker( i ) ;
		}
	}

	private static void createWorker( final int _num )
	{
		final Worker worker = new Worker( String.format( "PARALLEL_THREAD_%d", _num ) ) ;
		//worker.setPriority( 10 ) ;
		worker.start() ;
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

	private static final class ArrayJob<T> implements IJob
	{
		private final CountDownLatch latch ;
		private final int start ;
		private final int end ;

		private final T[] array ;
		private final IRangeRun<T> runner ;

		public ArrayJob( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final T[] _array,
						 final IRangeRun<T> _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			array = _array ;
			runner = _runner ;
		}

		@Override
		public void run()
		{
			for( int i = start; i < end; ++i )
			{
				runner.run( i, array[i] ) ;
			}

			latch.countDown() ;
		}
	}

	private static final class ListJob<T> implements IJob
	{
		private final CountDownLatch latch ;
		private final int start ;
		private final int end ;

		private final List<T> list ;
		private final IRangeRun<T> runner ;

		public ListJob( final CountDownLatch _latch,
						final int _start,
						final int _end,
						final List<T> _list,
						final IRangeRun<T> _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			list = _list ;
			runner = _runner ;
		}

		@Override
		public void run()
		{
			//System.out.println( "Started" ) ;
			//final long startTime = System.currentTimeMillis() ;

			for( int i = start; i < end; ++i )
			{
				runner.run( i, list.get( i ) ) ;
			}

			//final long endTime = System.currentTimeMillis() ;
			latch.countDown() ;

			//System.out.println( "J Start: " + start + " End: " + end + " Start Time: " + startTime + " Time Taken: " + ( endTime - startTime ) ) ;
		}
	}

	private static final class BatchJob<T> implements IJob
	{
		private final CountDownLatch latch ;
		private final int start ;
		private final int end ;

		private final List<T> list ;
		private final IBatchRun<T> runner ;

		public BatchJob( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final List<T> _list,
						 final IBatchRun<T> _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			list = _list ;
			runner = _runner ;
		}

		@Override
		public void run()
		{
			//System.out.println( "Started" ) ;
			//final long startTime = System.currentTimeMillis() ;

			runner.run( start, end, list ) ;

			//final long endTime = System.currentTimeMillis() ;
			latch.countDown() ;

			//System.out.println( "J Start: " + start + " End: " + end + " Start Time: " + startTime + " Time Taken: " + ( endTime - startTime ) ) ;
		}
	}

	private static final class Job implements IJob
	{
		private final IRun runner ;

		public Job( final IRun _runner )
		{
			runner = _runner ;
		}

		@Override
		public void run()
		{
			runner.run() ;
		}
	}

	private interface IJob
	{
		public void run() ;
	}

	private static final class Worker extends Thread
	{
		private final boolean temporary ;

		public Worker( final String _name )
		{
			this( _name, false ) ;
		}

		public Worker( final String _name, final boolean _temporary )
		{
			super( _name ) ;
			temporary = _temporary ;
		}

		@Override
		public void run()
		{
			++workerCount ;

			while( true )
			{
				try
				{
					final IJob job = Parallel.jobs.take() ;
					job.run() ;

					if( temporary )
					{
						break ;
					}
				}
				catch( Exception ex )
				{
					ex.printStackTrace() ;
				}
			}

			--workerCount ;
		}
	}
}
