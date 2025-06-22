package com.linxonline.mallet.util.caches ;

import java.util.ArrayList ;

/**
	Create a pool of elements that can be shared.
	The pool does not retain ownership of the element
	once taken, if reclaim is not called the Java VM
	will deal with the element accordingly.
*/
public class MemoryPool<E> implements IPool<E>, IPoolSync<E>
{
	private final IPool.ICreator<E> creator ;
	private final ArrayList<E> pool ;

	private int capacity ;

	public MemoryPool( final IPool.ICreator<E> _creator )
	{
		this( 10, _creator ) ;
	}

	/**
		Create a number of elements from the _creator based
		on the initial size of the pool.
	*/
	public MemoryPool( final int _initialSize, final IPool.ICreator<E> _creator )
	{
		capacity = _initialSize ;

		creator = _creator ;
		pool = new ArrayList<E>( capacity ) ;
		populate() ;
	}

	@Override
	public synchronized E takeSync()
	{
		return take() ;
	}

	@Override
	public synchronized E[] takeSync( final E[] _fill )
	{
		for( int i = 0; i < _fill.length; ++i )
		{
			_fill[i] = take() ;
		}

		return _fill ;
	}

	/**
		Return an element from the pool.
		If the pool is empty fill it up with the initial size again.
	*/
	@Override
	public E take()
	{
		if( pool.isEmpty() )
		{
			populate() ;
		}

		return pool.remove( 0 ) ;
	}

	@Override
	public synchronized boolean reclaimSync( final E _element )
	{
		return reclaim( _element ) ;
	}

	@Override
	public synchronized boolean reclaimSync( final E[] _elements )
	{
		for( int i = 0; i < _elements.length; ++i )
		{
			reclaim( _elements[i] ) ;
		}
		return true ;
	}

	/**
		Allow an element to be reclaimed by the pool.
		Any elements given back to the pool are expected not
		to be referenced by the previous owner.
		NOTE: You can give the pool element not created by the pool.
	*/
	@Override
	public boolean reclaim( final E _element )
	{
		pool.add( _element ) ;
		return true ;
	}

	/**
		Populate the pool with new objects filling it
		to its capacity.
	*/
	private void populate()
	{
		final int size = capacity ;
		for( int i = 0; i < size; ++i )
		{
			pool.add( creator.create() ) ;
		}
	}
}
