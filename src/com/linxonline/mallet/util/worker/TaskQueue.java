package com.linxonline.mallet.util.thread ;

import java.lang.Thread ;
import java.lang.StringBuilder ;
import java.util.concurrent.LinkedBlockingQueue ;

public class TaskQueue
{
	private final Thread[] threads ;
	private final LinkedBlockingQueue<Runnable> queue ;

	public TaskQueue( final int _threads, final String _name )
	{
		this( _threads, Integer.MAX_VALUE, _name ) ;
	}

	public TaskQueue( final int _threads, final int _capacity, final String _name )
	{
		final StringBuilder builder = new StringBuilder() ;
	
		queue = new LinkedBlockingQueue<Runnable>( _capacity ) ;
		threads = new Thread[_threads] ;
		for( int i = 0; i < _threads; ++i )
		{
			builder.setLength( 0 ) ;
			builder.append( _name ) ;
			builder.append( i ) ;

			threads[i] = new Thread( new Worker( queue ), builder.toString() ) ;
			threads[i].start() ;
		}
	}

	public boolean add( final Runnable _run )
	{
		try
		{
			queue.put( _run ) ;
			return true ;
		}
		catch( InterruptedException ex )
		{
			return false ;
		}
	}

	private static class Worker implements Runnable
	{
		private final LinkedBlockingQueue<Runnable> queue ;

		public Worker( final LinkedBlockingQueue<Runnable> _queue )
		{
			queue = _queue ;
		}

		@Override
		public void run()
		{
			while( true )
			{
				try
				{
					queue.take().run() ;
				}
				catch( InterruptedException ex )
				{
					continue ;
				}
			}
		}
	}
}
