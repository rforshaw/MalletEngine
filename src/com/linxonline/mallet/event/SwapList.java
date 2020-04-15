package com.linxonline.mallet.event ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

/**
	Double buffer effectively, prevents items from being added to 
	an array that is being processed, reduces the chance of an infinite loop or crash.

	Use in order:
		swap() ;
		process items
**/
public final class SwapList<T>
{
	private final int capacity ;
	private ArrayList<T> newEvents = null ;
	private ArrayList<T> active = null ;

	public SwapList()
	{
		this( 10 ) ;
	}

	public SwapList( int _initialCapacity )
	{
		capacity = _initialCapacity ;
		newEvents = new ArrayList<T>( capacity ) ;
		active = new ArrayList<T>( capacity ) ;
	}
	
	public void add( final T _t )
	{
		synchronized( newEvents )
		{
			newEvents.add( _t ) ;
		}
	}

	public void addAll( final List<T> _ts )
	{
		synchronized( newEvents )
		{
			newEvents.addAll( _ts ) ;
		}
	}

	public int size()
	{
		return active.size() + newEvents.size() ;
	}

	public boolean isEmpty()
	{
		return ( active.isEmpty() && newEvents.isEmpty() ) ;
	}

	/**
		Swap the buffers and clear the old active buffer.
		Return the new active list.
	**/
	public List<T> swap()
	{
		if( active.isEmpty() == false )
		{
			active.clear() ;
		}

		List<T> toReturn = active ;
		final int size = newEvents.size() ;
		if( size > 0 )
		{
			final ArrayList<T> oldEvents = active ;
			active = newEvents ;
			newEvents = oldEvents ;
			toReturn = active ;

			if( size > capacity )
			{
				// If the size of events exceeds our capacity then 
				// we want to resize the array - it's easy for an 
				// array to expand, it's much harder to shrink it!
				newEvents = new ArrayList<T>( capacity ) ;
				active = new ArrayList<T>( capacity ) ;
			}
		}

		return toReturn ;
	}

	public void clear()
	{
		newEvents.clear() ;
		active.clear() ;
	}
}
