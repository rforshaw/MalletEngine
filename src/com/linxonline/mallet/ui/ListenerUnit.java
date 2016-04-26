package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

public class ListenerUnit<T extends BaseListener>
{
	private final ArrayList<T> listeners = new ArrayList<T>() ;
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

	public ArrayList<T> getListeners()
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