package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.physics.CollisionCallback ;
import com.linxonline.mallet.physics.ContactPoint ;

import com.linxonline.mallet.physics.primitives.* ;
import com.linxonline.mallet.physics.hulls.* ;

public class CollisionComponent extends Component
{
	public final Hull hull ;

	public CollisionComponent( final String _name, final Hull _hull )
	{
		super( _name, "COLLISIONCOMPONENT" ) ;
		hull = _hull ;
	}

	public void setCollisionCallback( final CollisionCallback _callback )
	{
		hull.setCollisionCallback( _callback ) ;
	}

	@Override
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		_events.add( new Event<Hull>( "ADD_COLLISION_HULL", hull ) ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		_events.add( new Event<Hull>( "REMOVE_COLLISION_HULL", hull ) ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;

		// Shift the parents position by the penetration depth.
		final Vector2 accumulated = hull.updateContactData() ;
		parent.addToPosition( accumulated.x, accumulated.y, 0.0f ) ;
		// Set the hull to the parents new position.
		hull.setPosition( parent.position.x, parent.position.y ) ;
	}

	public void setRotate( final float _theta )
	{
		hull.setRotation( _theta ) ;
	}

	public static CollisionComponent generateBox2D( final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		return new CollisionComponent( "COLLISION", new Box2D( new AABB( _min, _max, _position, _offset ) ) ) ;
	}

	public static CollisionComponent generateBox2D( final String _name,
													final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		return new CollisionComponent( _name, new Box2D( new AABB( _min, _max, _position, _offset ) ) ) ;
	}
}