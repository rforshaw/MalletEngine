package com.linxonline.mallet.input.desktop ;

import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.awt.event.* ;
import java.io.* ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.util.pools.TimePool ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.locks.* ;

/**
	Input System is designed to use Java's built in input listeners, 
	requires to be added to a Window/Jframe to begin recieving input.
**/
public class InputSystem implements InputSystemInterface, 
									KeyListener, 
									MouseListener, 
									MouseMotionListener,
									MouseWheelListener
{
	public InputAdapterInterface inputAdapter = null ;
	private final TimePool<InputEvent> cache = new TimePool<InputEvent>( 0.25f, InputEvent.class ) ;

	private final ArrayList<InputHandler> handlers = new ArrayList<InputHandler>() ;
	private final HashMap<KeyCode, KeyState> keyboardState = new HashMap<KeyCode, KeyState>() ;
	private final ArrayList<KeyState> activeKeyStates = new ArrayList<KeyState>() ;

	private final ArrayList<InputEvent> mouseInputs = new ArrayList<InputEvent>() ;
	private final Vector2 mousePosition = new Vector2( 0, 0 ) ;

	public InputSystem() {}

	public void addInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			return ;
		}

		_handler.setInputAdapterInterface( inputAdapter ) ;
		handlers.add( _handler ) ;
	}

	public void removeInputHandler( final InputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		_handler.setInputAdapterInterface( null ) ;
		handlers.remove( _handler ) ;
	}

	/** Pass InputEvents to the handlers **/

	public synchronized void update()
	{
		passKeyInputs() ;
		passMouseInputs() ;
	}

	private void passKeyInputs()
	{
		final int handlerSize = handlers.size() ;
		final int stateSize = activeKeyStates.size() ;

		InputHandler handler = null ;
		KeyState state = null ;

		for( int i = 0; i < stateSize; i++ )
		{
			state = activeKeyStates.get( i ) ;
			final InputEvent input = cache.get() ;
			input.clone( state.input ) ;

			for( int j = 0; j < handlerSize; ++j )
			{
				handler = handlers.get( j ) ;
				handler.passInputEvent( input ) ;
			}
		}

		activeKeyStates.clear() ;
	}

	private void passMouseInputs()
	{
		final int inputSize = mouseInputs.size() ;
		final int handlerSize = handlers.size() ;

		InputEvent event = null ;
		InputHandler handler = null ;

		for( int i = 0; i < inputSize; ++i )
		{
			event = mouseInputs.get( i ) ;
			for( int j = 0; j < handlerSize; ++j )
			{
				handler = handlers.get( j ) ;
				handler.passInputEvent( event ) ;
			}
		}

		mouseInputs.clear() ;
	}

	/** Recieve Key Events from system **/

	public void keyPressed( KeyEvent _event )
	{
		if( _event.getID() == KeyEvent.KEY_PRESSED )
		{
			updateKey( InputType.KEYBOARD_PRESSED, _event ) ;
		}
	}

	public void keyReleased( KeyEvent _event )
	{
		if( _event.getID() == KeyEvent.KEY_RELEASED )
		{
			updateKey( InputType.KEYBOARD_RELEASED, _event ) ;
		}
	}

	public void keyTyped( KeyEvent _event ) {}

	/** Recieve mouse events from system **/
	
	public void mouseClicked( MouseEvent _event ) {}
	public void mouseEntered( MouseEvent _event ) {}
	public void mouseExited( MouseEvent _event ) {}

	public void mousePressed( MouseEvent _event )
	{
		switch( _event.getButton() )
		{
			case MouseEvent.BUTTON1 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE1_PRESSED, mousePosition ) ;
				break ;
			}
			case MouseEvent.BUTTON2 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE2_PRESSED, mousePosition ) ;
				break ;
			}
			case MouseEvent.BUTTON3 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE3_PRESSED, mousePosition ) ;
				break ;
			}
		}
	}

	public void mouseReleased( MouseEvent _event )
	{
		switch( _event.getButton() )
		{
			case MouseEvent.BUTTON1 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE1_RELEASED, mousePosition ) ;
				break ;
			}
			case MouseEvent.BUTTON2 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE2_RELEASED, mousePosition ) ;
				break ;
			}
			case MouseEvent.BUTTON3 :
			{
				mousePosition.x = _event.getX() ;
				mousePosition.y = _event.getY() ;
				updateMouse( InputType.MOUSE3_RELEASED, mousePosition ) ;
				break ;
			}
		}
	}

	public void mouseDragged( MouseEvent _event )
	{
		mousePosition.x = _event.getX() ;
		mousePosition.y = _event.getY() ;

		updateMouse( InputType.MOUSE_MOVED, mousePosition ) ;
	}

	public void mouseMoved( MouseEvent _event )
	{
		mousePosition.x = _event.getX() ;
		mousePosition.y = _event.getY() ;

		updateMouse( InputType.MOUSE_MOVED, mousePosition ) ;
	}

	/**  Recieve MouseWheelEvents from system **/
	
	public void mouseWheelMoved( MouseWheelEvent _event )
	{
		updateMouseWheel( _event ) ;
	}

	private synchronized void updateMouseWheel( MouseWheelEvent _event )
	{
		final int scroll = _event.getWheelRotation() ;
		final InputEvent input = new InputEvent( InputType.SCROLL_WHEEL, scroll, scroll ) ;
		mouseInputs.add( input ) ;
	}
	
	private synchronized void updateMouse( final InputType _inputType, final Vector2 _mousePosition )
	{
		final InputEvent input = cache.get() ;
		input.setInput( _inputType, ( int )_mousePosition.x, ( int )_mousePosition.y ) ;
		mouseInputs.add( input ) ;

		Locks.getLocks().getLock( "APPLICATION_LOCK" ).unlock() ;
	}

	private synchronized void updateKey( final InputType _inputType, final KeyEvent _event )
	{
		KeyCode keycode = KeyCode.getKeyCode( _event.getKeyChar() ) ;
		if( keycode == KeyCode.NONE )
		{
			keycode = isSpecialKeyDown( _event ) ;
		}

		//System.out.println( keycode ) ;

		if( keyboardState.containsKey( keycode ) == true )
		{
			changeKey( _inputType, keycode, _event ) ;
			return ;
		}

		// Create new Key if it doesn't exist.
		final InputEvent input = new InputEvent( _inputType, keycode ) ;
		input.isActionKey = _event.isActionKey() ;

		final KeyState state = new KeyState( input, true ) ;
		keyboardState.put( keycode, state ) ;
		activeKeyStates.add( state ) ;
	}

	private void changeKey( final InputType _inputType, final KeyCode _keycode, final KeyEvent _event )
	{
		final KeyState state = keyboardState.get( _keycode ) ;
		if( state.input.inputType != _inputType )			// If the Input Type has changed
		{
			final long eventTimeStamp = _event.getWhen() ;
			long dt = 0L ;

			// Timestamp check is done, due to Linux Input Bug
			// Causes endless Inputs to be sent, this filters duplicates.
			if( _inputType == InputType.KEYBOARD_PRESSED )
			{
				dt = eventTimeStamp - state.pressedTimeStamp ;
			}
			else if( _inputType == InputType.KEYBOARD_RELEASED )
			{
				dt = eventTimeStamp - state.releasedTimeStamp ;
			}

			if( dt > 0L )
			{
				activeKeyStates.add( state ) ;
				state.changed = true ;

				state.input.inputType = _inputType ;

				if( _inputType == InputType.KEYBOARD_PRESSED )
				{
					state.pressedTimeStamp = eventTimeStamp ;
				}
				else if( _inputType == InputType.KEYBOARD_RELEASED )
				{
					state.releasedTimeStamp = eventTimeStamp ;
				}
			}
		}
	}

	public void clearHandlers()
	{
		handlers.clear() ;
	}

	public synchronized void clearInputs()
	{
		mouseInputs.clear() ;
		keyboardState.clear() ;
		activeKeyStates.clear() ;
	}

	private final boolean exists( final InputHandler _handler )
	{
		return handlers.contains( _handler ) ;
	}

	private final KeyCode isSpecialKeyDown( final KeyEvent _event )
	{
		switch( _event.getKeyCode() )
		{
			case KeyEvent.VK_WINDOWS   : return KeyCode.WINDOWS ;
			case KeyEvent.VK_INSERT   : return KeyCode.INSERT ;
			case KeyEvent.VK_SCROLL_LOCK: return KeyCode.SCROLL_LOCK ;
			case KeyEvent.VK_PRINTSCREEN:return KeyCode.PRINT_SCREEN ;
			case KeyEvent.VK_DELETE    : return KeyCode.DELETE ;
			case KeyEvent.VK_HOME      : return KeyCode.HOME ;
			case KeyEvent.VK_END       : return KeyCode.END ;
			case KeyEvent.VK_PAGE_DOWN : return KeyCode.PAGE_UP ;
			case KeyEvent.VK_PAGE_UP   : return KeyCode.PAGE_DOWN ;
			case KeyEvent.VK_TAB       : return KeyCode.TAB ;
			case KeyEvent.VK_CAPS_LOCK : return KeyCode.CAPS_LOCK ;
			case KeyEvent.VK_UP        : return KeyCode.UP ;
			case KeyEvent.VK_DOWN      : return KeyCode.DOWN ;
			case KeyEvent.VK_LEFT      : return KeyCode.LEFT ;
			case KeyEvent.VK_RIGHT     : return KeyCode.RIGHT ;
			case KeyEvent.VK_ESCAPE    : return KeyCode.ESCAPE ;
			case KeyEvent.VK_CONTROL   : return KeyCode.CTRL ;
			case KeyEvent.VK_ALT       : return KeyCode.ALT ;
			case KeyEvent.VK_SHIFT     : return KeyCode.SHIFT ;
			case KeyEvent.VK_META      : return KeyCode.META ;
			case KeyEvent.VK_ALT_GRAPH : return KeyCode.ALTGROUP ;
			case KeyEvent.VK_BACK_SPACE: return KeyCode.BACKSPACE ;
			case KeyEvent.VK_F1        : return KeyCode.F1 ;
			case KeyEvent.VK_F2        : return KeyCode.F2 ;
			case KeyEvent.VK_F3        : return KeyCode.F3 ;
			case KeyEvent.VK_F4        : return KeyCode.F4 ;
			case KeyEvent.VK_F5        : return KeyCode.F5 ;
			case KeyEvent.VK_F6        : return KeyCode.F6 ;
			case KeyEvent.VK_F7        : return KeyCode.F7 ;
			case KeyEvent.VK_F8        : return KeyCode.F8 ;
			case KeyEvent.VK_F9        : return KeyCode.F9 ;
			case KeyEvent.VK_F10       : return KeyCode.F10 ;
			case KeyEvent.VK_F11       : return KeyCode.F11 ;
			case KeyEvent.VK_F12       : return KeyCode.F12 ;
			case KeyEvent.VK_NUM_LOCK  : return KeyCode.NUM_LOCK ;
			case KeyEvent.VK_NUMPAD0   : return KeyCode.NUMPAD0 ;
			case KeyEvent.VK_NUMPAD1   : return KeyCode.NUMPAD1 ;
			case KeyEvent.VK_NUMPAD2   : return KeyCode.NUMPAD2 ;
			case KeyEvent.VK_NUMPAD3   : return KeyCode.NUMPAD3 ;
			case KeyEvent.VK_NUMPAD4   : return KeyCode.NUMPAD4 ;
			case KeyEvent.VK_NUMPAD5   : return KeyCode.NUMPAD5 ;
			case KeyEvent.VK_NUMPAD6   : return KeyCode.NUMPAD6 ;
			case KeyEvent.VK_NUMPAD7   : return KeyCode.NUMPAD7 ;
			case KeyEvent.VK_NUMPAD8   : return KeyCode.NUMPAD8 ;
			case KeyEvent.VK_NUMPAD9   : return KeyCode.NUMPAD9 ;
			default 					  : return KeyCode.NONE ;
		}
	}
}