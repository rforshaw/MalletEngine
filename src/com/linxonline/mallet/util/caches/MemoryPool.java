package com.linxonline.mallet.util.caches ;

import java.util.Vector ;

/**
	Create a pool of elements that can be shared.
	The pool does not retain ownership of the element
	once taken, if reclaim is not called the Java VM
	will deal with the element accordingly.
*/
public class MemoryPool<E> implements IPool<E>
{
	private final IPool.ICreator<E> creator ;
	private final Vector<E> pool ;

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
		creator = _creator ;
		pool = new Vector<E>( _initialSize, _initialSize ) ;
		populate() ;
	}

	public synchronized E takeSync()
	{
		return take() ;
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

	public synchronized boolean reclaimSync( final E _element )
	{
		return reclaim( _element ) ;
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
		if( pool.contains( _element ) == true )
		{
			return false ;
		}

		pool.add( _element ) ;
		return true ;
	}

	/**
		Populate the pool with new objects filling it
		to its capacity.
	*/
	private void populate()
	{
		final int size = pool.capacity() ;
		for( int i = 0; i < size; ++i )
		{
			pool.add( creator.create() ) ;
		}
	}
}
