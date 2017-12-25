package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;

public interface IBase<T extends UIElement>
{
	/**
		Called when listener is added to a UIElement.
		This should only be called by UIElement when the 
		listener is added to it.
	*/
	public void setParent( final T _parent ) ;

	/**
		Return the parent UIElement that this listener was 
		added to.
		Should return null if the listener has not or has been 
		removed from a UIElement.
	*/
	public T getParent() ;

	/**
		Send the event back-up the chain.
	*/
	public void sendEvent( final Event<?> _event ) ;

	/**
		Called when the scroll is moved.
	*/
	public InputEvent.Action scroll( final InputEvent _input ) ;

	/**
		Called when the mouse is moved.
	*/
	public InputEvent.Action mouseMove( final InputEvent _input ) ;

	/**
		Called when the mouse has a trigger pressed.
	*/
	public InputEvent.Action mousePressed( final InputEvent _input ) ;

	/**
		Called when the mouse has a trigger released.
	*/
	public InputEvent.Action mouseReleased( final InputEvent _input ) ;

	/**
		Called when the user is moving a pressed finger 
		on the touch screen.
	*/
	public InputEvent.Action touchMove( final InputEvent _input ) ;

	/**
		Called when a touch screen is pressed.
	*/
	public InputEvent.Action touchPressed( final InputEvent _input ) ;

	/**
		Called when a touch screen is released.
	*/
	public InputEvent.Action touchReleased( final InputEvent _input ) ;

	/**
		Called when a key is pressed.
	*/
	public InputEvent.Action keyPressed( final InputEvent _input ) ;

	/**
		Called when a key is released.
	*/
	public InputEvent.Action keyReleased( final InputEvent _input ) ;

	/**
		Called when a joystick/gamepad stick is waggled.
	*/
	public InputEvent.Action analogueMove( final InputEvent _input ) ;

	/**
		Can be used to construct Draw objects before a 
		DrawDelegate is provided by the Rendering System.
	*/
	public void constructDraws() ;

	/**
		Called when parent UIElement is ready to recieve 
		draw requests.
	*/
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world ) ;

	/**
		Called when parent UIElement is refreshing itself.
	*/
	public void refresh() ;

	/**
		Called when parent UIElement has been flagged for shutdown.
		Clean up any resources you may have allocated.
	*/
	public void shutdown() ;

	public interface Meta extends Connect.Connection
	{
		public String getType() ;
	}
}
