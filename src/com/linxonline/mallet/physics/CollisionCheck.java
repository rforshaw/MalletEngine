package com.linxonline.mallet.physics ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.physics.primitives.OBB ;

import com.linxonline.mallet.physics.hulls.Box2D ;
import com.linxonline.mallet.physics.hulls.Hull ;

public final class CollisionCheck
{
	private static final Vector2 toCenter = new Vector2() ;
	private static final Vector2 axis = new Vector2() ;
	private static final ArrayList<Vector2> axes = new ArrayList<Vector2>() ;

	public static final void generateContactPoint( final Hull _box1, final Hull _box2 )
	{
		axes.clear() ;
		final Vector2[] axes1 = _box1.getAxes() ;
		final Vector2[] axes2 = _box2.getAxes() ;

		for( int i = 0; i < axes1.length; i++ )
		{
			axes.add( axes1[i] ) ;
		}

		for( int i = 0; i < axes2.length; i++ )
		{
			axes.add( axes2[i] ) ;
		}

		final Vector2 bC1 = _box1.getAbsoluteCenter() ;
		final Vector2 bC2 = _box2.getAbsoluteCenter() ;
		toCenter.x = bC2.x - bC1.x ;
		toCenter.y = bC2.y - bC1.y ;

		int index = 0 ;
		int size = axes.size() ;
		float bestOverlap = penetrationOnAxis( _box1, _box2, axes.get( index ), toCenter ) ;

		for( int i = 1; i < size; i++ )
		{
			float result = penetrationOnAxis( _box1, _box2, axes.get( i ), toCenter ) ;
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

		axis.x = axes.get( index ).x ;
		axis.y = axes.get( index ).y ;

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