package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.physics.CollisionAssist ;
import com.linxonline.mallet.physics.CollisionCallback ;
import com.linxonline.mallet.physics.ContactPoint ;

import com.linxonline.mallet.physics.hulls.* ;

public class CollisionComponent extends Component
{
	public final Hull[] hulls ;
	private boolean applyContact = true ;

	private ContactPoint point ;
	private Vector2 penShift ;

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
	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		CollisionAssist.remove( hulls ) ;
		super.readyToDestroy( _callback ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		if( applyContact == false )
		{
			return ;
		}

		if( point == null )
		{
			point = new ContactPoint() ;
			penShift = new Vector2() ;
		}

		// Shift the hulls position by the penetration depth.
		for( int i = 0; i < hulls.length; ++i )
		{
			penShift.setXY( 0.0f, 0.0f ) ;
			final Hull hull = hulls[i] ;
			Hull.calculatePenetrationDepth( hull.contactData, point, penShift ) ;
			hull.addToPosition( penShift.x, penShift.y ) ;
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
		final Box2D hull = CollisionAssist.createBox2D( new AABB( _min, _max ), null ) ;
		hull.setPosition( _position.x, _position.y ) ;
		hull.setOffset( _offset.x, _offset.y ) ;

		final CollisionComponent comp = new CollisionComponent( _parent, hull ) ;
		return comp ;
	}

	public static CollisionComponent generateBox2D( final Entity _parent,
													final Entity.AllowEvents _allow,
													final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		final Box2D hull = CollisionAssist.createBox2D( new AABB( _min, _max ), null ) ;
		hull.setPosition( _position.x, _position.y ) ;
		hull.setOffset( _offset.x, _offset.y ) ;

		final CollisionComponent comp = new CollisionComponent( _parent, _allow, hull ) ;
		return comp ;
	}
}
