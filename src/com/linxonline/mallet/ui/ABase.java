package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;

/**
	Implement the Base Interface storing the parent 
	as a local reference when setParent is called.

	Return PROPAGATE on input functions and provide 
	further functionality refresh and shutdown.
*/
public abstract class ABase<T extends UIElement> implements IBase<T>
{
	private T parent ;

	/**
		Called when listener is added to a UIElement.
		This should only be called by UIElement when the 
		listener is added to it.
	*/
	@Override
	public void setParent( final T _parent )
	{
		parent = _parent ;
		constructDraws() ;
	}

	/**
		Return the parent UIElement that this listener was 
		added to.
		Should return null if the listener has not or has been 
		removed from a UIElement.
	*/
	@Override
	public T getParent()
	{
		return parent ;
	}

	/**
		Send the event back-up the chain.
	*/
	@Override
	public void sendEvent( final Event<?> _event )
	{
		if( parent != null )
		{
			parent.addEvent( _event ) ;
		}
	}

	/**
		Called when the scroll is moved.
	*/
	@Override
	public InputEvent.Action scroll( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the mouse is moved.
	*/
	@Override
	public InputEvent.Action mouseMove( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the mouse has a trigger pressed.
	*/
	@Override
	public InputEvent.Action mousePressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the mouse has a trigger released.
	*/
	@Override
	public InputEvent.Action mouseReleased( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the user is moving a pressed finger 
		on the touch screen.
	*/
	@Override
	public InputEvent.Action touchMove( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a touch screen is pressed.
	*/
	@Override
	public InputEvent.Action touchPressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a touch screen is released.
	*/
	@Override
	public InputEvent.Action touchReleased( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a key is pressed.
	*/
	@Override
	public InputEvent.Action keyPressed( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a key is released.
	*/
	@Override
	public InputEvent.Action keyReleased( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when a joystick/gamepad stick is waggled.
	*/
	@Override
	public InputEvent.Action analogueMove( final InputEvent _input )
	{
		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Called when the parent UIElement is engaged.
		This can occur at any time.
	*/
	@Override
	public void engage() {}

	/**
		Called when the parent UIElement is no-longer engaged.
		This can occur at any time.
	*/
	@Override
	public void disengage() {}

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	@Override
	public void constructDraws() {}

	/**
		Called when parent UIElement is ready to recieve 
		draw requests.
	*/
	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world ) {}

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
