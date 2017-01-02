package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.input.InputEvent ;

public abstract class BaseListener<T extends UIElement>
{
	private T parent ;

	/**
		Called when listener is added to a UIElement.
		This should only be called by UIElement when the 
		listener is added to it.
	*/
	public void setParent( final T _parent )
	{
		parent = _parent ;
	}

	/**
		Return the parent UIElement that this listener was 
		added to.
		Should return null if the listener has not or has been 
		removed from a UIElement.
	*/
	public T getParent()
	{
		return parent ;
	}

	/**
		Send the event back-up the chain.
	*/
	public void sendEvent( final Event<?> _event )
	{
		if( parent != null )
		{
			parent.addEvent( _event ) ;
		}
	}

	/**
		Called when a key/button has been pressed.
		This includes KEYBOARD_PRESSED, MOUSEX_PRESSED,
		and GAMEPAD_PRESSED events. 
	*/
	public InputEvent.Action pressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a key/button has been released.
		This includes KEYBOARD_RELEASED, MOUSEX_RELEASED,
		and GAMEPAD_RELEASED events.
	*/
	public InputEvent.Action released( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a move event is generated.
		This can either be a TOUCH_MOVED, MOUSE_MOVED, 
		or GAMEPAD_ANALOGUE event.
	*/
	public InputEvent.Action move( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the parent UIElement is engaged.
		This can occur at any time.
	*/
	public void engage() {}

	/**
		Called when the parent UIElement is no-longer engaged.
		This can occur at any time.
	*/
	public void disengage() {}

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
