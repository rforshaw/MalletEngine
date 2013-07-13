package com.linxonline.mallet.util.pools ;

import java.util.LinkedList ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

public class TimePool<T>
{
	private final LinkedList<TimeWrapper> pool = new LinkedList<TimeWrapper>() ;	// Pool of objects that will be used.
	private final Class<T> objectCreator ;						// Used to create T type instances.
	private float wait = 0.0f ;									// The amount of time an object can be used for without being reused.
	private int currentPos = 0 ;								// Current location within pool.

	private TimeWrapper<T> temp = null ;

	public TimePool( final float _wait, Class<T> _class )
	{
		objectCreator = _class ;
		wait = _wait ;

		try { add( 0, 0.0f, objectCreator.newInstance() ) ; }
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }

	}

	public T get()
	{
		//System.out.println( pool.size() ) ;
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
			return ( T )insert( currentPos++, seconds, remainder, objectCreator.newInstance() ).obj ;
		}
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }

		return null ;
	}

	public int size()
	{
		return pool.size() ;
	}

	private void add( final long _seconds, final double _remainder, final T _t )
	{
		final TimeWrapper wrapper = new TimeWrapper( _seconds, _remainder, _t ) ;
		pool.add( wrapper ) ;
	}

	private TimeWrapper insert( final int _insert, final long _seconds, final double _remainder, final T _t )
	{
		final TimeWrapper wrapper = new TimeWrapper( _seconds, _remainder, _t ) ;
		pool.add( _insert, wrapper ) ;
		return wrapper ;
	}
	
	protected class TimeWrapper<T>
	{
		public long seconds ;
		public double remainder ;
		public final T obj ;

		public TimeWrapper( final long _seconds, final double _remainder, final T _obj )
		{
			seconds = _seconds ;
			remainder = _remainder ;
			obj = _obj ;
		}
	}
}