package com.linxonline.mallet.util.caches ;

import java.util.List ;

import com.linxonline.mallet.util.time.ElapsedTimer ;
import com.linxonline.mallet.util.MalletList ;

/**
	Provide a pool of objects that can be taken.
	They will be reclaimed automatically after a set duration.
*/
public class TimePool<E> implements IPool<E>
{
	private final IPool.ICreator<E> creator ;

	private final List<TimeWrapper<E>> pool ;
	private final float wait ;
	private int currentPos = 0 ;

	public TimePool( final float _wait, IPool.ICreator<E> _creator )
	{
		this( 10, _wait, _creator ) ;
	}
	
	public TimePool( final int _capacity, final float _wait, IPool.ICreator<E> _creator )
	{
		pool = MalletList.<TimeWrapper<E>>newList( _capacity ) ;
		for( int i = 0; i < _capacity; ++i )
		{
			pool.add( new TimeWrapper<E>( 0, 0, _creator.create() ) ) ;
		}

		creator = _creator ;
		wait = _wait ;
	}

	@Override
	public E take()
	{
		if( currentPos >= pool.size() )
		{
			currentPos = 0 ;
		}

		final long seconds = ElapsedTimer.getTotalElapsedTimeInSeconds() ;
		final double remainder = ElapsedTimer.getRemainderInNanoSeconds() ;

		final TimeWrapper<E> temp = pool.get( currentPos ) ;
		if( temp != null && temp.calcDiff( seconds, remainder ) >= wait )
		{
			++currentPos ;
			temp.seconds = seconds ;
			temp.remainder = remainder ;

			return temp.obj ;
		}

		final E obj = creator.create() ;
		pool.add( currentPos++, new TimeWrapper<E>( seconds, remainder, obj ) ) ;
		return obj ;
	}

	@Override
	public boolean reclaim( final E _element )
	{
		return true ;
	}

	private static class TimeWrapper<E>
	{
		public long seconds ;		// Total time elapsed in seconds
		public double remainder ;	// Defines the nano-seconds elapsed that is yet a second.
		public final E obj ;

		public TimeWrapper( final long _seconds, final double _remainder, final E _obj )
		{
			seconds = _seconds ;
			remainder = _remainder ;
			obj = _obj ;
		}

		public float calcDiff( final long _seconds, final double _remainder )
		{
			return ( float )( _seconds - seconds ) + ( float )( _remainder - remainder ) ;
		}
	}
}
