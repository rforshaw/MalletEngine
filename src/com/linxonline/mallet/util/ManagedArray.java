package com.linxonline.mallet.util ;

import java.util.List ;

/**
	Objects are only added to current state when 
	manageState() is called.
*/
public abstract class ManagedArray<T>
{
	private RemoveDelegate<T> FALLBACK = new RemoveDelegate<T>()
	{
		public void remove( final T _data ) {}
	} ;

	protected final List<T> toAdd = MalletList.<T>newList() ;
	protected final List<T> toRemove = MalletList.<T>newList() ;
	protected final List<T> current = MalletList.<T>newList() ;

	protected RemoveDelegate<T> removeDelegate = FALLBACK ;

	/**
		Set a custom delegate if the objects require specific 
		removal handling.
	*/
	public void setRemoveDelegate( final RemoveDelegate<T> _delegate )
	{
		removeDelegate = _delegate ;
		if( removeDelegate == null )
		{
			removeDelegate = FALLBACK ;
		}
	}

	public synchronized void add( final T _data )
	{
		if( _data != null )
		{
			toRemove.remove( _data ) ;
			toAdd.add( _data ) ;
		}
	}

	public synchronized void remove( final T _data )
	{
		if( _data != null )
		{
			toAdd.remove( _data ) ;
			toRemove.add( _data ) ;
		}
	}

	public List<T> getNewState()
	{
		final List<T> state = MalletList.<T>newList() ;
		state.addAll( current ) ;
		state.addAll( toAdd ) ;
		return state ;
	}

	public List<T> getCurrentData()
	{
		return current ;
	}

	public List<T> getToAddData()
	{
		return toAdd ;
	}

	/**
		Add new values that are in the toAdd list.
		Remove values that are in the remove list.
	*/
	public void manageState()
	{
		if( toAdd.isEmpty() == false )
		{
			addNewData( toAdd ) ;
		}

		if( toRemove.isEmpty() == false )
		{
			removeOldData( toRemove ) ;
		}
	}

	protected void addNewData( final List<T> _toAdd )
	{
		if( _toAdd.isEmpty() == false )
		{
			current.addAll( _toAdd ) ;
			_toAdd.clear() ;
		}
	}

	protected void removeOldData( final List<T> _toRemove )
	{
		if( _toRemove.isEmpty() == false )
		{
			final int size = _toRemove.size() ;
			for( int i = 0; i < size; i++ )
			{
				final T remove = _toRemove.get( i ) ;
				if( current.remove( remove ) == true )
				{
					removeDelegate.remove( remove ) ;
				}
			}
			_toRemove.clear() ;
		}
	}

	public interface RemoveDelegate<T>
	{
		public void remove( final T _data ) ;
	}
}