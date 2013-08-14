package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;

public final class CollisionCheck
{
	private static final Vector2 toCenter = new Vector2() ;
	private static final Vector2 axis = new Vector2() ;
	private static final Vector2[] axes = new Vector2[4] ;

	public static final boolean intersectByAABB( final Box2D _box1, final Box2D _box2 )
	{
		final AABB box1 = _box1.aabb ;
		final AABB box2 = _box2.aabb ;
		
		return box1.intersectAABB( box2 ) ;
	}

	public static final void generateContactPoint( final Box2D _box1, final Box2D _box2 )
	{
		final OBB box1 = _box1.obb ;
		final OBB box2 = _box2.obb ;

		box1.updateAxesAndEdges() ;
		box2.updateAxesAndEdges() ;

		final Vector2 bC1 = _box1.aabb.getAbsoluteCenter() ;
		final Vector2 bC2 = _box2.aabb.getAbsoluteCenter() ;
		toCenter.x = bC2.x - bC1.x ;
		toCenter.y = bC2.y - bC1.y ;

		// Make it easy to loop through axes from box1 and box2
		axes[0] = box1.axes[0] ;
		axes[1] = box1.axes[1] ;
		axes[2] = box2.axes[0] ;
		axes[3] = box2.axes[1] ;

		int index = 0 ;
		float bestOverlap = penetrationOnAxis( box1, box2, axes[index], toCenter ) ;

		for( int i = 1; i < axes.length; i++ )
		{
			//System.out.println( i ) ;
			float result = penetrationOnAxis( box1, box2, axes[i], toCenter ) ;
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

	private static final float penetrationOnAxis( final OBB _obbA, 
												  final OBB _obbB, 
												  final Vector2 _axis, 
												  final Vector2 _toCenter )
	{
		final float projectA = transformToAxis( _obbA, _axis ) ;
		final float projectB = transformToAxis( _obbB, _axis ) ;
		final float distance = Math.abs( Vector2.multiply( _toCenter, _axis ) ) ;

		return projectA + projectB - distance ;
	}

	private static final float transformToAxis( final OBB _obb, final Vector2 _axis )
	{
		float dp = Vector2.dot( _obb.points[0], _axis ) ;
		float max = dp ;
		float min = dp ;

		for( int i = 1; i < _obb.points.length; ++i )
		{
			dp = Vector2.dot( _obb.points[i], _axis ) ;
			if( dp > max ) { max = dp ; }
			else if( dp < min ) { min = dp ; }
		}

		return ( max - min ) * 0.5f ;
	}
}