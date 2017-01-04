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
		Called when the mouse is moved.
	*/
	public InputEvent.Action mouseMove( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the mouse has a trigger pressed.
	*/
	public InputEvent.Action mousePressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the mouse has a trigger released.
	*/
	public InputEvent.Action mouseReleased( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the user is moving a pressed finger 
		on the touch screen.
	*/
	public InputEvent.Action touchMove( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a touch screen is pressed.
	*/
	public InputEvent.Action touchPressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a touch screen is released.
	*/
	public InputEvent.Action touchReleased( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a key is pressed.
	*/
	public InputEvent.Action keyPressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a key is released.
	*/
	public InputEvent.Action keyReleased( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a joystick/gamepad stick is waggled.
	*/
	public InputEvent.Action analogueMove( final InputEvent _input )
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
