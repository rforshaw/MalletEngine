package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.concurrent.* ;
import java.util.concurrent.atomic.AtomicInteger ;

import com.linxonline.mallet.util.caches.IPoolSync ;
import com.linxonline.mallet.util.caches.MemoryPool ;

/**
	Assist with the processing of large data-sets
	that can be done in parallel.
	Note: Avoid calling Parallel within a Parallel
	operation - as this can result in a stall.
*/
public final class ParallelState implements AutoCloseable
{
	private final IPoolSync<FactoryArrayJob> factoryArrayJobs = new MemoryPool<FactoryArrayJob>( () -> new FactoryArrayJob() ) ;
	private final IPoolSync<FactoryListJob> factoryListJobs = new MemoryPool<FactoryListJob>( () -> new FactoryListJob() ) ;
	private final IPoolSync<BatchListJob> batchListJobs = new MemoryPool<BatchListJob>( () -> new BatchListJob() ) ;
	private final IPoolSync<BatchArrayJob> batchArrayJobs = new MemoryPool<BatchArrayJob>( () -> new BatchArrayJob() ) ;
	private final IPoolSync<ListJob> listJobs = new MemoryPool<ListJob>( () -> new ListJob() ) ;
	private final IPoolSync<ArrayJob> arrayJobs = new MemoryPool<ArrayJob>( () -> new ArrayJob() ) ;

	private final Worker[] workers ;
	private final LinkedBlockingDeque<IJob> jobs = new LinkedBlockingDeque<IJob>() ;

	private volatile int workerCount = 0 ;
	private AtomicInteger available = new AtomicInteger( 0 ) ;

	private final String name ;
	private final int minimumBatchSize ;

	public ParallelState( final String _name, final int _minBatchSize, final int _workerCount )
	{
		name = _name ;
		minimumBatchSize = _minBatchSize ;

		workers = new Worker[_workerCount] ;
		for( int i = 0; i < workers.length; ++i )
		{
			final int num = i + 1 ;
			workers[i] = new Worker( String.format( "%s_%d", name, num ) ) ;
			workers[i].start() ;
		}
	}

	public void run( final Parallel.IRun _run )
	{
		jobs.addFirst( new Job( _run ) ) ;
	}

	public <T> void forEach( final T[] _array, final Parallel.IRangeRun<T> _run )
	{
		forEach( _array, 0, _array.length, minimumBatchSize, _run ) ;
	}

	public <T> void forEach( final T[] _array, final int _batchSize, final Parallel.IRangeRun<T> _run )
	{
		forEach( _array, 0, _array.length, _batchSize, _run ) ;
	}

	public <T> void forEach( final T[] _array, final int _start, final int _end, final int _batchSize, final Parallel.IRangeRun<T> _run )
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

		createTempWorkers( 2, batchNum ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			final ArrayJob<T> job = arrayJobs.takeSync() ;
			job.set( latch, start, end, _array, _run ) ;
			jobs.addFirst( job ) ;

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

	public <T> void forEach( final List<T> _list, final Parallel.IRangeRun<T> _run )
	{
		forEach( _list, 0, _list.size(), minimumBatchSize, _run ) ;
	}

	public <T> void forEach( final List<T> _list, final int _batchSize, final Parallel.IRangeRun<T> _run )
	{
		forEach( _list, 0, _list.size(), _batchSize, _run ) ;
	}

	public <T> void forEach( final List<T> _list, final int _start, final int _end, final int _batchSize, final Parallel.IRangeRun<T> _run )
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

		createTempWorkers( 2, batchNum ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			final ListJob<T> job = listJobs.takeSync() ;
			job.set( latch, start, end, _list, _run ) ;
			jobs.addFirst( job ) ;

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

	public <T> void forBatch( final List<T> _list, final Parallel.IListRun<T> _run )
	{
		forBatch( _list, 0, _list.size(), minimumBatchSize, _run ) ;
	}

	public <T> void forBatch( final List<T> _list, final int _batchSize, final Parallel.IListRun<T> _run )
	{
		forBatch( _list, 0, _list.size(), _batchSize, _run ) ;
	}

	public <T> void forBatch( final List<T> _list, final int _start, final int _end, final int _batchSize, final Parallel.IListRun<T> _run )
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

		createTempWorkers( 2, batchNum ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			final BatchListJob<T> job = batchListJobs.takeSync() ;
			job.set( latch, start, end, _list, _run ) ;
			jobs.addFirst( job ) ;

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

	public <T> void forBatch( final T[] _array, final Parallel.IArrayRun<T> _run )
	{
		forBatch( _array, 0, _array.length, minimumBatchSize, _run ) ;
	}

	public <T> void forBatch( final T[] _array, final int _batchSize, final Parallel.IArrayRun<T> _run )
	{
		forBatch( _array, 0, _array.length, _batchSize, _run ) ;
	}

	public <T> void forBatch( final T[] _array, final int _start, final int _end, final int _batchSize, final Parallel.IArrayRun<T> _run )
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

		createTempWorkers( 2, batchNum ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			final BatchArrayJob<T> job = batchArrayJobs.takeSync() ;
			job.set( latch, start, end, _array, _run ) ;
			jobs.addFirst( job ) ;

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

	public <T> void forBatch( final List<T> _list, final Parallel.IListFactory<T> _factory )
	{
		forBatch( _list, 0, _list.size(), minimumBatchSize, _factory ) ;
	}

	public <T> void forBatch( final List<T> _list, final int _batchSize, final Parallel.IListFactory<T> _factory )
	{
		forBatch( _list, 0, _list.size(), _batchSize, _factory ) ;
	}

	public <T> void forBatch( final List<T> _list, final int _start, final int _end, final int _batchSize, final Parallel.IListFactory<T> _factory )
	{
		final int batchSize = _batchSize ;
		final int totalSize = _end - _start ;
		final int batchNum = ( totalSize + batchSize - 1 ) / batchSize ;

		_factory.required( batchNum ) ;

		createTempWorkers( 2, batchNum ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			final FactoryListJob<T> job = factoryListJobs.takeSync() ;
			job.set( latch, start, end, _list, _factory ) ;
			jobs.addFirst( job ) ;

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

	public <T> void forBatch( final T[] _array, final Parallel.IArrayFactory<T> _factory )
	{
		forBatch( _array, 0, _array.length, minimumBatchSize, _factory ) ;
	}

	public <T> void forBatch( final T[] _array, final int _batchSize, final Parallel.IArrayFactory<T> _factory )
	{
		forBatch( _array, 0, _array.length, _batchSize, _factory ) ;
	}

	public <T> void forBatch( final T[] _array, final int _start, final int _end, final int _batchSize, final Parallel.IArrayFactory<T> _factory )
	{
		final int batchSize = _batchSize ;
		final int totalSize = _end - _start ;
		final int batchNum = ( totalSize + batchSize - 1 ) / batchSize ;

		_factory.required( batchNum ) ;

		createTempWorkers( 2, batchNum ) ;
		int start = _start ;
		int numCompleted = 0 ;

		final CountDownLatch latch = new CountDownLatch( batchNum ) ;

		while( numCompleted < batchNum )
		{
			int end = start + batchSize ;
			end = ( end > _end ) ? _end : end ;

			final FactoryArrayJob<T> job = factoryArrayJobs.takeSync() ;
			job.set( latch, start, end, _array, _factory ) ;
			jobs.addFirst( job ) ;

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

	@Override
	public void close() throws Exception
	{
		for( int i = 0; i < workers.length; ++i )
		{
			workers[i].shutdown() ;
		}
	}

	private void createTempWorkers( int _num, final int _jobsTocomplete )
	{
		if( available.get() > 0 )
		{
			return ;
		}

		// We want to avoid hiring more workers than
		// we have jobs that need completing.
		_num = ( _num <= _jobsTocomplete ) ? _num : _jobsTocomplete ;
		final int shareJobs = _num / _jobsTocomplete ;

		final int count = workerCount + 1 ;
		for( int i = 0; i < _num; ++i )
		{
			final int num = count + i ;
			final Worker worker = new Worker( String.format( "TEMP_%s_%d", name, num ), shareJobs ) ;
			worker.start() ;
		}
	}

	private final class ArrayJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private T[] array ;
		private Parallel.IRangeRun<T> runner ;

		public ArrayJob() {}

		public void set( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final T[] _array,
						 final Parallel.IRangeRun<T> _runner )
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

			reset() ;
			ParallelState.this.arrayJobs.reclaimSync( this ) ;
		}
	}

	private final class ListJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private  List<T> list ;
		private Parallel.IRangeRun<T> runner ;

		public ListJob() {}

		public void set( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final List<T> _list,
						 final Parallel.IRangeRun<T> _runner )
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

			reset() ;
			ParallelState.this.listJobs.reclaimSync( this ) ;
		}
	}

	private final class BatchArrayJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private T[] array ;
		private Parallel.IArrayRun<T> runner ;

		public BatchArrayJob() {}

		public void set( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final T[] _array,
						 final Parallel.IArrayRun<T> _runner )
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

			reset() ;
			ParallelState.this.batchArrayJobs.reclaimSync( this ) ;
		}
	}

	private final class BatchListJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private List<T> list ;
		private Parallel.IListRun<T> runner ;

		public BatchListJob() {}

		public void set( final CountDownLatch _latch,
							 final int _start,
							 final int _end,
							 final List<T> _list,
							 final Parallel.IListRun<T> _runner )
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

			reset() ;
			ParallelState.this.batchListJobs.reclaimSync( this ) ;
		}
	}

	private final class FactoryListJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private List<T> list ;
		private Parallel.IListFactory<T> factory ;

		public FactoryListJob() {}

		public void set( final CountDownLatch _latch,
							 final int _start,
							 final int _end,
							 final List<T> _list,
							 final Parallel.IListFactory<T> _factory )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			list = _list ;
			factory = _factory ;
		}

		private void reset()
		{
			latch = null ;
			list = null ;
			factory = null ;
		}

		@Override
		public void run()
		{
			final Parallel.IListRun<T> run = factory.create() ;
			run.run( start, end, list ) ;

			latch.countDown() ;

			reset() ;
			ParallelState.this.factoryListJobs.reclaimSync( this ) ;
		}
	}

	private final class FactoryArrayJob<T> implements IJob
	{
		private CountDownLatch latch ;
		private int start ;
		private int end ;

		private T[] array ;
		private Parallel.IArrayFactory<T> factory ;

		public FactoryArrayJob() {}

		public void set( final CountDownLatch _latch,
							 final int _start,
							 final int _end,
							 final T[] _array,
							 final Parallel.IArrayFactory<T> _factory )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			array = _array ;
			factory = _factory ;
		}

		private void reset()
		{
			latch = null ;
			array = null ;
			factory = null ;
		}

		@Override
		public void run()
		{
			final Parallel.IArrayRun<T> run = factory.create() ;
			run.run( start, end, array ) ;

			latch.countDown() ;

			reset() ;
			ParallelState.this.factoryArrayJobs.reclaimSync( this ) ;
		}
	}

	private final class Job implements IJob
	{
		private final Parallel.IRun runner ;

		public Job( final Parallel.IRun _runner )
		{
			runner = _runner ;
		}

		@Override
		public void run()
		{
			runner.run() ;
		}
	}

	private sealed interface IJob permits Job, BatchListJob, BatchArrayJob, ListJob, ArrayJob, FactoryListJob, FactoryArrayJob
	{
		public void run() ;
	}

	private final class Worker extends Thread
	{
		private final boolean isTemp ;

		private int jobsTocomplete = 0 ;
		private volatile boolean running = true ;

		public Worker( final String _name )
		{
			super( _name ) ;
			isTemp = false ;
		}

		// A worker introduced only to
		// complete a fixed number of jobs is
		// considered a temp-worker.
		public Worker( final String _name, final int _jobsTocomplete )
		{
			super( _name ) ;
			isTemp = true ;
			jobsTocomplete = _jobsTocomplete ;
		}

		public void shutdown()
		{
			running = false ;
		}

		@Override
		public void run()
		{
			++workerCount ;

			while( running )
			{
				try
				{
					// Our worker thread has yet to retrieve a job
					// and so we'll flag it as available.
					final int av = available.incrementAndGet() ;

					// Grab a job and decrement our availability.
					// If our thread has been shutdown it should now fall
					// through and clean up after itself.
					final IJob job = ParallelState.this.jobs.poll( 10, TimeUnit.MILLISECONDS ) ;

					// We've got a job -maybe- we are no longer available
					// while we try to run it.
					available.decrementAndGet() ;
					if( job != null )
					{
						job.run() ;
					}

					// Jobs were created, but there was no
					// full-time workers available to process them.
					// To avoid the possibility of worker exhaustion
					// a few temp-workers were created to meet
					// the demand.
					if( isTemp )
					{
						jobsTocomplete -= -1 ;
						// Whichever comes first.
						if( jobsTocomplete <= 0 || job == null )
						{
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
	}
}
