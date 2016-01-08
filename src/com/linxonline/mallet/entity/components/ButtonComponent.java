package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

/**
	Used to determine if the user has acted within a UI area.

	When the user has clicked, rolled over, or the location 
	is reset to a neutral position an event defined by the 
	developer is sent through the entity's event-system.

	The event can then be picked up by other components such as a 
	render-component to modify the visual element of the entity.
*/
public class ButtonComponent extends MouseComponent
{
	private final AABB aabb = new AABB() ;
	private final Event neutral ;
	private final Event rollover ;
	private final Event clicked ;

	private State current = State.NEUTRAL ;

	private enum State
	{
		NEUTRAL,
		ROLLOVER,
		CLICKED
	}

	public ButtonComponent( final Event _neutral,
							final Event _rollover,
							final Event _clicked )
	{
		super() ;
		neutral = _neutral ;
		rollover = _rollover ;
		clicked = _clicked ;
	}

	public void setAABB( final Vector2 _offset,
						 final Vector2 _min,
						 final Vector2 _max )
	{
		if( _min != null )
		{
			aabb.min.setXY( _min ) ;
		}

		if( _max != null )
		{
			aabb.max.setXY( _max ) ;
		}

		if( _offset != null )
		{
			aabb.offset.setXY( _offset ) ;
		}
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		super.passInputEvent( _event ) ;

		if( aabb.intersectPoint( mouse.x, mouse.y ) == true )
		{
			if( mouse1Pressed == true )
			{
				if( current != State.CLICKED )
				{
					sendEvent( clicked ) ;
					current = State.CLICKED ;
					return InputEvent.Action.CONSUME ;
				}
			}
			else if( mouseMoved == true )
			{
				if( current != State.ROLLOVER )
				{
					sendEvent( rollover ) ;
					current = State.ROLLOVER ;
				}
				return InputEvent.Action.CONSUME ;
			}
		}

		if( current != State.NEUTRAL )
		{
			sendEvent( neutral ) ;
			current = State.NEUTRAL ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;

		final Vector3 position = parent.getPosition() ;
		aabb.setPosition( position.x, position.y ) ;
	}

	@Override
	public void updateMousePosition( final InputEvent _event )
	{
		if( inputAdapter != null )
		{
			mouse.x = inputAdapter.convertInputToUIRenderX( _event.mouseX ) ;
			mouse.y = inputAdapter.convertInputToUIRenderY( _event.mouseY ) ;
		}
	}

	private void sendEvent( final Event _event )
	{
		final EventController controller = getComponentEventController() ;
		controller.passEvent( _event ) ;
	}
}