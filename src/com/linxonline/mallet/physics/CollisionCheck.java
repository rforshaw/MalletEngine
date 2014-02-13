package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.physics.primitives.OBB ;

import com.linxonline.mallet.physics.hulls.Box2D ;
import com.linxonline.mallet.physics.hulls.Hull ;

public final class CollisionCheck
{
	private static final Vector2 toCenter = new Vector2() ;
	private static final Vector2 axis = new Vector2() ;
	private static final Vector2[] axes = new Vector2[4] ;

	public static final void generateContactPoint( final Hull _box1, final Hull _box2 )
	{
		final Vector2[] axes1 = _box1.getAxes() ;
		final Vector2[] axes2 = _box2.getAxes() ;

		final Vector2 bC1 = _box1.getAbsoluteCenter() ;
		final Vector2 bC2 = _box2.getAbsoluteCenter() ;
		toCenter.x = bC2.x - bC1.x ;
		toCenter.y = bC2.y - bC1.y ;

		// Make it easy to loop through axes from box1 and box2
		axes[0] = axes1[0] ;
		axes[1] = axes1[1] ;
		axes[2] = axes2[0] ;
		axes[3] = axes2[1] ;

		int index = 0 ;
		float bestOverlap = penetrationOnAxis( _box1, _box2, axes[index], toCenter ) ;

		for( int i = 1; i < axes.length; i++ )
		{
			float result = penetrationOnAxis( _box1, _box2, axes[i], toCenter ) ;
			if( result < 0.0f )
			{
				//System.out.println( "Failed SATs: " + result ) ;
				return ;
			}
			else if( result < bestOverlap )
			{
				bestOverlap = result ;
				index = i ;
			}
		}

		axis.x = axes[index].x ;
		axis.y = axes[index].y ;

		if( Vector2.multiply( axis, toCenter ) > 0.0f )
		{
			axis.x *= -1.0f ;
			axis.y *= -1.0f ;
		}

		bestOverlap *= 0.5f ;
		_box1.contactData.addContact( bestOverlap, axis.x, axis.y, 
									  ( _box1.isPhysical() && _box2.isPhysical() ),
									  _box2 ) ;
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
}