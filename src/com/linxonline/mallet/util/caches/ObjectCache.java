package com.linxonline.mallet.util.caches ;

import java.util.LinkedList ;

/**
	Provides a simple object cache to retrieve an object 
	that has been previously used.
	Objects used in this cache must have a default constructor.
*/
public class ObjectCache<T extends Cacheable> implements CacheInterface<T>
{
	private final LinkedList<T> available = new LinkedList<T>() ;	// Pool of objects to retrieve.
	private final Class<T> creator ;								// Allows the creation of new default objects.

	public ObjectCache( final Class<T> _creator )
	{
		creator = _creator ;
		expandCache( 10 ) ;
	}

	public ObjectCache( final Class<T> _creator, final int _size )
	{
		creator = _creator ;
		expandCache( _size ) ;
	}

	/**
		Expand the linked list to the specified size.
	*/
	private void expandCache( final int _size )
	{
		try
		{
			for( int i = 0; i < _size; ++i )
			{
				reclaim( creator.newInstance() ) ;
			}
		}
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }
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

		try
		{
			return creator.newInstance() ;
		}
		catch( InstantiationException ex ) { ex.printStackTrace() ; }
		catch( IllegalAccessException ex ) { ex.printStackTrace() ; }

		return null ;
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