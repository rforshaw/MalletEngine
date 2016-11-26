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
	private final ArrayList<InputHandler> handlers = new ArrayList<InputHandler>() ;
	private final ArrayList<InputEvent> touchInputs = new ArrayList<InputEvent>() ;
	private final ArrayList<InputEvent> keyInputs = new ArrayList<InputEvent>() ;

	private final TimeCache<InputEvent> cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class ) ;
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

	public void onKeyDown( final int _keyCode, final KeyEvent _event )
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

	public void onKeyUp( final int _keyCode, final KeyEvent _event )
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

	public void onTouchEvent( final MotionEvent _event )
	{
		final int action = _event.getAction() ;
		/*final int historySize = _event.getHistorySize() ;
		for( int i = 0; i < historySize; i++)
		{
			final int pointer = 0 ;
			addInput( action,
					  _event.getHistoricalX( pointer, i ),
					  _event.getHistoricalY( pointer, i ) ) ;
		}*/

		addInput( action, _event.getX(), _event.getY() ) ;
	}

	private void addInput( final int _action, final float _x, final float _y )
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
			input.setInput( type, ( int )_x, ( int )_y ) ;
			touchInputs.add( input ) ;
		}
	}

	public void update()
	{
		synchronized( keyInputs )
		{
			final int sizeInput = keyInputs.size() ;
			for( int i = 0; i < sizeInput; ++i )
			{
				passInputEventToHandlers( keyInputs.get( i ) ) ;
			}

			keyInputs.clear() ;
		}

		synchronized( touchInputs )
		{
			final int sizeInput = touchInputs.size() ;
			//System.out.println( "Inputs: " + sizeInput ) ;
			for( int i = 0; i < sizeInput; ++i )
			{
				passInputEventToHandlers( touchInputs.get( i ) ) ;
			}

			touchInputs.clear() ;
		}
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

	private void updateKeys( final int _inputType, final KeyEvent _event )
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
