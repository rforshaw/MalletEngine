package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

public abstract class State<T>
{
	protected final ArrayList<T> toAdd = new ArrayList<T>() ;
	protected final ArrayList<T> toRemove = new ArrayList<T>() ;
	protected final ArrayList<T> current = new ArrayList<T>() ;

	protected RemoveDelegate removeDelegate = null ;

	public <T> void setRemoveDelegate( final RemoveDelegate<T> _delegate )
	{
		removeDelegate = _delegate ;
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
