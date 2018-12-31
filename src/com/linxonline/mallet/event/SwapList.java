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
	
	public void add( final T _t )
	{
		newEvents.add( _t ) ;
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

		if( newEvents.isEmpty() == false )
		{
			final List<T> oldEvents = active ;
			active = newEvents ;
			newEvents = oldEvents ;
		}
		
		return active ;
	}

	public void clear()
	{
		newEvents.clear() ;
		active.clear() ;
	}
}
