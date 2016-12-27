package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.input.InputEvent ;

public abstract class InputListener<T extends UIElement> extends BaseListener<T>
{
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
	public void refresh() {}

	/**
		Called when parent UIElement has been flagged for shutdown.
		Clean up any resources you may have allocated.
	*/
	public void shutdown() {}
}
