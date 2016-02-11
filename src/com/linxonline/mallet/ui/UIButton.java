package com.linxonline.mallet.ui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

/**
	Used to determine if the user has acted within a UI area.

	When the user has clicked, rolled over, or the location 
	is reset to a neutral position an event defined by the 
	developer is sent through the entity's event-system.

	The event can then be picked up by other components such as a 
	render-component to modify the visual element of the entity.
*/
public abstract class UIButton extends UIElement
{
	private final Vector2 mouse = new Vector2() ;
	private State current = State.NEUTRAL ;

	private enum State
	{
		NEUTRAL,
		ROLLOVER,
		CLICKED
	}

	public UIButton( final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _length )
	{
		setPosition( _position.x, _position.y, _position.z ) ;
		setOffset( _offset.x, _offset.y, _offset.z ) ;
		setLength( _length.x, _length.y, _length.z ) ;
	}

	public UIButton( final Vector3 _position,
					 final Vector2 _offset,
					 final Vector2 _length )
	{
		setPosition( _position.x, _position.y, _position.z ) ;
		setOffset( _offset.x, _offset.y, 0.0f ) ;
		setLength( _length.x, _length.y, 0.0f ) ;
	}

	@Override
	public void update( final float _dt ) {}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		final InputType type = _event.getInputType() ;
		switch( type )
		{
			case MOUSE_MOVED : 
			case TOUCH_MOVE  : updateMousePosition( _event ) ; break ;
		}

		if( intersectPoint( mouse.x, mouse.y ) == true )
		{
			switch( type )
			{
				case MOUSE1_PRESSED :
				{
					if( current != State.CLICKED )
					{
						clicked( _event ) ;
						current = State.CLICKED ;
						return InputEvent.Action.CONSUME ;
					}
					break ;
				}
				case MOUSE_MOVED :
				{
					if( current != State.ROLLOVER )
					{
						rollover( _event ) ;
						current = State.ROLLOVER ;
					}
					return InputEvent.Action.CONSUME ;
				}
			}
		}

		if( current != State.NEUTRAL )
		{
			neutral( _event ) ;
			current = State.NEUTRAL ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	/**
		Override to implement custom click process
	*/
	public abstract void clicked( final InputEvent _event ) ;

	/**
		Override to implement custom click process
	*/
	public abstract void rollover( final InputEvent _event ) ;

	/**
		Override to implement custom click process
	*/
	public abstract void neutral( final InputEvent _event );

	public void updateMousePosition( final InputEvent _event )
	{
		final InputAdapterInterface adapter = getInputAdapter() ;
		if( adapter != null )
		{
			mouse.x = adapter.convertInputToUIRenderX( _event.mouseX ) ;
			mouse.y = adapter.convertInputToUIRenderY( _event.mouseY ) ;
		}
	}
}