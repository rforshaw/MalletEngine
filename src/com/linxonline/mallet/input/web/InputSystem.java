package com.linxonline.mallet.input.web ;

import java.util.Map ;
import java.util.List ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;
import org.teavm.jso.dom.html.* ;
import org.teavm.jso.dom.events.* ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.util.caches.TimeCache ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

public final class InputSystem implements IInputSystem
{
	private final TimeCache<InputEvent> cache ;

	private final List<IInputHandler> handlers = MalletList.<IInputHandler>newList() ;

	private final List<InputEvent> inputs = MalletList.<InputEvent>newList() ;
	private final Vector2 mousePosition = new Vector2( 0, 0 ) ;

	public InputSystem()
	{
		final HTMLDocument document = HTMLDocument.current();

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

		final InputEvent[] inputs = new InputEvent[150] ;
		for( int i = 0; i < inputs.length; i++ )
		{
			inputs[i] = new InputEvent() ;
		}
		cache = new TimeCache<InputEvent>( 0.25f, InputEvent.class, inputs ) ;
		document.addEventListener( "mousedown", mouseDown ) ;
		document.addEventListener( "mouseup", mouseUp) ;
		document.addEventListener( "mousemove", mouseMove ) ;

		document.addEventListener( "keydown", keyDown ) ;
		document.addEventListener( "keyup", keyUp ) ;
	}

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
		if( exists( _handler ) == false )
		{
			return ;
		}

		handlers.remove( _handler ) ;
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
		input.setID( InputID.MOUSE_1 ) ;
		input.setInput( _inputType, ( int )_mousePosition.x, ( int )_mousePosition.y ) ;
		inputs.add( input ) ;
	}

	public synchronized void update()
	{
		if( inputs.isEmpty() == false )
		{
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

	public void clearHandlers()
	{
		handlers.clear() ;
	}

	public synchronized void clearInputs()
	{
		inputs.clear() ;
	}

	private final boolean exists( final IInputHandler _handler )
	{
		assert _handler != null ; 
		return handlers.contains( _handler ) ;
	}
}
