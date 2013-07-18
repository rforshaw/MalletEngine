package com.linxonline.mallet.entity ;

import java.lang.StringBuffer ;

import com.linxonline.mallet.resources.texture.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.* ;

/*==============================================================*/
// TextAreaComponent - Graphical TextArea.					  //
// Requires RenderComponent.									  //
/*==============================================================*/

public class TextAreaComponent extends InputComponent
{
	private static final String DRAWLINE = "DRAWLINE" ;
	private static final String TEXTWIDTH = "TEXTWIDTH" ;
	private static final String TEXT = "TEXT" ;
	private static final String WORDS = "WORDS" ;

	private static final Event deadEvent = new Event( "", null ) ;
	private static final Event showKeyboard = new Event( "SHOW_KEYBOARD", true ) ;
	private static final Event returnEvent = new Event( "RETURN_KEY_PRESSED", null ) ;
	private static final Vector2 DEFAULT_OFFSET = new Vector2() ;

	private Texture texture = null ;
	public RenderComponent renderComponent = null ;
	public RenderComponent textComponent = null ;
	public EventComponent eventComponent = null ;

	private Line cursor = new Line( new Vector2( 0, 5 ), new Vector2( 0, 30 ) ) ;
	private float blink = 0.0f ;
	
	private final StringBuffer buffer = new StringBuffer() ;
	private int maxCharCount = 18 ;
	private boolean initialPress = false ;
	private boolean isSelected = false ;

	protected Vector2 dimension = new Vector2( 100.0f, 10.0f ) ;
	private Vector2 offset = new Vector2() ;
	protected int mouseX = 0 ;
	protected int mouseY = 0 ;

	public TextAreaComponent()
	{
		super( "TEXTAREA", "INPUTCOMPONENT" ) ;
	}

	public void setDimensions( final float _width, final float _height )
	{
		dimension = new Vector2( _width, _height ) ;
	}
	
	public final void setEventComponent( final EventComponent _component )
	{
		eventComponent = _component ;
		eventComponent.setSendToEntityComponents( false ) ;
	}

	public final void setRenderComponent( final RenderComponent _component )
	{
		renderComponent = _component ;
		renderComponent.getDrawAt( 0 ).addObject( "DRAWLINE", cursor ) ;
		offset.y = _component.getDrawAt( 0 ).getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ).y ;
		texture = _component.getDrawAt( 0 ).getObject( "TEXTURE", Texture.class, null ) ;
	}

	public final void setTextComponent( final RenderComponent _component )
	{
		textComponent = _component ;
		offset.x = _component.getDrawAt( 0 ).getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ).x ;
	}

	@Override
	public void update( final float _dt )
	{
		/*if( isSelected == true )
		{
			cursorBlink( _dt ) ;
		}

		final int textWidth = textComponent.getDrawAt( 0 ).getInteger( TEXTWIDTH, -1 ) ;
		if( textWidth != -1 )
		{
			cursor.start.x = 0.0f + offset.x + textWidth ;
			cursor.start.y = 5.0f + offset.y ;

			cursor.end.x = 0.0f + offset.x + textWidth ;
			cursor.end.y = 30.0f + offset.y ;
		}*/

		updateInputs() ;
	}

	public void cursorBlink( final float _dt )
	{
		blink += _dt ;
		if( blink <= 0.5f )
		{
			setCursor( null ) ;
			return ;
		}

		setCursor( cursor ) ;
		if( blink >= 1.0f )
		{
			blink = 0.0f ;
		}
	}

	private void setCursor( final Line _cursor )
	{
		sendDeadEvent() ;
		renderComponent.getDrawAt( 0 ).addObject( DRAWLINE, _cursor ) ;
	}
	
	protected void updateInputs()
	{
		final int size = inputs.size() ;
		InputEvent event = null ;
	
		for( int i = 0; i < size; ++i )
		{
			event = inputs.get( i ) ;
			updateKeyboardInputs( event ) ;

			if( event.inputType == InputType.MOUSE_MOVED ||
				event.inputType == InputType.TOUCH_MOVE )
			{
				mouseX = ( int )event.mouseX ;
				mouseY = ( int )event.mouseY ;
				continue ;
			}

			if( event.inputType == InputType.MOUSE1_PRESSED ||
				event.inputType == InputType.TOUCH_DOWN )
			{
				mouseX = ( int )event.mouseX ;
				mouseY = ( int )event.mouseY ;

				if( intersect( mouseX, mouseY ) )
				{
					isSelected = true ;
					pressed( mouseX, mouseY ) ;
					continue ;
				}
				else
				{
					isSelected = false ;
					setCursor( null ) ;
					eventComponent.passEvent( returnEvent ) ;
				}
			}
		}

		inputs.clear() ;
	}

	public void updateKeyboardInputs( final InputEvent _event )
	{
		if( isSelected == false )
		{
			return ;
		}

		if( _event.inputType == InputType.KEYBOARD_PRESSED )
		{
			final KeyCode keycode = _event.getKeyCode() ;
			if( keycode == KeyCode.SHIFT || keycode == KeyCode.CTRL ||
				keycode == KeyCode.ALT || keycode == KeyCode.ALTGROUP ||
				keycode == KeyCode.BACKSPACE || keycode == KeyCode.ENTER )
			{
				if( keycode == KeyCode.BACKSPACE )
				{
					final int length = buffer.length() ;
					if( length > 0 )
					{
						buffer.deleteCharAt( length - 1 ) ;
						setText( buffer.toString() ) ;
					}
				}
				else if( keycode == KeyCode.ENTER )
				{
					eventComponent.passEvent( returnEvent ) ;
				}

				return ;
			}
		
			if( buffer.length() < maxCharCount )
			{
				buffer.append( _event.getKeyCharacter() ) ;
				setText( buffer.toString() ) ;
			}
		}
	}

	public final boolean intersect( final int _x, final int _y )
	{ 
		final Vector3 pos = parent.getPosition() ;
		final int x = ( int )( pos.x + offset.x ) ;
		final int y = ( int )( pos.y + offset.y ) ;

		int width = x ;
		int height = y ;
		if( dimension != null )
		{
			width += ( int )dimension.x ;
			height += ( int )dimension.y ;
		}

		if( _x >= x && _x <= width )
		{
			if( _y >= y && _y <= height )
			{
				return true ;
			}
		}

		return false ;
	}

	protected void pressed( final int _x, final int _y )
	{
		if( initialPress == false )
		{
			setText( buffer.toString() ) ;
			initialPress = true ;
		}

		eventComponent.passEvent( showKeyboard ) ;
	}

	public void setText( final String _text )
	{
		textComponent.getDrawAt( 0 ).addString( TEXT, _text ) ;
		textComponent.getDrawAt( 0 ).addObject( WORDS, null ) ;
		textComponent.getDrawAt( 0 ).addInteger( TEXTWIDTH, -1 ) ;
		sendDeadEvent() ;
	}

	public String getText()
	{
		return textComponent.getDrawAt( 0 ).getString( TEXT ) ;
	}

	private void sendDeadEvent()
	{
		eventComponent.passEvent( deadEvent ) ;
	}
}