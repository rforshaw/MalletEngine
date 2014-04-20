package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.renderer.* ;

public class CheckBoxComponent extends InputComponent
{
	public RenderComponent renderComponent = null ;
	public EventComponent eventComponent = null ;

	public String defaultBox = null ;
	public String defaultRollover = null ;
	public String defaultCheck = null ;
	public Vector2 boxDim = new Vector2( 64, 128 ) ;

	protected boolean isPressed = false ;
	protected Vector2 mouse = new Vector2() ;
	protected Vector2 offset = null ;
	protected ArrayList<Event> events = new ArrayList<Event>() ;

	public CheckBoxComponent()
	{
		super() ;
	}

	public void setBoxDimension( final float _width, final float _height )
	{
		boxDim.setXY( _width, _height ) ;
	}

	public final void setEventComponent( final EventComponent _component )
	{
		eventComponent = _component ;
		eventComponent.setSendToEntityComponents( true ) ;
	}

	public final void setRenderComponent( final RenderComponent _component )
	{
		renderComponent = _component ;
		offset = _component.getDrawAt( 0 ).getObject( "OFFSET", new Vector2() ) ;

		final Vector3 pos = _component.getDrawAt( 0 ).getObject( "POSITION", null ) ;
		renderComponent.add( DrawFactory.createTexture( null, pos, offset, null, null, null, null, 11 ) ) ;
		setBox( defaultBox, 10 ) ;
	}

	public final void addMessage( final Event _message )
	{
		events.add( _message ) ;
	}

	@Override
	public void update( final float _dt )
	{
		setBox( defaultBox, 10 ) ;
		
		if( intersect( mouse.x, mouse.y, boxDim ) == true )
		{
			setBox( defaultRollover, 10 ) ;
		}
		
		updateInputs() ;
	}

	protected void updateInputs()
	{
		InputEvent input = null ;
		final int size = inputs.size() ;
	
		for( int i = 0; i < size; ++i )
		{
			input = inputs.get( i ) ;
			if( input.inputType == InputType.MOUSE_MOVED ||
				input.inputType == InputType.TOUCH_MOVE )
			{
				mouse.x = input.mouseX ;
				mouse.y = input.mouseY ;
				continue ;
			}

			if( input.inputType == InputType.MOUSE1_PRESSED ||
				input.inputType == InputType.TOUCH_DOWN )
			{
				mouse.x = input.mouseX ;
				mouse.y = input.mouseY ;
			
				if( intersect( mouse.x, mouse.y, boxDim ) == true )
				{
					isPressed = true ;
					tick() ;
					pressed( mouse.x, mouse.y ) ;
					continue ;
				}
			}

			if( input.inputType == InputType.MOUSE1_RELEASED ||
				input.inputType == InputType.TOUCH_UP )
			{
				if( intersect( mouse.x, mouse.y, boxDim ) == true )
				{
					isPressed = false ;
					released( mouse.x, mouse.y ) ;
					continue ;
				}
			}
		}

		inputs.clear() ;
	}

	public void passMessages()
	{
		for( Event event : events )
		{
			if( eventComponent != null )
			{
				eventComponent.passEvent( event ) ;
			}
		}
	}

	public void tick()
	{
		final String tex = renderComponent.getDrawAt( 1 ).getString( "FILE", null ) ;
		if( tex == null )
		{
			renderComponent.getDrawAt( 1 ).addString( "FILE", defaultCheck ) ;
			renderComponent.getDrawAt( 1 ).addObject( "TEXTURE", null ) ;
			eventComponent.passEvent( new Event( "TICKED", null ) ) ;
		}
		else
		{
			renderComponent.getDrawAt( 1 ).addString( "FILE", null ) ;
			renderComponent.getDrawAt( 1 ).addObject( "TEXTURE", null ) ;
			eventComponent.passEvent( new Event( "UNTICKED", null ) ) ;
		}
	}

	protected void pressed( final float _x, final float _y ) {}

	protected void released( final float _x, final float _y )
	{
		passMessages() ;
	}

	private final boolean intersect( final float _x, final float _y, final Vector2 _dim )
	{
		final Vector3 pos = parent.getPosition() ;
		final float x = pos.x + offset.x ;
		final float y = pos.y + offset.y ;

		final float width = x + ( int )_dim.x ;
		final float height = y + ( int )_dim.y ;

		if( _x >= x && _x <= width )
		{
			if( _y >= y && _y <= height )
			{
				return true ;
			}
		}

		return false ;
	}

	protected final void setBox( final String _texture, final int _layer )
	{
		renderComponent.getDrawAt( 0 ).addString( "FILE", _texture ) ;
		renderComponent.getDrawAt( 0 ).addObject( "TEXTURE", null ) ;
		renderComponent.getDrawAt( 0 ).addInteger( "LAYER", _layer ) ;
	}
}