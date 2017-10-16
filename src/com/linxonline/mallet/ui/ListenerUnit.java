package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public class ListenerUnit<T extends IBase<? extends UIElement>>
{
	private final List<T> listeners = MalletList.<T>newList() ;

	public ListenerUnit() {}

	public boolean add( final T _listener )
	{
		if( _listener != null )
		{
			if( listeners.contains( _listener ) == false )
			{
				listeners.add( _listener ) ;
				return true ;
			}
		}
		return false ;
	}

	public boolean add( final int _index, final T _listener )
	{
		if( _listener != null )
		{
			if( listeners.contains( _listener ) == false )
			{
				listeners.add( _index, _listener ) ;
				return true ;
			}
		}
		return false ;
	}

	public boolean remove( final T _listener )
	{
		if( listeners.remove( _listener ) == true )
		{
			//_listener.setParent( null ) ;
			return true ;
		}
		return false ;
	}

	public List<T> getListeners()
	{
		return listeners ;
	}

	public void refresh()
	{
		final int size = listeners.size() ;
		for( int i = 0; i < size; i++ )
		{
			listeners.get( i ).refresh() ;
		}
	}

	public void shutdown()
	{
		final int size = listeners.size() ;
		for( int i = 0; i < size; i++ )
		{
			listeners.get( i ).shutdown() ;
		}
	}

	public void clear()
	{
		listeners.clear() ;
	}
}
