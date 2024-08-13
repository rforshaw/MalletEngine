package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.concurrent.* ;
import java.util.concurrent.atomic.AtomicInteger ;

import com.linxonline.mallet.util.caches.MemoryPool ;

/**
	Assist with the processing of large data-sets
	that can be done in parallel.
	Note: Avoid calling Parallel within a Parallel
	operation - as this can result in a stall.
*/
public final class Parallel
{
	private final static int MINIMUM_BATCH_SIZE = 50 ;
	private final static int MAX_WORKER_COUNT = 6 ;

	private final static MemoryPool<BatchListJob> batchListJobs = new MemoryPool<BatchListJob>( () -> new BatchListJob() ) ;
	private final static MemoryPool<BatchArrayJob> batchArrayJobs = new MemoryPool<BatchArrayJob>( () -> new BatchArrayJob() ) ;
	private final static MemoryPool<ListJob> listJobs = new MemoryPool<ListJob>( () -> new ListJob() ) ;
	private final static MemoryPool<ArrayJob> arrayJobs = new MemoryPool<ArrayJob>( () -> new ArrayJob() ) ;

	private final static LinkedBlockingDeque<IJob> jobs = new LinkedBlockingDeque<IJob>() ;
	private volatile static int workerCount = 0 ;
	private static AtomicInteger available = new AtomicInteger( 0 ) ;
	static
	{
		createWorkers( 4 ) ;
	}

	private Parallel() {}

	public static void run( final IRun _run )
	{
		jobs.addFirst( new Job( _run ) ) ;
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

		createTempWorkers( 2 ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			synchronized( arrayJobs )
			{
				final ArrayJob<T> job = arrayJobs.take() ;
				job.set( latch, start, end, _array, _run ) ;
				jobs.addFirst( job ) ;
			}

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

		createTempWorkers( 2 ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			synchronized( listJobs )
			{
				final ListJob<T> job = listJobs.take() ;
				job.set( latch, start, end, _list, _run ) ;
				jobs.addFirst( job ) ;
			}

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

	public static <T> void forBatch( final List<T> _list, final IListRun<T> _run )
	{
		forBatch( _list, 0, _list.size(), MINIMUM_BATCH_SIZE, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final int _batchSize, final IListRun<T> _run )
	{
		forBatch( _list, 0, _list.size(), _batchSize, _run ) ;
	}

	public static <T> void forBatch( final List<T> _list, final int _start, final int _end, final int _batchSize, final IListRun<T> _run )
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

		createTempWorkers( 2 ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			synchronized( batchListJobs )
			{
				final BatchListJob<T> job = batchListJobs.take() ;
				job.set( latch, start, end, _list, _run ) ;
				jobs.addFirst( job ) ;
			}

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

	public static <T> void forBatch( final T[] _array, final IArrayRun<T> _run )
	{
		forBatch( _array, 0, _array.length, MINIMUM_BATCH_SIZE, _run ) ;
	}

	public static <T> void forBatch( final T[] _array, final int _batchSize, final IArrayRun<T> _run )
	{
		forBatch( _array, 0, _array.length, _batchSize, _run ) ;
	}

	public static <T> void forBatch( final T[] _array, final int _start, final int _end, final int _batchSize, final IArrayRun<T> _run )
	{
		final int batchSize = _batchSize ;
		final int totalSize = _end - _start ;
		final int batchNum = ( totalSize + batchSize - 1 ) / batchSize ;

		if( batchNum <= 1 )
		{
			// No point creating a job if only 1 job is being used.
			_run.run( _start, _end, _array ) ;
			return ;
		}

		createTempWorkers( 2 ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			synchronized( batchArrayJobs )
			{
				final BatchArrayJob<T> job = batchArrayJobs.take() ;
				job.set( latch, start, end, _array, _run ) ;
				jobs.addFirst( job ) ;
			}

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

	private static void createTempWorkers( final int _num )
	{
		if( available.get() > 0 )
		{
			return ;
		}

		for( int i = 0; i < _num; ++i )
		{
			createWorker( workerCount + i, Worker.Type.TEMP ) ;
		}
	}

	private static void createWorkers( final int _num )
	{
		for( int i = 0; i < _num; ++i )
		{
			createWorker( workerCount + i, Worker.Type.FULL_TIME ) ;
		}
	}

	private static void createWorker( final int _num, final Worker.Type _type )
	{
		final Worker worker = new Worker( String.format( "PARALLEL_THREAD_%d", _num ), _type ) ;
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

	public interface IListRun<T>
	{
		public void run( final int _start, final int _end, final List<T> _batch ) ;
	}

	public interface IArrayRun<T>
	{
		public void run( final int _start, final int _end, final T[] _batch ) ;
	}

	private static final class ArrayJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private T[] array ;
		private IRangeRun<T> runner ;

		public ArrayJob() {}

		public void set( final CountDownLatch _latch,
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

		private void reset()
		{
			latch = null ;
			array = null ;
			runner = null ;
		}

		@Override
		public void run()
		{
			for( int i = start; i < end; ++i )
			{
				runner.run( i, array[i] ) ;
			}
			latch.countDown() ;

			synchronized( arrayJobs )
			{
				reset() ;
				arrayJobs.reclaim( this ) ;
			}
		}
	}

	private static final class ListJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private  List<T> list ;
		private IRangeRun<T> runner ;

		public ListJob() {}

		public void set( final CountDownLatch _latch,
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

		private void reset()
		{
			latch = null ;
			list = null ;
			runner = null ;
		}

		@Override
		public void run()
		{
			for( int i = start; i < end; ++i )
			{
				runner.run( i, list.get( i ) ) ;
			}
			latch.countDown() ;

			synchronized( listJobs )
			{
				reset() ;
				listJobs.reclaim( this ) ;
			}
		}
	}

	private static final class BatchArrayJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private T[] array ;
		private IArrayRun<T> runner ;

		public BatchArrayJob() {}

		public void set( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final T[] _array,
						 final IArrayRun<T> _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			array = _array ;
			runner = _runner ;
		}

		private void reset()
		{
			latch = null ;
			array = null ;
			runner = null ;
		}

		@Override
		public void run()
		{
			runner.run( start, end, array ) ;
			latch.countDown() ;

			synchronized( batchArrayJobs )
			{
				reset() ;
				batchArrayJobs.reclaim( this ) ;
			}
		}
	}

	private static final class BatchListJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private List<T> list ;
		private IListRun<T> runner ;

		public BatchListJob() {}

		public void set( final CountDownLatch _latch,
							 final int _start,
							 final int _end,
							 final List<T> _list,
							 final IListRun<T> _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			list = _list ;
			runner = _runner ;
		}

		private void reset()
		{
			latch = null ;
			list = null ;
			runner = null ;
		}

		@Override
		public void run()
		{
			runner.run( start, end, list ) ;
			latch.countDown() ;

			synchronized( batchListJobs )
			{
				reset() ;
				batchListJobs.reclaim( this ) ;
			}
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
		private final Type type ;
	
		public Worker( final String _name, final Type _type )
		{
			super( _name ) ;
			type = _type ;
		}

		@Override
		public void run()
		{
			++workerCount ;

			while( true )
			{
				try
				{
					final int av = available.incrementAndGet() ;

					switch( type )
					{
						default :
						case FULL_TIME :
						{
							final IJob job = Parallel.jobs.take() ;
							available.decrementAndGet() ;

							job.run() ;
							break ;
						}
						case TEMP      :
						{
							// A temp worker should only exist if Parallel was called
							// within a Parallel call and there are no full-time workers.
							// We add jobs at the front of the queue to ensure they
							// are picked up first. Should prevent indefinite stalling.
							final IJob job = Parallel.jobs.poll( 10, TimeUnit.MILLISECONDS ) ;
							available.decrementAndGet() ;

							if( job == null )
							{
								--workerCount ;
								return  ;
							}

							job.run() ;
							--workerCount ;
							return ;
						}
					}
				}
				catch( Exception ex )
				{
					ex.printStackTrace() ;
				}
			}
		}

		private enum Type
		{
			FULL_TIME,
			TEMP
		}
	}
}
