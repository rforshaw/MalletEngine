package com.linxonline.mallet.input ;

import java.util.HashMap ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.awt.event.* ;
import java.io.* ;

import com.linxonline.mallet.maths.Vector2 ;

/*==============================================================*/
// InputSystem is the root class to obtain InputEvents using 	  //
// Javas Input Listeners.									  //
// Needs to be added to a JFrame or equivelant to receive Input.//
/*==============================================================*/
// TODO: Add InputEvent pool, to reduce the constant creation of InputEvents. 

public class InputSystem implements InputSystemInterface, 
									KeyListener, 
									MouseListener, 
									MouseMotionListener,
									MouseWheelListener
{
	public InputAdapterInterface inputAdapter = null ;
	private InputCache cache = new InputCache( 0.25f ) ;
	private ArrayList<InputHandler> handlers = new ArrayList<InputHandler>() ;
	private HashMap<Integer, KeyState> keyboardState = new HashMap<Integer, KeyState>() ;
	private ArrayList<InputEvent> mouseInputs = new ArrayList<InputEvent>() ;
	private Vector2 mousePosition = new Vector2( 0, 0 ) ;
	private Vector2 screenMousePosition = new Vector2( 0, 0 ) ;

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

	public synchronized void update()
	{
		passKeyInputs() ;
		passMouseInputs() ;
	}

	private void passKeyInputs()
	{
		final Collection<KeyState> keybaord = keyboardState.values() ;
		final int handlerSize = handlers.size() ;
		InputHandler handler = null ;

		for( final KeyState keyState : keybaord )
		{
			if( keyState.changed == true )
			{
				// Clone Input Event incase KeyState is changed
				// before game logic has processed it.
				final InputEvent input = cache.getInput() ;
				input.clone( keyState.input ) ;
				for( int j = 0; j < handlerSize; ++j )
				{
					handler = handlers.get( j ) ;
					handler.passInputEvent( input ) ;
				}
			}

			keyState.changed = false ;
		}
	}

	private void passMouseInputs()
	{
		final int inputSize = mouseInputs.size() ;
		final int handlerSize = handlers.size() ;

		// If there are no Inputs, add the last mouse position to queue.
		// Ensure position is updated with correct viewport dimensions.
		/*if( mouseInputs.size() == 0 )
		{
			updateMouse( InputEvent.MOUSE_MOVED, mousePosition ) ;
			return ;
		}*/

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

	public void keyPressed( KeyEvent _event )
	{
		if( _event.getID() == KeyEvent.KEY_PRESSED )
		{
			updateKey( InputEvent.KEYBOARD_PRESSED, _event ) ;
		}
	}

	public void keyReleased( KeyEvent _event )
	{
		if( _event.getID() == KeyEvent.KEY_RELEASED )
		{
			updateKey( InputEvent.KEYBOARD_RELEASED, _event ) ;
		}
	}

	public void keyTyped( KeyEvent _event ) {}

	public void mouseClicked( MouseEvent _event ) {}

	public void mouseEntered( MouseEvent _event ) {}

	public void mouseExited( MouseEvent _event ) {}

	public void mousePressed( MouseEvent _event )
	{
		final int button = _event.getButton() ;

		if( button == MouseEvent.BUTTON1 )
		{
			mousePosition.x = _event.getX() ;
			mousePosition.y = _event.getY() ;

			updateMouse( InputEvent.MOUSE1_PRESSED, mousePosition ) ;
		}
		else if( button == MouseEvent.BUTTON2 )
		{
			mousePosition.x = _event.getX() ;
			mousePosition.y = _event.getY() ;

			updateMouse( InputEvent.MOUSE2_PRESSED, mousePosition ) ;
		}
	}

	public void mouseReleased( MouseEvent _event )
	{
		final int button = _event.getButton() ;

		if( button == MouseEvent.BUTTON1 )
		{
			mousePosition.x = _event.getX() ;
			mousePosition.y = _event.getY() ;

			updateMouse( InputEvent.MOUSE1_RELEASED, mousePosition ) ;
		}
		else if( button == MouseEvent.BUTTON2 )
		{
			mousePosition.x = _event.getX() ;
			mousePosition.y = _event.getY() ;

			updateMouse( InputEvent.MOUSE2_RELEASED, mousePosition ) ;
		}
	}

	public void mouseDragged( MouseEvent _event )
	{
		mousePosition.x = _event.getX() ;
		mousePosition.y = _event.getY() ;

		updateMouse( InputEvent.MOUSE_MOVED, mousePosition ) ;
	}

	public void mouseMoved( MouseEvent _event )
	{
		mousePosition.x = _event.getX() ;
		mousePosition.y = _event.getY() ;

		updateMouse( InputEvent.MOUSE_MOVED, mousePosition ) ;
	}

	public void mouseWheelMoved( MouseWheelEvent _event )
	{
		updateMouseWheel( _event ) ;
	}
	
	private synchronized void updateMouseWheel( MouseWheelEvent _event )
	{
		final int scroll = _event.getWheelRotation() ;
		InputEvent input = new InputEvent( InputEvent.SCROLL_WHEEL, 
											scroll, 
											scroll ) ;
		mouseInputs.add( input ) ;
	}
	
	private synchronized void updateMouse( final int _inputType, final Vector2 _mousePosition )
	{
		// Used to convert raw mouse position to game mouse position.
		// Moved to InputComponent or InputHandler, etc to do
		screenMousePosition.x =  _mousePosition.x ;
		screenMousePosition.y =  _mousePosition.y ;

		final InputEvent input = cache.getInput() ;
		input.setInput( _inputType, screenMousePosition.x, screenMousePosition.y ) ;
		mouseInputs.add( input ) ;
	}

	private synchronized void updateKey( final int _inputType, final KeyEvent _event )
	{
		final int keycode = _event.getKeyCode() ;
		if( keyboardState.containsKey( keycode ) == true )
		{
			changeKey( _inputType, _event ) ;
			return ;
		}

		// Create new Key if it doesn't exist.
		InputEvent input = new InputEvent( _inputType, _event.getKeyChar(), _event.getKeyCode() ) ;
		input.isActionKey = _event.isActionKey() ;
		keyboardState.put( keycode, new KeyState( input, true ) ) ;
	}

	private void changeKey( final int _inputType, final KeyEvent _event )
	{
		final int keycode = _event.getKeyCode() ;
		final KeyState state = keyboardState.get( keycode ) ;
		if( state.input.inputType != _inputType )			// If the Input Type has changed
		{
			final long eventTimeStamp = _event.getWhen() ;
			long dt = 0L ;

			// Timestamp check is done, due to Linux Input Bug
			// Causes endless Inputs to be sent, this filters duplicates.
			if( _inputType == InputEvent.KEYBOARD_PRESSED )
			{
				dt = eventTimeStamp - state.pressedTimeStamp ;
			}
			else if( _inputType == InputEvent.KEYBOARD_RELEASED )
			{
				dt = eventTimeStamp - state.releasedTimeStamp ;
			}

			if( dt > 0L )
			{
				final InputEvent input = new InputEvent( _inputType, _event.getKeyChar(), keycode ) ;
				input.isActionKey = _event.isActionKey() ;

				state.changed = true ;
				state.input = input ;

				if( _inputType == InputEvent.KEYBOARD_PRESSED )
				{
					state.pressedTimeStamp = eventTimeStamp ;
				}
				else if( _inputType == InputEvent.KEYBOARD_RELEASED )
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
	}

	private final boolean exists( final InputHandler _handler )
	{
		return handlers.contains( _handler ) ;
	}

	private final boolean isSpecialKeysDown( final KeyEvent _event )
	{
		if( _event.isShiftDown() == true )
		{
			return true ;
		}

		if( _event.isControlDown() == true )
		{
			return true ;
		}

		if( _event.isAltDown() == true )
		{
			return true ;
		}
		
		if( _event.isAltGraphDown() == true )
		{
			return true ;
		}
		
		if( _event.isMetaDown() == true )
		{
			return true ;
		}
		
		return false ;
	}
}