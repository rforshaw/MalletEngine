package com.linxonline.mallet.util.caches ;

import java.util.LinkedList ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

/**
	Provide a cache of objects based on the Class defined.
	Objects used in this cache, must have a default constructor.
	An object is automatically returned when the object is 
	older than the wait time. 
*/
public class TimeCache<T extends Cacheable> implements CacheInterface<T>
{
	private final LinkedList<TimeWrapper> pool = new LinkedList<TimeWrapper>() ;	// Pool of objects that will be used.
	private final Class<T> objectCreator ;											// Used to create T type instances.
	private final float wait ;														// The amount of time an object can be used for without being reused.
	private int currentPos = 0 ;													// Current location within pool.

	private TimeWrapper<T> temp = null ;

	public TimeCache( final float _wait, Class<T> _class )
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
			temp.obj.reset() ;
			return temp.obj ;
		}

		final T obj = newInstance() ;
		if( obj != null )
		{
			insert( currentPos++, seconds, remainder, obj ) ;
		}

		return obj ;
	}

	/**
		Not used as TimePool will automatically reclaim, 
		used objects after a period of time has elapsed.
		No need for the developer to return it.
	*/
	@Override
	public void reclaim( final T _obj ) {}

	public T newInstance()
	{
		try
		{
			// Create new instance if one is not suitable to return.
			return objectCreator.newInstance() ;
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