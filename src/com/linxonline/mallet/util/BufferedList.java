package com.linxonline.mallet.util ;

import java.util.List ;

/**
	Object instances are not directly added to the 
	main list.
	They are buffered and only added or removed once 
	update() has been called.
*/
public class BufferedList<T>
{
	private final AddListener<T> ADD_FALLBACK = new AddListener<T>()
	{
		public void add( final T _data ) {}
	} ;

	private final RemoveListener<T> REMOVE_FALLBACK = new RemoveListener<T>()
	{
		public void remove( final T _data ) {}
	} ;

	private final List<Task> tasks ;
	private final List<T> current ;

	private AddListener<T> addListener = ADD_FALLBACK ;
	private RemoveListener<T> removeListener = REMOVE_FALLBACK ;

	public BufferedList()
	{
		this( 10 ) ;
	}

	public BufferedList( final int _capacity )
	{
		tasks = MalletList.<Task>newList( _capacity ) ;
		current = MalletList.<T>newList( _capacity ) ;
	}

	public void setAddListener( final AddListener<T> _listener )
	{
		addListener = ( _listener != null ) ? _listener : ADD_FALLBACK ;
	}

	public void setRemoveListener( final RemoveListener<T> _listener )
	{
		removeListener = ( _listener != null ) ? _listener : REMOVE_FALLBACK ;
	}

	public synchronized void add( final T _data )
	{
		tasks.add( new Task()
		{
			@Override
			public void execute()
			{
				current.add( _data ) ;
				addListener.add( _data ) ;
			}
		} ) ;
	}

	public synchronized void remove( final T _data )
	{
		tasks.add( new Task()
		{
			@Override
			public void execute()
			{
				if( current.remove( _data ) == true )
				{
					removeListener.remove( _data ) ;
				}
			}
		} ) ;
	}

	public synchronized void insert( final T _data, final int _index )
	{
		tasks.add( new Task()
		{
			@Override
			public void execute()
			{
				if( _index < current.size() )
				{
					current.add( _index, _data ) ;
					return ;
				}

				current.add( _data ) ;
			}
		} ) ;
	}

	public List<T> getCurrentData()
	{
		return current ;
	}

	/**
		Add new values that are in the toAdd list.
		Remove values that are in the remove list.
	*/
	public void update()
	{
		if( tasks.isEmpty() == false )
		{
			final int size = tasks.size() ;
			for( int i = 0; i < size; i++ )
			{
				tasks.get( i ).execute() ;
			}
			tasks.clear() ;
		}
	}

	private interface Task
	{
		public void execute() ;
	}

	public interface RemoveListener<T>
	{
		public void remove( final T _data ) ;
	}

	public interface AddListener<T>
	{
		public void add( final T _data ) ;
	}
}
