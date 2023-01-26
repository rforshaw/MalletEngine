package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.concurrent.* ;

public final class Parallel
{
	private final static LinkedBlockingQueue<IJob> jobs = new LinkedBlockingQueue<IJob>() ;
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
		final int numJobs = calculateJobsRequired( _run.length ) ;

		final CountDownLatch latch = new CountDownLatch( numJobs ) ;

		final int size = _array.length / numJobs ;
		int start = 0 ;
		int end = size ;

		for( int i = 0; i < numJobs; ++i )
		{
			final int runIndex = ( i < _run.length ) ? i : _run.length - 1 ;
			if( i == ( numJobs - 1 ) )
			{
				end = _array.length ;
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
	}

	public static <T> void forEach( final List<T> _list, final IRangeRun<T> ... _run )
	{
		final int numJobs = calculateJobsRequired( _run.length ) ;

		final CountDownLatch latch = new CountDownLatch( numJobs ) ;

		final int size = _list.size() / numJobs ;
		int start = 0 ;
		int end = size ;

		for( int i = 0; i < numJobs; ++i )
		{
			final int runIndex = ( i < _run.length ) ? i : _run.length - 1 ;
			if( i == ( numJobs - 1 ) )
			{
				end = _list.size() ;
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
	}

	private static int calculateJobsRequired( final int _size )
	{
		return 4 ;
	}

	private static void createWorkers( final int _num )
	{
		for( int i = 0; i < _num; ++i )
		{
			createWorker() ;
		}
	}

	private static void createWorker()
	{
		final Thread thread = new Thread( new Worker(), "PARALLEL_THREAD" ) ;
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
				//System.out.println( "start: " + start + " End: " + end + " I: " + i ) ;
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
			for( int i = start; i < end; ++i )
			{
				//System.out.println( "start: " + start + " End: " + end + " I: " + i ) ;
				runner.run( i, list.get( i ) ) ;
			}
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
		}
	}
}
