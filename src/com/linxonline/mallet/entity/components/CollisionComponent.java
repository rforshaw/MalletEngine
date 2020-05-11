package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.physics.CollisionCallback ;
import com.linxonline.mallet.physics.ContactPoint ;

import com.linxonline.mallet.physics.primitives.* ;
import com.linxonline.mallet.physics.hulls.* ;

public class CollisionComponent extends Component
{
	public final Hull hull ;
	private boolean applyContact = true ;

	public CollisionComponent( final Entity _parent, final Hull _hull )
	{
		this( _parent, Entity.AllowEvents.YES, _hull ) ;
	}

	public CollisionComponent( final Entity _parent,
							   final Entity.AllowEvents _allow,
							   final Hull _hull )
	{
		super( _parent, _allow ) ;
		hull = _hull ;
	}

	/**
		Update the hulls position to take into account 
		contact data, using the penetration depth shift 
		the hull so it no longer collides with other objects.
	*/
	public void applyContact( final boolean _apply )
	{
		applyContact = _apply ;
	}

	public void setCollisionCallback( final CollisionCallback _callback )
	{
		hull.setCollisionCallback( _callback ) ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		_events.add( new Event<Hull>( "ADD_COLLISION_HULL", hull ) ) ;
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		_events.add( new Event<Hull>( "REMOVE_COLLISION_HULL", hull ) ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;

		if( applyContact == true )
		{
			// Shift the hulls position by the penetration depth.
			final Vector2 accumulated = hull.updateContactData() ;
			hull.addToPosition( accumulated.x, accumulated.y ) ;
		}
	}

	public void setRotate( final float _theta )
	{
		hull.setRotation( _theta ) ;
	}

	public static CollisionComponent generateBox2D( final Entity _parent,
													final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		final CollisionComponent comp = new CollisionComponent( _parent, new Box2D( new AABB( _min, _max, _position, _offset ) ) ) ;
		return comp ;
	}
	
	public static CollisionComponent generateBox2D( final Entity _parent,
													final Entity.AllowEvents _allow,
													final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		final CollisionComponent comp = new CollisionComponent( _parent, _allow, new Box2D( new AABB( _min, _max, _position, _offset ) ) ) ;
		return comp ;
	}
}
