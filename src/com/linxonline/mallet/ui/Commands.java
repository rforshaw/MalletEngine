package com.linxonline.mallet.ui ;

import java.util.ArrayDeque ;

public final class Commands
{
	private final int capacity ;
	private final ArrayDeque<ICommand> undo ;
	private final ArrayDeque<ICommand> redo ;

	public Commands()
	{
		this( 100 ) ;
	}

	public Commands( final int _capacity )
	{
		capacity = _capacity ;
		undo = new ArrayDeque<ICommand>( capacity ) ;
		redo = new ArrayDeque<ICommand>( capacity ) ;
	}

	public void apply( final ICommand _com )
	{
		redo.clear() ;
		if( _com.apply() )
		{
			if( undo.size() >= capacity )
			{
				undo.pollLast() ;
			}

			undo.push( _com ) ;
		}
	}

	public void undo()
	{
		if( undo.isEmpty() )
		{
			return ;
		}

		final ICommand com = undo.pop() ;
		if( com.undo() )
		{
			if( redo.size() >= capacity )
			{
				redo.pollLast() ;
			}

			redo.push( com ) ;
		}
	}

	public void redo()
	{
		if( redo.isEmpty() )
		{
			return ;
		}

		final ICommand com = redo.pop() ;
		if( com.apply() )
		{
			if( undo.size() >= capacity )
			{
				undo.pollLast() ;
			}

			undo.push( com ) ;
		}
	}

	// Return false if the operation fails
	// to be applied or undone.
	public interface ICommand
	{
		public boolean apply() ;
		public boolean undo() ;
	}
}
