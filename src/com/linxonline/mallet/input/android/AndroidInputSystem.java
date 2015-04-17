package com.linxonline.mallet.input.android ;

import java.util.* ;

import android.view.KeyEvent ;
import android.view.MotionEvent ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.caches.TimeCache ;

public class AndroidInputSystem implements InputSystemInterface, 
										   AndroidInputListener
{
	public InputAdapterInterface inputAdapter = null ;
	private ArrayList<InputHandler> handlers = new ArrayList<InputHandler>() ;
	private ArrayList<InputEvent> touchInputs = new ArrayList<InputEvent>() ;
	private ArrayList<InputEvent> keyInputs = new ArrayList<InputEvent>() ;

	private final TimeCache<InputEvent> cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class ) ;
	private final Vector2 touchPosition = new Vector2( 0, 0 ) ;

	public AndroidInputSystem() {}

	public void addInputHandler( InputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			return ;
		}

		_handler.setInputAdapterInterface( inputAdapter ) ;
		handlers.add( _handler ) ;
	}

	public void removeInputHandler( InputHandler _handler )
	{
		handlers.remove( _handler ) ;
	}

	public void onKeyDown( int _keyCode, KeyEvent _event )
	{
		if( _event.getAction() == KeyEvent.ACTION_UP )
		{
			//updateKeys( InputType.KEYBOARD_PRESSED, _event ) ;
		}
		else
		{
			//updateKeys( InputType.KEYBOARD_RELEASED, _event ) ;
		}
	}

	public void onKeyUp( int _keyCode, KeyEvent _event )
	{
		if( _event.getAction() == KeyEvent.ACTION_UP )
		{
			//updateKeys( InputType.KEYBOARD_PRESSED, _event ) ;
		}
		else
		{
			//updateKeys( InputType.KEYBOARD_RELEASED, _event ) ;
		}
	}

	public void onTouchEvent( MotionEvent _event )
	{
		synchronized( touchInputs )
		{
			InputType type = InputType.TOUCH_MOVE ;
			final int action = _event.getAction() ;

			if( action == MotionEvent.ACTION_UP )
			{
				type = InputType.TOUCH_UP ;
			}
			else if( action == MotionEvent.ACTION_DOWN )
			{
				type = InputType.TOUCH_DOWN ;
			}

			touchPosition.x = _event.getX() ;
			touchPosition.y = _event.getY() ;

			final InputEvent input = cache.get() ;
			input.setInput( type, ( int )touchPosition.x, ( int )touchPosition.y ) ;
			touchInputs.add( input ) ;
		}
	}
	
	public void update()
	{
		final int sizeHandlers = handlers.size() ;
		InputHandler handler = null ;
		InputEvent event = null ;

		synchronized( keyInputs )
		{
			final int sizeInput = keyInputs.size() ;
			for( int i = 0; i < sizeInput; ++i )
			{
				event = keyInputs.get( i ) ;
				for( int j = 0; j < sizeHandlers; ++j )
				{
					handler = handlers.get( j ) ;
					handler.passInputEvent( event ) ;
				}
			}
		}

		keyInputs.clear() ;

		synchronized( touchInputs )
		{
			final int sizeInput = touchInputs.size() ;
			//System.out.println( "Inputs: " + sizeInput ) ;
			for( int i = 0; i < sizeInput; ++i )
			{
				event = touchInputs.get( i ) ;
				for( int j = 0; j < sizeHandlers; ++j )
				{
					//System.out.println( event ) ;
					handler = handlers.get( j ) ;
					handler.passInputEvent( event ) ;
				}
			}
		}

		touchInputs.clear() ;
	}
	
	private void updateKeys( int _inputType, KeyEvent _event )
	{
		System.out.println( "UPDATE KEYS: Needs implementing." ) ;
		/*KeyCode keycode = _event.getKeyCode() ;
		if( keycode == KeyEvent.KEYCODE_SHIFT_LEFT || 
			keycode == KeyEvent.KEYCODE_SHIFT_RIGHT )
		{
			keycode = KeyCode.SHIFT ;
		}
		else if( keycode == KeyEvent.KEYCODE_ALT_LEFT || 
				 keycode == KeyEvent.KEYCODE_ALT_RIGHT )
		{
			keycode = KeyCode.ALT ;
		}
		else if( keycode == KeyEvent.KEYCODE_DEL )
		{
			keycode = KeyCode.BACKSPACE ;
		}
		else
		{
			keycode = _event.getUnicodeChar() ;
		}

		synchronized( keyInputs )
		{
			InputEvent input = new InputEvent( _inputType, 
											 ( char )_event.getUnicodeChar(), 
											  keycode ) ;
			keyInputs.add( input ) ;
		}*/
	}
	
	public void clearHandlers()
	{
		handlers.clear() ;
	}

	public void clearInputs() {}

	private boolean exists( InputHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}
}