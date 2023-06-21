package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.concurrent.* ;

public final class Parallel
{
	private final static int MINIMUM_DATA_SIZE = 50 ;

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

	public static <T> void forEach( final T[] _array, final IRangeRun<T> ... _run )
	{
		forEach( _array, 0, _array.length, MINIMUM_DATA_SIZE, _run ) ;
	}

	public static <T> void forEach( final T[] _array, final int _start, final int _end, final int _minimum, final IRangeRun<T> ... _run )
	{
		//final long startTime = System.currentTimeMillis() ;

		final int numJobs = calculateJobsRequired( _run.length, _end - _start, _minimum ) ;
		if( numJobs <= 1 )
		{
			// No point creating a job if only 1 job is being used.
			for( int i = _start; i < _end; ++i )
			{
				_run[0].run( i, _array[i] ) ;
			}
			return ;
		}

		final CountDownLatch latch = new CountDownLatch( numJobs ) ;

		final int size = ( _end - _start ) / numJobs ;
		int start = _start ;
		int end = size ;

		//System.out.println( "Creating: " + numJobs + " jobs." ) ;
		for( int i = 0; i < numJobs; ++i )
		{
			final int runIndex = ( i < _run.length ) ? i : _run.length - 1 ;
			if( i == ( numJobs - 1 ) )
			{
				end = _end ;
			}

			jobs.add( new ArrayJob( latch, start, end, _array, _run[runIndex] ) ) ;
			start = end ;
			end += size ;
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

	public static <T> void forEach( final List<T> _list, final IRangeRun<T> ... _run )
	{
		forEach( _list, 0, _list.size(), MINIMUM_DATA_SIZE, _run ) ;
	}

	public static <T> void forEach( final List<T> _list, final int _start, final int _end, final int _minimum, final IRangeRun<T> ... _run )
	{
		//final long startTime = System.currentTimeMillis() ;

		final int numJobs = calculateJobsRequired( _run.length, _end - _start, _minimum ) ;
		if( numJobs <= 1 )
		{
			// No point creating a job if only 1 job is being used.
			for( int i = _start; i < _end; ++i )
			{
				_run[0].run( i, _list.get( i ) ) ;
			}
			return ;
		}

		final CountDownLatch latch = new CountDownLatch( numJobs ) ;

		final int size = ( _end - _start ) / numJobs ;
		int start = _start ;
		int end = size ;

		//System.out.println( "Creating: " + numJobs + " jobs." ) ;
		for( int i = 0; i < numJobs; ++i )
		{
			final int runIndex = ( i < _run.length ) ? i : _run.length - 1 ;
			if( i == ( numJobs - 1 ) )
			{
				end = _end ;
			}

			jobs.add( new ListJob( latch, start, end, _list, _run[runIndex] ) ) ;
			start = end ;
			end += size ;
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

	/**
		Attempt to figure out how many jobs we need to process the data.
		This is mostly used to identify the minimum number required.
		If the data set is not large enough to be shared we don't
		want to run 4 empty jobs, we want to run 1 'full' job.
	*/
	private static int calculateJobsRequired( final int _runSize, final int _dataSize, final int _minimum )
	{
		int jobs = 4 ;			// We assume 4 jobs will be used by default
		if( _runSize > 1 )
		{
			// More than 1 runner means we
			// limit ourselves to that many jobs.
			jobs = _runSize ;

			if( _runSize > workerCount )
			{
				// If the number if workers is less than
				// the number of runners then let's create
				// more workers.
				// Add an extra worker just to ensure there
				// is a worker spare.
				createWorkers( _runSize - workerCount + 1 ) ;
			}
		}

		final int size = _dataSize / jobs ;
		if( size == 0 || size < _minimum )
		{
			// If the number of items being processed is 0
			// then it's likely to be a rounding error.
			// We likely only need 1 job to process.

			// If there isn't enough data to warrant multiple jobs
			// then we should also avoid creating more jobs 
			// than needed.
			jobs = 1 ;
		}

		//System.out.println( "Jobs: " + jobs ) ;
		return jobs ;
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
		final Thread thread = new Thread( new Worker(), String.format( "PARALLEL_THREAD_%d", _num ) ) ;
		thread.setPriority( 10 ) ;
		thread.start() ;
	}

	public interface IRun
	{
		public void run() ;
	}

	public interface IRangeRun<T>
	{
		public void run( final int _index, final T _item ) ;
	}

	private static class ArrayJob<T> implements IJob
	{
		private final CountDownLatch latch ;
		private final int start ;
		private final int end ;

		private final T[] array ;
		private final IRangeRun runner ;

		public ArrayJob( final CountDownLatch _latch,
						 final int _start,
						 final int _end,
						 final T[] _array,
						 final IRangeRun _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			array = _array ;
			runner = _runner ;
			//System.out.println( "Runner: " + runner.toString() ) ;
		}

		@Override
		public void countDown()
		{
			latch.countDown() ;
		}

		@Override
		public void run()
		{
			for( int i = start; i < end; ++i )
			{
				runner.run( i, array[i] ) ;
			}
		}
	}

	private static class ListJob<T> implements IJob
	{
		private final CountDownLatch latch ;
		private final int start ;
		private final int end ;

		private final List<T> list ;
		private final IRangeRun runner ;

		public ListJob( final CountDownLatch _latch,
						final int _start,
						final int _end,
						final List<T> _list,
						final IRangeRun _runner )
		{
			latch = _latch ;

			start = _start ;
			end = _end ;

			list = _list ;
			runner = _runner ;
		}

		@Override
		public void countDown()
		{
			latch.countDown() ;
		}

		@Override
		public void run()
		{
			//System.out.println( "Started" ) ;
			//final long startTime = System.currentTimeMillis() ;

			for( int i = start; i < end; ++i )
			{
				final T t = list.get( i ) ;
				runner.run( i, t ) ;
			}

			//final long endTime = System.currentTimeMillis() ;
			//System.out.println( "J Time Taken: " + ( endTime - startTime ) + " Amount: " + ( end - start ) ) ;
		}
	}

	private static class Job implements IJob
	{
		private final IRun runner ;

		public Job( final IRun _runner )
		{
			runner = _runner ;
		}

		@Override
		public void countDown() {}

		@Override
		public void run()
		{
			runner.run() ;
		}
	}

	private interface IJob
	{
		public void countDown() ;
		public void run() ;
	}

	private static class Worker implements Runnable
	{
		private final boolean temporary ;
	
		public Worker()
		{
			this( false ) ;
		}

		public Worker( final boolean _temporary )
		{
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
					job.countDown() ;

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
