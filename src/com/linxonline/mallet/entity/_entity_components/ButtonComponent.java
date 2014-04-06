package com.linxonline.mallet.entity ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.renderer.* ;

/*==============================================================*/
// ButtonComponent - Graphical 2D Button.					      //
// Requires RenderComponent and EventComponent to run.		  //
/*==============================================================*/

public class ButtonComponent extends InputComponent
{
	public final static int SEND_INTERNAL_AND_EXTERNAL = 0 ;
	public final static int SEND_EXTERNAL = 1 ;
	public final static int SEND_INTERNAL = 2 ;

	private static final Vector2 DEFAULT_OFFSET = new Vector2() ;

	public String button = null ;
	public String rollover = null ;
	public String pressed = null ;

	public RenderComponent renderComponent = null ;
	public RenderComponent textComponent = null ;
	public EventComponent eventComponent = null ;

	protected ArrayList<Event> events = new ArrayList<Event>() ;
	protected Vector2 dimension = null ;
	protected boolean rolledOver = false ;
	protected boolean isPressed = false ;
	private Vector2 offset = null ;
	protected int mouseX = 0 ;
	protected int mouseY = 0 ;
	protected int sendTo = SEND_EXTERNAL ;

	protected MalletColour defaultColour = null ;
	protected MalletColour pressedColour = null ;

	public ButtonComponent()
	{
		super( "BUTTON", "INPUTCOMPONENT" ) ;
	}

	public void setSendToMode( final int _mode )
	{
		sendTo = _mode ;
	}
	
	public void setDimensions( final float _width, final float _height )
	{
		dimension = new Vector2( _width, _height ) ;
	}
	
	public void setEventComponent( final EventComponent _component )
	{
		eventComponent = _component ;
		eventComponent.setSendToEntityComponents( true ) ;
	}

	public final void setRenderComponent( final RenderComponent _component )
	{
		renderComponent = _component ;
		offset = _component.getDrawAt( 0 ).getObject( "OFFSET", DEFAULT_OFFSET ) ;
		setButton( button, 10 ) ;
	}

	public final void setTextComponent( final RenderComponent _component )
	{
		textComponent = _component ;
	}
	
	public void setColours( final MalletColour _default, final MalletColour _select )
	{
		defaultColour = _default ;
		pressedColour = _select ;
	}

	public final void addMessage( final Event _message )
	{
		events.add( _message ) ;
	}

	@Override
	public void update( final float _dt )
	{
		rolledOver = false ;
		setTextColour( defaultColour ) ;
		setButton( button, 10 ) ;

		if( intersect( mouseX, mouseY ) )
		{
			rolledOver = true ;
			setButton( rollover, 10 ) ;
		}

		if( isPressed == true )
		{
			setTextColour( pressedColour ) ;
			setButton( pressed, 10 ) ;
		}

		updateInputs() ;
	}

	protected void updateInputs()
	{
		final int size = inputs.size() ;
		InputEvent event = null ;
		for( int i = 0; i < size; ++i )
		{
			event = inputs.get( i ) ;
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
					isPressed = true ;
					pressed( mouseX, mouseY ) ;
					continue ;
				}
			}

			if( event.inputType == InputType.MOUSE1_RELEASED ||
				event.inputType == InputType.TOUCH_UP )
			{
				isPressed = false ;
				mouseX = ( int )event.mouseX ;
				mouseY = ( int )event.mouseY ;

				if( intersect( mouseX, mouseY ) )
				{
					released( mouseX, mouseY ) ;
					continue ;
				}
			}
		}

		inputs.clear() ;
	}

	public void passMessages()
	{
		switch( sendTo )
		{
			case SEND_INTERNAL_AND_EXTERNAL :
			{
				passMessagesInternal() ;
				passMessagesExternal() ;
				break ;
			}
			case SEND_INTERNAL :
			{
				passMessagesInternal() ;
				break ;
			}
			case SEND_EXTERNAL :
			{
				passMessagesExternal() ;
				break ;
			}
		}
	}

	protected void passMessagesExternal()
	{
		for( Event event : events )
		{
			if( eventComponent != null )
			{
				eventComponent.passEvent( event ) ;
			}
		}
	}

	protected void passMessagesInternal()
	{
		for( Event event : events )
		{
			componentEvents.passEvent( event ) ;
		}
	}
	
	public final boolean intersect( final int _x, final int _y )
	{
		final Vector3 pos = parent.getPosition() ;
		final int x = ( int )( pos.x + offset.x ) ;
		final int y = ( int )( pos.y + offset.y ) ;

		int width = x ;
		int height = y  ;
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
		setTextColour( pressedColour ) ;
		setButton( pressed, 10 ) ;
	}

	protected void released( final int _x, final int _y )
	{
		passMessages() ;
	}

	public void setText( final String _text )
	{
		textComponent.getDrawAt( 0 ).addString( "TEXT", _text ) ;
	}

	protected final void setTextColour( final MalletColour _colour )
	{
		if( _colour != null )
		{
			textComponent.getDrawAt( 0 ).addObject( "COLOUR", _colour ) ;
		}
	}
	
	protected final void setButton( final String _button, final int _layer )
	{
		renderComponent.getDrawAt( 0 ).addString( "TEXTURE", null ) ;
		renderComponent.getDrawAt( 0 ).addString( "FILE", _button ) ;
	}
}