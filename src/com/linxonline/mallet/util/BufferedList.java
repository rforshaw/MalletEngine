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

	private final int capacity ;
	private List<Task> tasks ;
	private final List<T> current ;

	private AddListener<T> addListener = ADD_FALLBACK ;
	private RemoveListener<T> removeListener = REMOVE_FALLBACK ;

	public BufferedList()
	{
		this( 10 ) ;
	}

	public BufferedList( final int _capacity )
	{
		capacity = _capacity ;
		tasks = MalletList.<Task>newList( capacity ) ;
		current = MalletList.<T>newList( capacity ) ;
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
		tasks.add( new AddTask( _data ) ) ;
	}

	public synchronized void remove( final T _data )
	{
		tasks.add( new RemoveTask( _data ) ) ;
	}

	public synchronized void insert( final T _data, final int _index )
	{
		tasks.add( new InsertTask( _data, _index ) ) ;
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
		if( tasks.isEmpty() == true )
		{
			return ;
		}

		final int size = tasks.size() ;
		for( int i = 0; i < size; i++ )
		{
			tasks.get( i ).execute() ;
		}
		tasks.clear() ;

		if( size > capacity )
		{
			// If the size of tasks exceeds our capacity then 
			// we want to resize the array - it's easy for an 
			// array to expand, it's much harder to shrink it!
			tasks = MalletList.<Task>newList( capacity ) ;
		}
	}

	private interface Task
	{
		public void execute() ;
	}

	private class AddTask implements Task
	{
		private final T data ;

		public AddTask( final T _data )
		{
			data = _data ;
		}

		@Override
		public void execute()
		{
			current.add( data ) ;
			addListener.add( data ) ;
		}
	}

	private class RemoveTask implements Task
	{
		private final T data ;

		public RemoveTask( final T _data )
		{
			data = _data ;
		}

		@Override
		public void execute()
		{
			if( current.remove( data ) == true )
			{
				removeListener.remove( data ) ;
			}
		}
	}

	private class InsertTask implements Task
	{
		private final T data ;
		private final int index ;

		public InsertTask( final T _data, final int _index )
		{
			data = _data ;
			index = _index ;
		}

		@Override
		public void execute()
		{
			if( index < current.size() )
			{
				current.add( index, data ) ;
				return ;
			}

			current.add( data ) ;
		}
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
