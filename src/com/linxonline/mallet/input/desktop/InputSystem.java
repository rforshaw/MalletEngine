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

	private final List<InputHandler> handlers = MalletList.<InputHandler>newList() ;
	private boolean[] keys = new boolean[32767] ;

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

		for( int i = 0; i < keys.length; ++i )
		{
			keys[i] = false ;
		}
	}

	public void addInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			return ;
		}

		handlers.add( _handler ) ;
	}

	public void removeInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		handlers.remove( _handler ) ;
		_handler.reset() ;
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
			final InputHandler handler = handlers.get( j ) ;
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
		// Sometimes when multiple keys have been pressed 
		// for a long duration, the next key to be pressed 
		// is flagged WRONGLY as an auto-repeat.
		// If the key is considered as released(false) then 
		// we'll always consider it as a valid pressed action. 

		final int index = ( int )_event.getKeyCode() ;
		if( keys[index] == false )
		{
			keys[index] = true ;

			KeyCode keycode = KeyCode.getKeyCode( _event.getKeyChar() ) ;
			if( keycode == KeyCode.NONE )
			{
				keycode = isSpecialKeyDown( _event ) ;
			}

			final InputEvent input = cache.get() ;
			input.setID( InputID.KEYBOARD_1 ) ;
			input.setInput( InputType.KEYBOARD_PRESSED, keycode, _event.getWhen() ) ;
			inputs.add( input ) ;
		}
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

		final int index = ( int )_event.getKeyCode() ;
		if( keys[index] == true )
		{
			keys[index] = false ;

			KeyCode keycode = KeyCode.getKeyCode( _event.getKeyChar() ) ;
			if( keycode == KeyCode.NONE )
			{
				keycode = isSpecialKeyDown( _event ) ;
			}

			final InputEvent input = cache.get() ;
			input.setID( InputID.KEYBOARD_1 ) ;
			input.setInput( InputType.KEYBOARD_RELEASED, keycode, _event.getWhen() ) ;
			inputs.add( input ) ;
		}
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
		if( handlers.isEmpty() == false )
		{
			final int size = handlers.size() ;
			for( int i = 0; i < size; ++i )
			{
				handlers.get( i ).reset() ;
			}
			handlers.clear() ;
		}
	}

	public synchronized void clearInputs()
	{
		inputs.clear() ;
	}

	private final boolean exists( final InputHandler _handler )
	{
		assert _handler != null ; 
		return handlers.contains( _handler ) ;
	}

	private final KeyCode isSpecialKeyDown( final KeyEvent _event )
	{
		switch( _event.getKeyCode() )
		{
			case KeyEvent.VK_WINDOWS     : return KeyCode.WINDOWS ;
			case KeyEvent.VK_INSERT      : return KeyCode.INSERT ;
			case KeyEvent.VK_SCROLL_LOCK : return KeyCode.SCROLL_LOCK ;
			case KeyEvent.VK_PRINTSCREEN : return KeyCode.PRINT_SCREEN ;
			case KeyEvent.VK_DELETE      : return KeyCode.DELETE ;
			case KeyEvent.VK_HOME        : return KeyCode.HOME ;
			case KeyEvent.VK_END         : return KeyCode.END ;
			case KeyEvent.VK_PAGE_DOWN   : return KeyCode.PAGE_UP ;
			case KeyEvent.VK_PAGE_UP     : return KeyCode.PAGE_DOWN ;
			case KeyEvent.VK_TAB         : return KeyCode.TAB ;
			case KeyEvent.VK_CAPS_LOCK   : return KeyCode.CAPS_LOCK ;
			case KeyEvent.VK_UP          : return KeyCode.UP ;
			case KeyEvent.VK_DOWN        : return KeyCode.DOWN ;
			case KeyEvent.VK_LEFT        : return KeyCode.LEFT ;
			case KeyEvent.VK_RIGHT       : return KeyCode.RIGHT ;
			case KeyEvent.VK_ESCAPE      : return KeyCode.ESCAPE ;
			case KeyEvent.VK_CONTROL     : return KeyCode.CTRL ;
			case KeyEvent.VK_ALT         : return KeyCode.ALT ;
			case KeyEvent.VK_SHIFT       : return KeyCode.SHIFT ;
			case KeyEvent.VK_META        : return KeyCode.META ;
			case KeyEvent.VK_ALT_GRAPH   : return KeyCode.ALTGROUP ;
			case KeyEvent.VK_BACK_SPACE  : return KeyCode.BACKSPACE ;
			case KeyEvent.VK_F1          : return KeyCode.F1 ;
			case KeyEvent.VK_F2          : return KeyCode.F2 ;
			case KeyEvent.VK_F3          : return KeyCode.F3 ;
			case KeyEvent.VK_F4          : return KeyCode.F4 ;
			case KeyEvent.VK_F5          : return KeyCode.F5 ;
			case KeyEvent.VK_F6          : return KeyCode.F6 ;
			case KeyEvent.VK_F7          : return KeyCode.F7 ;
			case KeyEvent.VK_F8          : return KeyCode.F8 ;
			case KeyEvent.VK_F9          : return KeyCode.F9 ;
			case KeyEvent.VK_F10         : return KeyCode.F10 ;
			case KeyEvent.VK_F11         : return KeyCode.F11 ;
			case KeyEvent.VK_F12         : return KeyCode.F12 ;
			case KeyEvent.VK_NUM_LOCK    : return KeyCode.NUM_LOCK ;
			case KeyEvent.VK_NUMPAD0     : return KeyCode.NUMPAD0 ;
			case KeyEvent.VK_NUMPAD1     : return KeyCode.NUMPAD1 ;
			case KeyEvent.VK_NUMPAD2     : return KeyCode.NUMPAD2 ;
			case KeyEvent.VK_NUMPAD3     : return KeyCode.NUMPAD3 ;
			case KeyEvent.VK_NUMPAD4     : return KeyCode.NUMPAD4 ;
			case KeyEvent.VK_NUMPAD5     : return KeyCode.NUMPAD5 ;
			case KeyEvent.VK_NUMPAD6     : return KeyCode.NUMPAD6 ;
			case KeyEvent.VK_NUMPAD7     : return KeyCode.NUMPAD7 ;
			case KeyEvent.VK_NUMPAD8     : return KeyCode.NUMPAD8 ;
			case KeyEvent.VK_NUMPAD9     : return KeyCode.NUMPAD9 ;
			default                      : return KeyCode.NONE ;
		}
	}
}
