package com.linxonline.mallet.input.web ;

import java.util.Map ;
import java.util.List ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;
import org.teavm.jso.dom.html.* ;
import org.teavm.jso.dom.events.* ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.util.caches.TimeCache ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

public class InputSystem implements InputSystemInterface
{
	public InputAdapterInterface inputAdapter = null ;
	private final TimeCache<InputEvent> cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class ) ;

	private final List<InputHandler> handlers = MalletList.<InputHandler>newList() ;
	private final Map<KeyCode, KeyState> keyboardState = MalletMap.<KeyCode, KeyState>newMap() ;
	private final List<KeyState> activeKeyStates = MalletList.<KeyState>newList() ;

	private final List<InputEvent> mouseInputs = MalletList.<InputEvent>newList() ;
	private final Vector2 mousePosition = new Vector2( 0, 0 ) ;

	public InputSystem()
	{
		final HTMLDocument document = HTMLDocument.current();
		final HTMLCanvasElement canvas = ( HTMLCanvasElement )document.getElementById( "mallet-canvas" ) ;

		final EventListener<MouseEvent> mouseDown = new EventListener<MouseEvent>()
		{
			public void handleEvent( final MouseEvent _event )
			{
				switch( _event.getButton() )
				{
					case MouseEvent.LEFT_BUTTON :
					{
						mousePosition.x = _event.getClientX() ;
						mousePosition.y = _event.getClientY() ;
						updateMouse( InputType.MOUSE1_PRESSED, mousePosition ) ;
						break ;
					}
					case MouseEvent.MIDDLE_BUTTON :
					{
						mousePosition.x = _event.getClientX() ;
						mousePosition.y = _event.getClientY() ;
						updateMouse( InputType.MOUSE2_PRESSED, mousePosition ) ;
						break ;
					}
					case MouseEvent.RIGHT_BUTTON :
					{
						mousePosition.x = _event.getClientX() ;
						mousePosition.y = _event.getClientY() ;
						updateMouse( InputType.MOUSE3_PRESSED, mousePosition ) ;
						break ;
					}
				}
			}
		} ;

		final EventListener<MouseEvent> mouseUp = new EventListener<MouseEvent>()
		{
			public void handleEvent( final MouseEvent _event )
			{
				switch( _event.getButton() )
				{
					case MouseEvent.LEFT_BUTTON :
					{
						mousePosition.x = _event.getClientX() ;
						mousePosition.y = _event.getClientY() ;
						updateMouse( InputType.MOUSE1_RELEASED, mousePosition ) ;
						break ;
					}
					case MouseEvent.MIDDLE_BUTTON :
					{
						mousePosition.x = _event.getClientX() ;
						mousePosition.y = _event.getClientY() ;
						updateMouse( InputType.MOUSE2_RELEASED, mousePosition ) ;
						break ;
					}
					case MouseEvent.RIGHT_BUTTON :
					{
						mousePosition.x = _event.getClientX() ;
						mousePosition.y = _event.getClientY() ;
						updateMouse( InputType.MOUSE3_RELEASED, mousePosition ) ;
						break ;
					}
				}
			}
		} ;

		final EventListener<MouseEvent> mouseMove = new EventListener<MouseEvent>()
		{
			public void handleEvent( final MouseEvent _event )
			{
				mousePosition.x = _event.getClientX() ;
				mousePosition.y = _event.getClientY() ;
				updateMouse( InputType.MOUSE_MOVED, mousePosition ) ;
			}
		} ;

		final EventListener<MouseEvent> mouseWheel = new EventListener<MouseEvent>()
		{
			public void handleEvent( final MouseEvent _event )
			{
			
			}
		} ;

		final EventListener<KeyboardEvent> keyDown = new EventListener<KeyboardEvent>()
		{
			public void handleEvent( final KeyboardEvent _event )
			{
			
			}
		} ;

		final EventListener<KeyboardEvent> keyUp = new EventListener<KeyboardEvent>()
		{
			public void handleEvent( final KeyboardEvent _event )
			{
			
			}
		} ;
		
		canvas.addEventListener( "mousedown", mouseDown, true ) ;
		document.addEventListener( "mousedown", mouseDown, true ) ;

		canvas.addEventListener( "mouseup", mouseUp, true ) ;
		document.addEventListener( "mouseup", mouseUp, true ) ;

		canvas.addEventListener( "mousemove", mouseMove, true ) ;
		document.addEventListener( "mousemove", mouseMove, true ) ;

		canvas.addEventListener( "mousewheel", mouseWheel, true ) ;

		document.addEventListener( "keydown", keyDown, false ) ;
		document.addEventListener( "keyup", keyUp, false ) ;
		//document.addEventListener( "keypress", keyPress, false ) ;

		canvas.addEventListener( "touchstart", mouseDown ) ;
		canvas.addEventListener( "touchmove", mouseMove ) ;
		canvas.addEventListener( "touchcancel", mouseUp ) ;
		canvas.addEventListener( "touchend", mouseUp ) ;
	}

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

		handlers.remove( _handler ) ;
		_handler.reset() ;
	}

	/*private synchronized void updateMouseWheel( MouseWheelEvent _event )
	{
		final int scroll = _event.getWheelRotation() ;
		final InputEvent input = new InputEvent( InputType.SCROLL_WHEEL, scroll, scroll ) ;
		mouseInputs.add( input ) ;
	}*/

	private synchronized void updateMouse( final InputType _inputType, final Vector2 _mousePosition )
	{
		final InputEvent input = cache.get() ;
		input.setInput( _inputType, ( int )_mousePosition.x, ( int )_mousePosition.y ) ;
		mouseInputs.add( input ) ;
	}

	public synchronized void update()
	{
		passKeyInputs() ;
		passMouseInputs() ;
	}

	private void passKeyInputs()
	{
		final int stateSize = activeKeyStates.size() ;
		KeyState state = null ;

		for( int i = 0; i < stateSize; i++ )
		{
			state = activeKeyStates.get( i ) ;
			final InputEvent input = cache.get() ;
			input.clone( state.input ) ;

			passInputEventToHandlers( input ) ;
		}

		activeKeyStates.clear() ;
	}

	private void passMouseInputs()
	{
		final int inputSize = mouseInputs.size() ;
		InputEvent input = null ;

		for( int i = 0; i < inputSize; ++i )
		{
			input = mouseInputs.get( i ) ;
			passInputEventToHandlers( input ) ;
		}

		mouseInputs.clear() ;
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

	public void clearHandlers()
	{
		final int size = handlers.size() ;
		for( int i = 0; i < size; ++i )
		{
			handlers.get( i ).reset() ;
		}
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
		assert _handler != null ; 
		return handlers.contains( _handler ) ;
	}
}
