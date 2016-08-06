package com.linxonline.mallet.util.arrays ;

import java.util.ArrayList ;

/**
	Objects are only added to current state when 
	manageState() is called.
*/
public abstract class ManagedArray<T>
{
	private RemoveDelegate FALLBACK = new RemoveDelegate<Object>()
	{
		public void remove( final Object _data ) {}
	} ;

	protected final ArrayList<T> toAdd = new ArrayList<T>() ;
	protected final ArrayList<T> toRemove = new ArrayList<T>() ;
	protected final ArrayList<T> current = new ArrayList<T>() ;

	protected RemoveDelegate removeDelegate = FALLBACK ;

	/**
		Set a custom delegate if the objects require specific 
		removal handling.
	*/
	public <T> void setRemoveDelegate( final RemoveDelegate<T> _delegate )
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
			toAdd.add( _data ) ;
		}
	}

	public synchronized void remove( final T _data )
	{
		if( _data != null )
		{
			toRemove.add( _data ) ;
		}
	}

	public ArrayList<T> getCurrentData()
	{
		return current ;
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

	protected void addNewData( final ArrayList<T> _toAdd )
	{
		for( final T add : _toAdd )
		{
			current.add( add ) ;
		}
		_toAdd.clear() ;
	}

	protected void removeOldData( final ArrayList<T> _toRemove )
	{
		for( final T remove : _toRemove )
		{
			if( current.remove( remove ) == true )
			{
				removeDelegate.remove( remove ) ;
			}
		}
		_toRemove.clear() ;
	}

	public interface RemoveDelegate<T>
	{
		public void remove( final T _data ) ;
	}
}