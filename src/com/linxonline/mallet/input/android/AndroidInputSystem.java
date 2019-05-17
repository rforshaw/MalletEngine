package com.linxonline.mallet.input.android ;

import java.util.* ;

import android.view.KeyEvent ;
import android.view.MotionEvent ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.caches.TimeCache ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

public class AndroidInputSystem implements IInputSystem, 
										   AndroidInputListener
{
	public InputAdapterInterface inputAdapter = null ;
	private final TimeCache<InputEvent> cache ;

	private final List<InputHandler> handlers = MalletList.<InputHandler>newList() ;

	private final List<InputEvent> inputs = MalletList.<InputEvent>newList() ;
	private final Vector2 touchPosition = new Vector2( 0, 0 ) ;

	public AndroidInputSystem()
	{
		final InputEvent[] inputs = new InputEvent[150] ;
		for( int i = 0; i < inputs.length; i++ )
		{
			inputs[i] = new InputEvent() ;
		}
		cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class, inputs ) ;
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
				addInput( id, action, _event.getX( i ), _event.getY( i ), _event.getEventTime() ) ;
			}
		}
	}

	private void addInput( final InputID _id, final int _action, final float _x, final float _y, final long _when )
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

		synchronized( inputs )
		{
			final InputEvent input = cache.get() ;
			input.setID( _id ) ;
			input.setInput( type, ( int )_x, ( int )_y, _when ) ;
			inputs.add( input ) ;
		}
	}

	public void update()
	{
		final int sizeInput = inputs.size() ;
		//System.out.println( "Inputs: " + sizeInput ) ;
		for( int i = 0; i < sizeInput; ++i )
		{
			passInputEventToHandlers( inputs.get( i ) ) ;
		}

		inputs.clear() ;
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
		if( keycode == KeyCode.NONE )
		{
			keycode = isSpecialKeyDown( _event ) ;
		}

		final InputEvent input = cache.get() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( _inputType, keycode, _event.getEventTime() ) ;
		inputs.add( input ) ;
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

	public void clearInputs()
	{
		inputs.clear() ;
	}

	private boolean exists( final InputHandler _handler )
	{
		assert _handler != null ;
		return handlers.contains( _handler ) ;
	}

	private final KeyCode isSpecialKeyDown( final KeyEvent _event )
	{
		switch( _event.getKeyCode() )
		{
			case KeyEvent.KEYCODE_DEL : return KeyCode.BACKSPACE ;
			default                   : return KeyCode.NONE ;
		}
	}
}
