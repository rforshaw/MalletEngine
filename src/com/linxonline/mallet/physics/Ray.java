package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.* ;

/**
	Used internally by the collision-system to determine
	if an object has collided with the ray.
	The ray is built to try and reduce the creation of temporary
	objects, in particular Vectors, and AABB.
*/
class Ray
{
	private AABB aabb ;

	private final Vector3 point = new Vector3() ;
	private final Vector3 direction = new Vector3() ;
	private final Intersection intersection = new Intersection() ;

	public Ray() {}

	public void setFromPoints( final float _x1, final float _y1, final float _z1,
							   final float _x2, final float _y2, final float _z2 )
	{
		point.setXYZ( _x1, _y1, _z1 ) ;
		direction.setXYZ( _x2, _y2, _z2 ) ;
		direction.subtract( _x1, _y1, _z1 ) ;
	}

	public void setFromPoints( final float _x1, final float _y1,
							   final float _x2, final float _y2 )
	{
		point.setXYZ( _x1, _y1, 0.0f ) ;
		direction.setXYZ( _x2, _y2, 0.0f ) ;
		direction.subtract( _x1, _y1, 0.0f ) ;
	}

	public void setPoint( final float _x, final float _y, final float _z )
	{
		point.setXYZ( _x, _y, _z ) ;
	}

	public void setDirection( final float _x, final float _y, final float _z )
	{
		direction.setXYZ( _x, _y, _z ) ;
	}

	public Vector3 getPoint()
	{
		return point ;
	}

	public Vector3 getDirection()
	{
		return direction ;
	}

	public Intersection getIntersection()
	{
		return intersection ;
	}

	public AABB getTempAABB()
	{
		if( aabb == null )
		{
			aabb = new AABB() ;
		}

		return aabb ;
	}
}
