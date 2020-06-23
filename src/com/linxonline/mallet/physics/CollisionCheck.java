package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.physics.primitives.OBB ;

import com.linxonline.mallet.physics.hulls.Box2D ;
import com.linxonline.mallet.physics.hulls.Hull ;

public final class CollisionCheck
{
	private final Vector2 toCenter = new Vector2() ;
	private final Vector2 axis = new Vector2() ;
	private final Vector2 axis1 = new Vector2() ;
	private final Vector2 axis2 = new Vector2() ;

	private final Vector2 boxCenter1 = new Vector2() ;
	private final Vector2 boxCenter2 = new Vector2() ;

	private final ContactPoint contact = new ContactPoint() ;

	public CollisionCheck() {}

	/**
		Generate a Contact Point if a collision has occured.
		Return true if a contact point was generated.
		Return false if no collision was detected.

		This implementation does not handle fast moving objects 
		that can 'teleport' through objects instead of 
		colliding with the object.

		To solve telporting a physic system would need to be 
		implemented so to handle translating hulls over time 
		can be correctly implemented. Rather than leaving 
		motion to the game-logic to update.
	*/
	public final boolean generateContactPoint( final Hull _box1, final Hull _box2 )
	{
		final boolean box1Interested = _box1.isCollidableWithGroup( _box2.getGroupID() ) ;
		final boolean box2Interested = _box2.isCollidableWithGroup( _box1.getGroupID() ) ;
		if( box1Interested == false && box2Interested == false )
		{
			return false ;
		}

		final AABB aabb1 = _box1.getAABB() ;
		final AABB aabb2 = _box2.getAABB() ; 
		if( aabb1.intersectAABB( aabb2 ) == false && 
			aabb2.intersectAABB( aabb1 ) == false )
		{
			return false ;
		}

		_box1.getAbsoluteCenter( boxCenter1 ) ;
		_box2.getAbsoluteCenter( boxCenter2 ) ;

		toCenter.x = boxCenter2.x - boxCenter1.x ;
		toCenter.y = boxCenter2.y - boxCenter1.y ;

		// Find the best overlap for _box1
		final float overlap1 = penetration( _box1, _box2, toCenter, axis1 ) ;
		if( overlap1 <= 0.0f )
		{
			return false ;
		}

		// Find the best overlap for _box2
		final float overlap2 = penetration( _box2, _box1, toCenter, axis2 ) ;
		if( overlap2 <= 0.0f )
		{
			return false ;
		}

		final float overlap = ( overlap1 < overlap2 ) ? overlap1 * 0.5f : overlap2 * 0.5f ;		// Get the best overlap overall
		final Vector2 axis = ( overlap1 < overlap2 ) ? axis1 : axis2 ;							// Set the axis based on best overlap

		if( Vector2.multiply( axis, toCenter ) > 0.0f )
		{
			axis.x *= -1.0f ;
			axis.y *= -1.0f ;
		}

		// If one of the hulls is not physical then it will not 
		// force the two hulls to push apart from each other.
		// If it did then hulls used for triggers could be 
		// 'pushed' away - or prevent the other hull from passing 
		// through it.
		final boolean physical = _box1.isPhysical() && _box2.isPhysical() ;

		if( box1Interested == true )
		{
			final int index1 = _box1.contactData.addContact( overlap, axis.x, axis.y, physical, _box2 ) ;
			if( index1 < ContactData.MAX_COLLISION_POINTS )
			{
				callback( _box1.contactData.get( index1, contact ), _box1.getCallback() ) ;
			}
			else
			{
				System.out.println( "Reached maximum contacts on hull 1" ) ;
			}
		}

		if( box2Interested == true )
		{
			final int index2 = _box2.contactData.addContact( overlap, -axis.x, -axis.y, physical, _box1 ) ;
			if( index2 < ContactData.MAX_COLLISION_POINTS )
			{
				callback( _box2.contactData.get( index2, contact ), _box2.getCallback() ) ;
			}
			else
			{
				System.out.println( "Reached maximum contacts on hull 2" ) ;
			}
		}

		return true ;
	}

	private final float penetration( final Hull _a, 
									 final Hull _b,
									 final Vector2 _toCenter,
									 final Vector2 _setAxis )
	{
		final float[] axes = _a.getAxes() ;

		FloatBuffer.fill( axes, axis, 0 ) ;
		float bestOverlap = penetrationOnAxis( _a, _b, axis, _toCenter ) ;
		if( bestOverlap <= 0.0f )
		{
			return bestOverlap ;
		}

		_setAxis.setXY( axis.x, axis.y ) ;
		for( int i = 2; i < axes.length; i += 2 )
		{
			FloatBuffer.fill( axes, axis, i ) ;
			final float result = penetrationOnAxis( _a, _b, axis, _toCenter ) ;
			if( result <= 0.0f )
			{
				return result ;
			}
			else if( result < bestOverlap )
			{
				bestOverlap = result ;
				_setAxis.setXY( axis.x, axis.y ) ;
			}
		}

		return bestOverlap ;
	}

	private static final float penetrationOnAxis( final Hull _a, 
												  final Hull _b, 
												  final Vector2 _axis, 
												  final Vector2 _toCenter )
	{
		final float projectA = _a.projectToAxis( _axis ) ;
		final float projectB = _b.projectToAxis( _axis ) ;
		final float distance = Math.abs( Vector2.multiply( _toCenter, _axis ) ) ;

		return projectA + projectB - distance ;
	}

	private static void callback( final ContactPoint _point, final CollisionCallback _callback )
	{
		if( _callback != null )
		{
			_callback.collisionCallback( _point ) ;
		}
	}
}
