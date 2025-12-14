package com.linxonline.mallet.util ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

/**
	Object instances are not directly added to the 
	main list.
	They are buffered and only added or removed once 
	update() has been called.
*/
public final class BufferedList<T>
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
	private ArrayList<Task> tasks ;

	private int currentSize = 0 ;
	private final ArrayList<T> current ;

	private AddListener<T> addListener = ADD_FALLBACK ;
	private RemoveListener<T> removeListener = REMOVE_FALLBACK ;

	public BufferedList()
	{
		this( 10 ) ;
	}

	public BufferedList( final int _capacity )
	{
		capacity = _capacity ;
		tasks = new ArrayList<Task>( capacity ) ;
		current = new ArrayList<T>( capacity ) ;
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

	public synchronized void removeAll( final T _data )
	{
		tasks.add( new RemoveAllTask( _data ) ) ;
	}

	public synchronized void insert( final T _data, final int _index )
	{
		tasks.add( new InsertTask( _data, _index ) ) ;
	}

	public synchronized boolean contains( final T _data )
	{
		return tasks.contains( _data ) ;
	}

	public List<T> getCurrentData()
	{
		return current ;
	}

	public int getTaskSize()
	{
		return tasks.size() ;
	}

	/**
		Add new values that are in the toAdd list.
		Remove values that are in the remove list.
	*/
	public void update()
	{
		final int taskSize = tasks.size() ;
		for( int i = 0; i < taskSize; i++ )
		{
			tasks.get( i ).execute() ;
		}

		if( currentSize > taskSize )
		{
			current.trimToSize() ;
		}

		if( taskSize > capacity )
		{
			// If the size of tasks exceeds our capacity then 
			// we want to resize the array - it's easy for an 
			// array to expand, it's much harder to shrink it!
			tasks = new ArrayList<Task>( capacity ) ;
			return ;
		}

		tasks.clear() ;
	}

	private abstract class Task
	{
		protected final T data ;

		public Task( final T _data )
		{
			data = _data ;
		}

		public abstract void execute() ;

		@Override
		public int hashCode()
		{
			return data.hashCode() ;
		}

		@Override
		public boolean equals( final Object _obj )
		{
			return data.equals( _obj ) ;
		}
	}

	private final class AddTask extends Task
	{
		public AddTask( final T _data )
		{
			super( _data ) ;
		}

		@Override
		public void execute()
		{
			current.add( data ) ;
			addListener.add( data ) ;

			currentSize = current.size() ;
		}
	}

	private final class RemoveTask extends Task
	{
		public RemoveTask( final T _data )
		{
			super( _data ) ;
		}

		@Override
		public void execute()
		{
			if( current.remove( data ) == true )
			{
				currentSize = current.size() ;
				removeListener.remove( data ) ;
			}
		}
	}

	private final class RemoveAllTask extends Task
	{
		public RemoveAllTask( final T _data )
		{
			super( _data ) ;
		}

		@Override
		public void execute()
		{
			Iterator<T> iter = current.iterator() ;
			while( iter.hasNext() )
			{
				final T val = iter.next() ;
				if( val.equals( data ) )
				{
					iter.remove() ;
					removeListener.remove( data ) ;
				}
				
			}
		}
	}

	private final class InsertTask extends Task
	{
		private final int index ;

		public InsertTask( final T _data, final int _index )
		{
			super( _data ) ;
			index = _index ;
		}

		@Override
		public void execute()
		{
			if( index < current.size() )
			{
				current.add( index, data ) ;
				currentSize = current.size() ;
				return ;
			}

			current.add( data ) ;
			currentSize = current.size() ;
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
