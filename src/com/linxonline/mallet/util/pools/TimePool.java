package com.linxonline.mallet.util.pools ;

import java.util.LinkedList ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

/**
	Provides a cache of objects based on time elapsed.
*/
public class TimePool<T> implements PoolInterface<T>
{
	private final LinkedList<TimeWrapper> pool = new LinkedList<TimeWrapper>() ;	// Pool of objects that will be used.
	private final Class<T> objectCreator ;											// Used to create T type instances.
	private float wait = 0.0f ;														// The amount of time an object can be used for without being reused.
	private int currentPos = 0 ;													// Current location within pool.

	private TimeWrapper<T> temp = null ;

	public TimePool( final float _wait, Class<T> _class )
	{
		objectCreator = _class ;
		wait = _wait ;

		try { add( 0, 0.0f, objectCreator.newInstance() ) ; }
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }

	}

	@Override
	public T get()
	{
		// Ensure currentPos is within the pool limits.
		if( currentPos >= pool.size() ) { currentPos = 0 ; }

		final long seconds = ElapsedTimer.getTotalElapsedTimeInSeconds() ;
		final double remainder = ElapsedTimer.getRemainderInNanoSeconds() ;

		temp = pool.get( currentPos ) ;
		final float diff = ( float )( seconds - temp.seconds ) + ( float )( remainder - temp.remainder ) ;

		if( diff >= wait )
		{
			++currentPos ;
			temp.seconds = seconds ;
			temp.remainder = remainder ;
			return temp.obj ;
		}

		try
		{
			// Create new instance if one is not suitable to return.
			return ( T )insert( currentPos++, seconds, remainder, objectCreator.newInstance() ).obj ;
		}
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }

		return null ;
	}

	@Override
	public int size()
	{
		return pool.size() ;
	}

	@Override
	public void trimTo( final int _size )
	{
		while( pool.size() > _size )
		{
			pool.pop() ;
		}
	}

	/**
		Adds T onto the end of pool.
	**/
	private void add( final long _seconds, final double _remainder, final T _t )
	{
		final TimeWrapper wrapper = new TimeWrapper( _seconds, _remainder, _t ) ;
		pool.add( wrapper ) ;
	}

	/**
		Insert T into specified location of pool.
		Pushes everything from that position to the right by 1.
	**/
	private TimeWrapper insert( final int _insert, final long _seconds, final double _remainder, final T _t )
	{
		final TimeWrapper wrapper = new TimeWrapper( _seconds, _remainder, _t ) ;
		pool.add( _insert, wrapper ) ;
		return wrapper ;
	}

	protected class TimeWrapper<T>
	{
		public long seconds ;		// Total time elapsed in seconds
		public double remainder ;	// Defines the nano-seconds elapsed that is yet a second.
		public final T obj ;

		public TimeWrapper( final long _seconds, final double _remainder, final T _obj )
		{
			seconds = _seconds ;
			remainder = _remainder ;
			obj = _obj ;
		}
	}
}