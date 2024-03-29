package com.linxonline.mallet.util.notification ;

import java.util.List ;

import com.linxonline.mallet.util.Debounce ;
import com.linxonline.mallet.util.MalletList ;

/**
	Convenience class that allows one system to inform 
	multiple other systems that it has completed a 
	predefined task.
	The notification can pass to the listeners any relevant 
	information it deams fit.
*/
public class Notification<T>
{
	private final List<Notify<T>> listeners ;
	private final Inform informer = new Inform() ;

	public Notification()
	{
		listeners = MalletList.<Notify<T>>newList() ;
	}

	public Notification( final int _initialCapacity )
	{
		listeners = MalletList.<Notify<T>>newList( _initialCapacity ) ;
	}

	public void addNotify( final Notify<T> _toInform )
	{
		if( listeners.contains( _toInform ) == false )
		{
			listeners.add( _toInform ) ;
		}
	}

	public void removeNotify( final Notify<T> _toInform )
	{
		listeners.remove( _toInform ) ;
	}

	public void informOnce()
	{
		informOnce( null ) ;
	}

	public void informOnce( final T _data )
	{
		inform( _data ) ;
		clear() ;
	}

	public void inform()
	{
		inform( null ) ;
	}

	public void inform( final T _data )
	{
		informer.setData( _data ) ;
		Debounce.debounce( informer ) ;
	}

	public void clear()
	{
		listeners.clear() ;
	}

	private class Inform implements Runnable
	{
		private T data ;

		public void setData( final T _data )
		{
			data = _data ;
		}

		@Override
		public void run()
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; ++i )
			{
				listeners.get( i ).inform( data ) ;
			}
		}
	}
	
	public interface Notify<T>
	{
		public void inform( final T _data ) ;
	}
}
