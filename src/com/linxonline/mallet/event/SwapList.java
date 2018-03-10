package com.linxonline.mallet.event ;

import java.util.List ;

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
	private List<T> newEvents = MalletList.<T>newList() ;
	private List<T> active = MalletList.<T>newList() ;

	public SwapList() {}
	
	public final void add( final T _t )
	{
		newEvents.add( _t ) ;
	}

	public final T getAt( final int _index )
	{
		return active.get( _index ) ;
	}
	
	public final List<T> getActiveList()
	{
		return active ;
	}

	public final int size()
	{
		return active.size() ;
	}

	public boolean isEmpty()
	{
		return ( active.isEmpty() && newEvents.isEmpty() ) ;
	}

	/**
		Swap the buffers and clear the old active buffer.
	**/
	public final void swap()
	{
		if( active.isEmpty() == false )
		{
			active.clear() ;
		}

		if( newEvents.isEmpty() == false )
		{
			final List<T> oldEvents = active ;
			active = newEvents ;
			newEvents = oldEvents ;
		}
	}

	public final void clear()
	{
		newEvents.clear() ;
		active.clear() ;
	}
}
