package com.linxonline.mallet.input.desktop ;

import com.jogamp.newt.event.MouseEvent ;
import com.jogamp.newt.event.KeyEvent ;
import com.jogamp.newt.event.MouseListener ;
import com.jogamp.newt.event.KeyListener ;

import com.linxonline.mallet.input.IInputHandler ;
import com.linxonline.mallet.input.IInputSystem ;
import com.linxonline.mallet.input.InputType ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.input.InputID ;
import com.linxonline.mallet.input.KeyCode ;

public final class InputSystem implements IInputSystem, KeyListener, MouseListener
{
	private final InputEvent[] inputs = new InputEvent[20] ;
	private int current = 0 ;
	private int start = 0 ;
	private int end = 0 ;

	public InputSystem()
	{
		for( int i = 0; i < inputs.length; ++i )
		{
			inputs[i] = new InputEvent() ;
		}
	}

	public void passInputs( final long _from, final IInputHandler _handler )
	{
		synchronized( inputs )
		{
			if( start == end )
			{
				return ;
			}
			else if( end > start )
			{
				for( int i = start; i < end; ++i )
				{
					final InputEvent input = inputs[i] ;
					if( input != null && input.getWhen() >= _from )
					{
						passInputEventToHandler( i, _handler ) ;
					}
				}
			}
			else
			{
				for( int i = start; i < inputs.length; ++i )
				{
					final InputEvent input = inputs[i] ;
					if( input != null && input.getWhen() >= _from )
					{
						passInputEventToHandler( i, _handler ) ;
					}
				}

				for( int i = 0; i < end; ++i )
				{
					final InputEvent input = inputs[i] ;
					if( input != null && input.getWhen() >= _from )
					{
						passInputEventToHandler( i, _handler ) ;
					}
				}
			}
		}
	}

	private void passInputEventToHandler( final int _index, final IInputHandler _handler )
	{
		final InputEvent input = inputs[_index] ;

		switch( _handler.passInputEvent( input ) )
		{
			case CONSUME   :
			{
				inputs[_index] = null ;
				break ;
			}
			case PROPAGATE : break ;
			default        : return ;
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

		final InputEvent input = take() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( InputType.KEYBOARD_PRESSED, keycode, _event.getWhen() ) ;
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

		final InputEvent input = take() ;
		input.setID( InputID.KEYBOARD_1 ) ;
		input.setInput( InputType.KEYBOARD_RELEASED, keycode, _event.getWhen() ) ;
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
		final InputEvent input = take() ;
		final int scroll = ( int )_event.getRotation()[1] ;
		input.setInput( InputType.SCROLL_WHEEL, scroll, scroll, _event.getWhen() ) ;
	}

	private void updateMouse( final InputType _inputType, final MouseEvent _event )
	{
		final InputEvent input = take() ;
		input.setID( InputID.MOUSE_1 ) ;
		input.setInput( _inputType, _event.getX(), _event.getY(), _event.getWhen() ) ;
	}

	@Override
	public void clearInputs()
	{
		synchronized( inputs )
		{
			current = 0 ;
			start = 0 ;
			end = 0 ;
		}
	}

	private InputEvent take()
	{
		synchronized( inputs )
		{
			final int size = inputs.length ;

			end = ++end % size ;
			if( start == end )
			{
				start = ++start % size ;
			}

			final InputEvent input = inputs[current] ;
			current = ++current % size ;

			return input ;
		}
	}
}
