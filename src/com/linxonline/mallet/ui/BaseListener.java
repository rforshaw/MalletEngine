package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.input.InputEvent ;

public abstract class BaseListener<T extends UIElement>
{
	private T parent ;

	public void setParent( final T _parent )
	{
		parent = _parent ;
	}

	public T getParent()
	{
		return parent ;
	}

	public void sendEvent( final Event<?> _event )
	{
		if( parent != null )
		{
			parent.addEvent( _event ) ;
		}
	}

	public InputEvent.Action pressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	public InputEvent.Action released( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	public InputEvent.Action move( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	public InputEvent.Action exited( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when parent UIElement is refreshing itself.
	*/
	public abstract void refresh() ;

	/**
		Called when parent UIElement has been flagged for shutdown.
		Clean up any resources you may have allocated.
	*/
	public abstract void shutdown() ;
}
