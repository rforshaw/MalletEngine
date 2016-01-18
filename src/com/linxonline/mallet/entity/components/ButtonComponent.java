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
public abstract class ButtonComponent extends MouseComponent
{
	private final AABB aabb = new AABB() ;
	private State current = State.NEUTRAL ;

	private enum State
	{
		NEUTRAL,
		ROLLOVER,
		CLICKED
	}

	public ButtonComponent()
	{
		super() ;
	}

	public ButtonComponent( final Vector3 _position,
							final Vector2 _offset,
							final Vector2 _min,
							final Vector2 _max )
	{
		super() ;
		setAABB( _position, _offset, _min, _max ) ;
	}
	
	public void setAABB( final Vector3 _position,
						 final Vector2 _offset,
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

		if( _position != null )
		{
			aabb.setPosition( _position.x, _position.y ) ;
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
					clicked( _event ) ;
					current = State.CLICKED ;
					return InputEvent.Action.CONSUME ;
				}
			}
			else if( mouseMoved == true )
			{
				if( current != State.ROLLOVER )
				{
					rollover( _event ) ;
					current = State.ROLLOVER ;
				}
				return InputEvent.Action.CONSUME ;
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

	@Override
	public void updateMousePosition( final InputEvent _event )
	{
		if( inputAdapter != null )
		{
			mouse.x = inputAdapter.convertInputToUIRenderX( _event.mouseX ) ;
			mouse.y = inputAdapter.convertInputToUIRenderY( _event.mouseY ) ;
		}
	}
}