package com.linxonline.mallet.input.android ;

import java.util.* ;

import android.view.KeyEvent ;
import android.view.MotionEvent ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.caches.TimePool ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

public final class AndroidInputSystem implements IInputSystem, AndroidInputListener
{
	private final TimePool<InputEvent> cache = new TimePool<InputEvent>( 150, 0.25f, () -> new InputEvent() ) ;

	private final List<IInputHandler> handlers = MalletList.<IInputHandler>newList() ;

	private final List<InputEvent> inputs = MalletList.<InputEvent>newList() ;
	private final Vector2 touchPosition = new Vector2( 0, 0 ) ;

	public AndroidInputSystem() {}

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
		handlers.remove( _handler ) ;
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
			final InputEvent input = cache.take() ;
			input.setID( _id ) ;
			input.setInput( type, ( int )_x, ( int )_y, _when ) ;
			inputs.add( input ) ;
		}
	}

	public void update()
	{
		synchronized( inputs )
		{
			final int sizeInput = inputs.size() ;
			//System.out.println( "Inputs: " + sizeInput ) ;
			for( int i = 0; i < sizeInput; ++i )
			{
				passInputEventToHandlers( inputs.get( i ) ) ;
			}

			inputs.clear() ;
		}
	}

	private void passInputEventToHandlers( final InputEvent _input )
	{
		final int handlerSize = handlers.size() ;
		IInputHandler handler = null ;

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

		final InputEvent input = cache.take() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( _inputType, keycode, _event.getEventTime() ) ;
		inputs.add( input ) ;
	}

	public void clearHandlers()
	{
		handlers.clear() ;
	}

	public void clearInputs()
	{
		inputs.clear() ;
	}

	private boolean exists( final IInputHandler _handler )
	{
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
