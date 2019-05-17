package com.linxonline.mallet.util.caches ;

import java.util.LinkedList ;

import java.lang.reflect.Constructor ;
import java.lang.reflect.InvocationTargetException ;

import com.linxonline.mallet.util.time.ElapsedTimer ;
import com.linxonline.mallet.util.Logger ;

/**
	Provide a cache of objects based on the Class defined.
	Objects used in this cache, must have a default constructor.
	An object is automatically returned when the object is 
	older than the wait time. 
*/
public class TimeCache<T extends Cacheable> implements ICache<T>
{
	private final LinkedList<TimeWrapper<T>> pool = new LinkedList<TimeWrapper<T>>() ;	// Pool of objects that will be used.
	private final float wait ;															// The amount of time an object can be used for without being reused.
	private Constructor<T> creator ;												// Used to create T type instances.
	private int currentPos = 0 ;														// Current location within pool.

	public TimeCache( final float _wait, final Class<T> _class, final T[] _items )
	{
		try
		{
			creator = _class.getConstructor() ;
		}
		catch( NoSuchMethodException ex )
		{
			Logger.println( "Failed to acquire valid item constructor for cache.", Logger.Verbosity.MAJOR ) ;
			creator = null ;
		}
		wait = _wait ;

		for( T item : _items )
		{
			add( 0, 0.0f, item ) ;
		}
	}

	@Override
	public T get()
	{
		if( currentPos >= pool.size() ) { currentPos = 0 ; }

		final long seconds = ElapsedTimer.getTotalElapsedTimeInSeconds() ;
		final double remainder = ElapsedTimer.getRemainderInNanoSeconds() ;
		final TimeWrapper<T> temp = pool.get( currentPos ) ;

		// If we can't create a new item then we have to 
		// accept what we've got.
		if( creator == null )
		{
			++currentPos ;
			temp.seconds = seconds ;
			temp.remainder = remainder ;
			temp.obj.reset() ;
			return temp.obj ;
		}

		// If there is a creator we can hopefully create a new one..
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
		if( obj == null )
		{
			// Our creator has failed us, we can only use the items 
			// that are currently in the pool.
			creator = null ;
			return get() ;
		}

		insert( currentPos++, seconds, remainder, obj ) ;
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
			return creator.newInstance() ;
		}
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }
		catch( InvocationTargetException ex ) { ex.printStackTrace() ; }

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
		pool.add( new TimeWrapper<T>( _seconds, _remainder, _t ) ) ;
	}

	/**
		Insert T into specified location of pool.
		Pushes everything from that position to the right by 1.
	**/
	private TimeWrapper<T> insert( final int _insert, final long _seconds, final double _remainder, final T _t )
	{
		final TimeWrapper<T> wrapper = new TimeWrapper<T>( _seconds, _remainder, _t ) ;
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
