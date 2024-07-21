package com.linxonline.mallet.input.desktop ;

import java.util.* ;

import com.jogamp.newt.event.* ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.util.caches.TimePool ;
import com.linxonline.mallet.util.MalletList ;

public final class InputSystem implements IInputSystem, KeyListener, MouseListener
{
	private final TimePool<InputEvent> cache = new TimePool<InputEvent>( 150, 0.25f, () -> new InputEvent() ) ;

	private final List<IInputHandler> handlers = MalletList.<IInputHandler>newList() ;
	private final List<InputEvent> inputs = MalletList.<InputEvent>newList() ;

	public InputSystem() {}

	@Override
	public void addInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == true )
		{
			return ;
		}

		handlers.add( _handler ) ;
	}

	@Override
	public void removeInputHandler( final IInputHandler _handler )
	{
		if( exists( _handler ) == false )
		{
			return ;
		}

		handlers.remove( _handler ) ;
	}

	/** Pass InputEvents to the handlers **/
	@Override
	public void update()
	{
		synchronized( inputs )
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

		final InputEvent input = cache.take() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( InputType.KEYBOARD_PRESSED, keycode, _event.getWhen() ) ;

		synchronized( inputs )
		{
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

		KeyCode keycode = KeyCode.getKeyCode( _event.getKeyChar() ) ;
		if( keycode == KeyCode.NONE )
		{
			keycode = KeyCode.getKeyCode( ( int )_event.getKeyCode() ) ;
		}

		final InputEvent input = cache.take() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( InputType.KEYBOARD_RELEASED, keycode, _event.getWhen() ) ;

		synchronized( inputs )
		{
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
				updateMouse( InputType.MOUSE1_PRESSED, _event ) ;
				break ;
			}
			case MouseEvent.BUTTON2 :
			{
				updateMouse( InputType.MOUSE2_PRESSED, _event ) ;
				break ;
			}
			case MouseEvent.BUTTON3 :
			{
				updateMouse( InputType.MOUSE3_PRESSED, _event ) ;
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
				updateMouse( InputType.MOUSE1_RELEASED, _event ) ;
				break ;
			}
			case MouseEvent.BUTTON2 :
			{
				updateMouse( InputType.MOUSE2_RELEASED, _event ) ;
				break ;
			}
			case MouseEvent.BUTTON3 :
			{
				updateMouse( InputType.MOUSE3_RELEASED, _event ) ;
				break ;
			}
		}
	}

	public void mouseDragged( final MouseEvent _event )
	{
		updateMouse( InputType.MOUSE_MOVED, _event ) ;
	}

	public void mouseMoved( final MouseEvent _event )
	{
		updateMouse( InputType.MOUSE_MOVED, _event ) ;
	}

	/**  Recieve MouseWheelEvents from system **/

	public void mouseWheelMoved( final MouseEvent _event )
	{
		updateMouseWheel( _event ) ;
	}

	private void updateMouseWheel( final MouseEvent _event )
	{
		final InputEvent input = cache.take() ;
		final int scroll = ( int )_event.getRotation()[1] ;

		input.setInput( InputType.SCROLL_WHEEL, scroll, scroll, _event.getWhen() ) ;
		
		synchronized( inputs )
		{
			inputs.add( input ) ;
		}
	}

	private void updateMouse( final InputType _inputType, final MouseEvent _event )
	{
		final InputEvent input = cache.take() ;
		input.setID( InputID.MOUSE_1 ) ;
		input.setInput( _inputType, _event.getX(), _event.getY(), _event.getWhen() ) ;

		synchronized( inputs )
		{
			inputs.add( input ) ;
		}
	}

	@Override
	public void clearHandlers()
	{
		handlers.clear() ;
	}

	@Override
	public void clearInputs()
	{
		synchronized( inputs )
		{
			inputs.clear() ;
		}
	}

	private final boolean exists( final IInputHandler _handler )
	{
		return handlers.contains( _handler ) ;
	}
}
