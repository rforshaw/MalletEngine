package com.linxonline.mallet.util.caches ;

import java.util.ArrayDeque ;

import java.lang.reflect.Constructor ;
import java.lang.reflect.InvocationTargetException ;

/**
	Provides a simple object cache to retrieve an object 
	that has been previously used.
	Objects used in this cache must have a default constructor.
*/
public class ObjectCache<T extends Cacheable> implements ICache<T>
{
	private final ArrayDeque<T> available = new ArrayDeque<T>() ;	// Pool of objects to retrieve.
	private final Constructor<T> creator ;							// Allows the creation of new default objects.

	public ObjectCache( final Class<T> _class ) throws NoSuchMethodException
	{
		this( _class, 10 ) ;
	}

	public ObjectCache( final Class<T> _class, final int _size ) throws NoSuchMethodException
	{
		creator = _class.getConstructor() ;
		expandCache( _size ) ;
	}

	/**
		Expand the linked list to the specified size.
	*/
	private void expandCache( final int _size )
	{
		for( int i = 0; i < _size; ++i )
		{
			reclaim( newInstance() ) ;
		}
	}

	/**
		Return an object from the pool, or create a new one
		if the pool contains none.
		Requires the object to have a default constructor.
	*/
	public T get()
	{
		if( size() > 0 )
		{
			return available.pop() ;
		}

		return newInstance() ;
	}

	/**
		Return the object back to the pool.
		Before returning, ensure the object is reset.
	*/
	public void reclaim( final T _obj )
	{
		_obj.reset() ;
		available.push( _obj ) ;
	}

	public T newInstance()
	{
		try
		{
			return creator.newInstance() ;
		}
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }
		catch( InvocationTargetException ex ) { ex.printStackTrace() ; }

		return null ;
	}

	public int size()
	{
		return available.size() ;
	}

	/**
		Trim the pool size to the specified size.
	*/
	public void trimTo( final int _size )
	{
		while( available.size() > _size )
		{
			available.pop() ;
		}
	}
}
