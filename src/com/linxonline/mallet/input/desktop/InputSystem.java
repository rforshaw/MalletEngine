package com.linxonline.mallet.input.desktop ;

import java.util.* ;

import com.jogamp.newt.event.* ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.util.caches.TimeCache ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

/**
	Input System is designed to use Java's built in input listeners, 
	requires to be added to a Window/Jframe to begin recieving input.
**/
public class InputSystem implements IInputSystem, 
									KeyListener, 
									MouseListener
{
	private final TimeCache<InputEvent> cache ;

	private final List<IInputHandler> handlers = MalletList.<IInputHandler>newList() ;

	private final List<InputEvent> inputs = MalletList.<InputEvent>newList() ;
	private final Vector2 mousePosition = new Vector2( 0, 0 ) ;

	public InputSystem()
	{
		final InputEvent[] inputs = new InputEvent[150] ;
		for( int i = 0; i < inputs.length; i++ )
		{
			inputs[i] = new InputEvent() ;
		}

		cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class, inputs ) ;
	}

	public void addInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			return ;
		}

		handlers.add( _handler ) ;
	}

	public void removeInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		handlers.remove( _handler ) ;
	}

	/** Pass InputEvents to the handlers **/

	public synchronized void update()
	{
		if( inputs.isEmpty() == true )
		{
			return ;
		}

		final int inputSize = inputs.size() ;
		for( int i = 0; i < inputSize; ++i )
		{
			passInputEventToHandlers( inputs.get( i ) ) ;
		}

		inputs.clear() ;
	}

	private void passInputEventToHandlers( final InputEvent _input )
	{
		final int handlerSize = handlers.size() ;
		for( int j = 0; j < handlerSize; ++j )
		{
			final IInputHandler handler = handlers.get( j ) ;
			switch( handler.passInputEvent( _input ) )
			{
				case PROPAGATE : continue ;
				case CONSUME   :
				default        : return ;
			}
		}
	}

	/** Recieve Key Events from system **/

	@Override
	public void keyPressed( final KeyEvent _event )
	{
		/*if( _event.isAutoRepeat() == true )
		{
			// If the event is an auto-repeat skip it,
			// we don't want to spam the input-system.
			return ;
		}*/

		// Sometimes when multiple keys have been pressed 
		// for a long duration, the next key to be pressed 
		// is flagged WRONGLY as an auto-repeat.
		// If the key is considered as released(false) then 
		// we'll always consider it as a valid pressed action. 

		KeyCode keycode = KeyCode.getKeyCode( _event.getKeyChar() ) ;
		if( keycode == KeyCode.NONE )
		{
			keycode = KeyCode.getKeyCode( ( int )_event.getKeyCode() ) ;
		}

		final InputEvent input = cache.get() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( InputType.KEYBOARD_PRESSED, keycode, _event.getWhen() ) ;
		inputs.add( input ) ;
	}

	@Override
	public void keyReleased( final KeyEvent _event )
	{
		if( _event.isAutoRepeat() == true )
		{
			// If the event is an auto-repeat skip it,
			// we don't want to spam the input-system.
			return ;
		}

		KeyCode keycode = KeyCode.getKeyCode( _event.getKeyChar() ) ;
		if( keycode == KeyCode.NONE )
		{
			keycode = KeyCode.getKeyCode( ( int )_event.getKeyCode() ) ;
		}

		final InputEvent input = cache.get() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( InputType.KEYBOARD_RELEASED, keycode, _event.getWhen() ) ;
		inputs.add( input ) ;
	}

	public void keyTyped( final KeyEvent _event ) {}

	/** Recieve mouse events from system **/
	
	public void mouseClicked( final MouseEvent _event ) {}

	public void mouseEntered( final MouseEvent _event ) {}

	public void mouseExited( final MouseEvent _event ) {}

	public void mousePressed( final MouseEvent _event )
	{
		switch( _event.getButton() )
		{
			// If the Mouse Event comes from a button greater 
			// than the three supported don't ignore it.
			// Consider it as the first mouse button.
			// This is better than the user clicking and 
			// getting no response.
			default                 :
			case MouseEvent.BUTTON1 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE1_PRESSED, mousePosition, _event.getWhen() ) ;
				break ;
			}
			case MouseEvent.BUTTON2 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE2_PRESSED, mousePosition, _event.getWhen() ) ;
				break ;
			}
			case MouseEvent.BUTTON3 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE3_PRESSED, mousePosition, _event.getWhen() ) ;
				break ;
			}
		}
	}

	public void mouseReleased( final MouseEvent _event )
	{
		switch( _event.getButton() )
		{
			// If the Mouse Event comes from a button greater 
			// than the three supported don't ignore it.
			// Consider it as the first mouse button.
			// This is better than the user clicking and 
			// getting no response.
			default                 :
			case MouseEvent.BUTTON1 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE1_RELEASED, mousePosition, _event.getWhen() ) ;
				break ;
			}
			case MouseEvent.BUTTON2 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE2_RELEASED, mousePosition, _event.getWhen() ) ;
				break ;
			}
			case MouseEvent.BUTTON3 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE3_RELEASED, mousePosition, _event.getWhen() ) ;
				break ;
			}
		}
	}

	public void mouseDragged( final MouseEvent _event )
	{
		mousePosition.x = _event.getX() ;
		mousePosition.y = _event.getY() ;

		updateMouse( InputType.MOUSE_MOVED, mousePosition, _event.getWhen() ) ;
	}

	public void mouseMoved( final MouseEvent _event )
	{
		mousePosition.x = _event.getX() ;
		mousePosition.y = _event.getY() ;

		updateMouse( InputType.MOUSE_MOVED, mousePosition, _event.getWhen() ) ;
	}

	/**  Recieve MouseWheelEvents from system **/
	
	public void mouseWheelMoved( final MouseEvent _event )
	{
		updateMouseWheel( _event ) ;
	}

	private synchronized void updateMouseWheel( final MouseEvent _event )
	{
		final InputEvent input = cache.get() ;
		final int scroll = ( int )_event.getRotation()[1] ;

		input.setInput( InputType.SCROLL_WHEEL, scroll, scroll, _event.getWhen() ) ;
		inputs.add( input ) ;
	}

	private synchronized void updateMouse( final InputType _inputType, final Vector2 _mousePosition, final long _when )
	{
		final InputEvent input = cache.get() ;
		input.setID( InputID.MOUSE_1 ) ;
		input.setInput( _inputType, ( int )_mousePosition.x, ( int )_mousePosition.y, _when ) ;
		inputs.add( input ) ;
	}

	public void clearHandlers()
	{
		handlers.clear() ;
	}

	public synchronized void clearInputs()
	{
		inputs.clear() ;
	}

	private final boolean exists( final IInputHandler _handler )
	{
		assert _handler != null ; 
		return handlers.contains( _handler ) ;
	}
}
