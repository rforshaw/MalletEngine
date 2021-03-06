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
	public final Hull[] hulls ;
	private boolean applyContact = true ;

	private final ContactPoint point = new ContactPoint() ;
	private final Vector2 penShift = new Vector2() ;

	public CollisionComponent( final Entity _parent, final Hull ... _hulls )
	{
		this( _parent, Entity.AllowEvents.YES, _hulls ) ;
	}

	public CollisionComponent( final Entity _parent,
							   final Entity.AllowEvents _allow,
							   final Hull ... _hulls )
	{
		super( _parent, _allow ) ;
		hulls = _hulls ;
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

	public void setCollisionCallback( final int _index, final CollisionCallback _callback )
	{
		hulls[_index].setCollisionCallback( _callback ) ;
	}

	@Override
	public void passInitialEvents( final List<Event<?>> _events )
	{
		for( final Hull hull : hulls )
		{
			_events.add( new Event<Hull>( "ADD_COLLISION_HULL", hull ) ) ;
		}
	}

	@Override
	public void passFinalEvents( final List<Event<?>> _events )
	{
		for( final Hull hull : hulls )
		{
			_events.add( new Event<Hull>( "REMOVE_COLLISION_HULL", hull ) ) ;
		}
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;

		if( applyContact == true )
		{
			penShift.setXY( 0.0f, 0.0f ) ;

			// Shift the hulls position by the penetration depth.
			for( int i = 0; i < hulls.length; ++i )
			{
				final Hull hull = hulls[i] ;
				Hull.calculatePenetrationDepth( hull.contactData, point, penShift ) ;
				hull.addToPosition( penShift.x, penShift.y ) ;
			}
		}
	}

	public void setRotate( final int _index, final float _theta )
	{
		hulls[_index].setRotation( _theta ) ;
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
