package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

public class ListenerUnit<T extends BaseListener>
{
	private final List<T> listeners = MalletList.<T>newList() ;
	private final UIElement parent ;

	public ListenerUnit( final UIElement _parent )
	{
		parent = _parent ;
	}

	public void addListener( final T _listener )
	{
		if( _listener != null )
		{
			if( listeners.contains( _listener ) == false )
			{
				listeners.add( _listener ) ;
				_listener.setParent( parent ) ;
			}
		}
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
