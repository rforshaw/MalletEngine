package com.linxonline.mallet.input.android ;

import java.util.* ;

import android.view.KeyEvent ;
import android.view.MotionEvent ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.caches.TimeCache ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

public class AndroidInputSystem implements InputSystemInterface, 
										   AndroidInputListener
{
	public InputAdapterInterface inputAdapter = null ;
	private final TimeCache<InputEvent> cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class ) ;

	private final List<InputHandler> handlers = MalletList.<InputHandler>newList() ;
	private final Map<KeyCode, KeyState> keyboardState = MalletMap.<KeyCode, KeyState>newMap() ;
	private final List<KeyState> activeKeyStates = MalletList.<KeyState>newList() ;

	private final List<InputEvent> touchInputs = MalletList.<InputEvent>newList() ;
	private final Vector2 touchPosition = new Vector2( 0, 0 ) ;

	public AndroidInputSystem() {}

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
		handlers.remove( _handler ) ;
		_handler.reset() ;
	}

	@Override
	public void onKeyDown( final int _keyCode, final KeyEvent _event )
	{
		updateKeys( InputType.KEYBOARD_PRESSED, _event ) ;
	}

	@Override
	public void onKeyUp( final int _keyCode, final KeyEvent _event )
	{
		updateKeys( InputType.KEYBOARD_RELEASED, _event ) ;
	}

	public void onTouchEvent( final MotionEvent _event )
	{
		final int size = _event.getPointerCount() ;
		for( int i = 0; i < size; i++ )
		{
			InputID id = InputID.NONE ;
			switch( i )
			{
				case 0 : id = InputID.TOUCH_1 ; break ;
				case 1 : id = InputID.TOUCH_2 ; break ;
				case 2 : id = InputID.TOUCH_3 ; break ;
				case 3 : id = InputID.TOUCH_4 ; break ;
			}

			if( id != InputID.NONE )
			{
				final int action = _event.getAction() ;
				addInput( id, action, _event.getX( i ), _event.getY( i ) ) ;
			}
		}
	}

	private void addInput( final InputID _id, final int _action, final float _x, final float _y )
	{
		InputType type = InputType.TOUCH_MOVE ;
		if( _action == MotionEvent.ACTION_UP )
		{
			type = InputType.TOUCH_UP ;
		}
		else if( _action == MotionEvent.ACTION_DOWN )
		{
			type = InputType.TOUCH_DOWN ;
		}

		synchronized( touchInputs )
		{
			final InputEvent input = cache.get() ;
			input.setID( _id ) ;
			input.setInput( type, ( int )_x, ( int )_y ) ;
			touchInputs.add( input ) ;
		}
	}

	public void update()
	{
		passKeyInputs() ;
		passMouseInputs() ;
	}

	private void passKeyInputs()
	{
		final int stateSize = activeKeyStates.size() ;
		for( int i = 0; i < stateSize; i++ )
		{
			final KeyState state = activeKeyStates.get( i ) ;
			final InputEvent input = cache.get() ;
			input.clone( state.input ) ;

			passInputEventToHandlers( input ) ;
		}

		activeKeyStates.clear() ;
	}

	private void passMouseInputs()
	{
		final int sizeInput = touchInputs.size() ;
		//System.out.println( "Inputs: " + sizeInput ) ;
		for( int i = 0; i < sizeInput; ++i )
		{
			passInputEventToHandlers( touchInputs.get( i ) ) ;
		}

		touchInputs.clear() ;
	}

	private void passInputEventToHandlers( final InputEvent _input )
	{
		final int handlerSize = handlers.size() ;
		InputHandler handler = null ;

		for( int j = 0; j < handlerSize; ++j )
		{
			handler = handlers.get( j ) ;
			switch( handler.passInputEvent( _input ) )
			{
				case PROPAGATE : continue ;
				case CONSUME   : return ;
			}
		}
	}

	private void updateKeys( final InputType _inputType, final KeyEvent _event )
	{
		KeyCode keycode = KeyCode.getKeyCode( ( char )_event.getUnicodeChar() ) ;
		/*if( keycode == KeyCode.NONE )
		{
			keycode = isSpecialKeyDown( _event ) ;
		}*/

		{
			final KeyState state = keyboardState.get( keycode ) ;
			if( state != null )
			{
				changeKey( _inputType, state, _event ) ;
				return ;
			}
		}

		// Create new Key if it doesn't exist.
		final InputEvent input = new InputEvent( _inputType, keycode, InputID.KEYBOARD_1 ) ;
		//input.isActionKey = _event.isActionKey() ;

		final KeyState state = new KeyState( input, true ) ;
		keyboardState.put( keycode, state ) ;
		activeKeyStates.add( state ) ;
	}

	private void changeKey( final InputType _inputType, final KeyState _state, final KeyEvent _event )
	{
		if( _state.input.inputType != _inputType )			// If the Input Type has changed
		{
			final long eventTimeStamp = _event.getEventTime() ;
			long dt = 0L ;

			// Timestamp check is done, due to Linux Input Bug
			// Causes endless Inputs to be sent, this filters duplicates.
			if( _inputType == InputType.KEYBOARD_PRESSED )
			{
				dt = eventTimeStamp - _state.pressedTimeStamp ;
			}
			else if( _inputType == InputType.KEYBOARD_RELEASED )
			{
				dt = eventTimeStamp - _state.releasedTimeStamp ;
			}

			if( dt > 0L )
			{
				activeKeyStates.add( _state ) ;
				_state.changed = true ;

				_state.input.inputType = _inputType ;

				if( _inputType == InputType.KEYBOARD_PRESSED )
				{
					_state.pressedTimeStamp = eventTimeStamp ;
				}
				else if( _inputType == InputType.KEYBOARD_RELEASED )
				{
					_state.releasedTimeStamp = eventTimeStamp ;
				}
			}
		}
	}

	public void clearHandlers()
	{
		final int size = handlers.size() ;
		for( int i = 0; i < size; ++i )
		{
			handlers.get( i ).reset() ;
		}
		handlers.clear() ;
	}

	public void clearInputs() {}

	private boolean exists( final InputHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}
}
