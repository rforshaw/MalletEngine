package com.linxonline.mallet.util ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.MalletMap ;

public final class Debounce implements IGameSystem.IUpdate
{
	private final static Map<Runnable, Op> lookup = MalletMap.<Runnable, Op>newMap() ;
	private final static BufferedList<Op> operations = new BufferedList<Op>() ;

	private static boolean init = false ;

	public Debounce()
	{
		// This constructor must be called at least once
		// and added to the GameSystem main-loop.
		init = true ;
	}

	/**
		Execute the runnable in 0.1 seconds if not called again.
	*/
	public static void debounce( Runnable _runnable )
	{
		debounce( 0.1f, _runnable ) ;
	}

	/**
		Debounce allows the user to trigger an action once, after
		a period of time has alloted. The runnable will only be called
		when the caller of the debounce operation has stopped calling it.
		NOTE: Make sure each time you call debounce you pass in the same runnable,
		else you'll have multiple runnables be called once the duration has been met.
	*/
	public static void debounce( final float _duration, Runnable _runnable )
	{
		if( init == false )
		{
			Logger.println( "Debounce has not been initialised and added to the main-loop.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		synchronized( lookup )
		{		
			Op op = lookup.get( _runnable ) ;
			if( op != null )
			{
				op.reset() ;
				return ;
			}

			// Op objects should probably be cached to reduce garbage collections.
			op = new Op( _duration, _runnable ) ;
			lookup.put( _runnable, op ) ;
			operations.add( op ) ;
		}
	}

	/**
		Called by the main-loop do not call this function manually.
	*/
	@Override
	public void update( final double _dt )
	{
		synchronized( lookup )
		{
			operations.update() ;
			final float dt = ( float )_dt ;

			final List<Op> current = operations.getCurrentData() ;
			final int size = current.size() ;
			for( int i = 0; i < size; ++i  )
			{
				final Op op = current.get( i ) ;
				if( op.shouldExecute( dt ) )
				{
					// Remove the operation from the list,
					// as it's possible that executing the operation
					// could cause the same operation to be added back in.
					operations.remove( op ) ;
					lookup.remove( op.getRunnable() ) ;
					op.execute( dt ) ;
				}
			}
		}
	}

	private static final class Op
	{
		private final Runnable runnable ;
		private final float duration ;
		private float elapsed = 0.0f ;

		public Op( final float _duration, final Runnable _runnable )
		{
			duration = _duration ;
			runnable = _runnable ;
		}

		public Runnable getRunnable()
		{
			return runnable ;
		}

		public boolean shouldExecute( final float _dt )
		{
			elapsed += _dt ;
			if( elapsed < duration )
			{
				return false ;
			}

			return true ;
		}
		
		public void execute( final float _dt )
		{
			runnable.run() ;
		}

		public void reset()
		{
			elapsed = 0.0f ;
		}
	}
}
