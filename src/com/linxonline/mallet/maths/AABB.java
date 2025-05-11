package com.linxonline.mallet.maths ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class AABB
{
	public float minX, minY ;
	public float maxX, maxY ;

	private AABB( final float _minX, final float _minY,
				  final float _maxX, final float _maxY )
	{
		minX = _minX ;
		minY = _minY ;
		maxX = _maxX ;
		maxY = _maxY ;
	}

	private AABB( final OBB _obb )
	{
		setFromOBB( _obb ) ;
	}

	public static AABB create()
	{
		return new AABB( 0.0f, 0.0f, 0.0f, 0.0f ) ;
	}

	public static AABB create( final Vector2 _length )
	{
		return new AABB( 0.0f, 0.0f, _length.x, _length.y ) ;
	}

	public static AABB create( final Vector2 _min, final Vector2 _max )
	{
		return new AABB( _min.x, _min.y, _max.x, _max.y ) ;
	}

	public static AABB create( final float _maxX, final float _maxY )
	{
		return new AABB( 0.0f, 0.0f, _maxX, _maxY ) ;
	}

	public static AABB create( final float _minX, final float _minY,
							   final float _maxX, final float _maxY )
	{
		return new AABB( _minX, _minY, _maxX, _maxY ) ;
	}

	public static AABB create( final OBB _obb )
	{
		return new AABB( _obb ) ;
	}

	/**
		Calculate the dimensions of the AABB from the OBB.
		Doesn't apply the OBB's position and offset to the AABB.
	*/
	public void setFromOBB( final OBB _obb )
	{
		final Vector2 point = _obb.getPoint( 0, new Vector2() ) ;

		minX = point.x ;
		minY = point.y ;
		maxX = point.x ;
		maxY = point.y ;

		final int length = _obb.getLength() ;
		for( int i = 1; i < length; ++i )
		{
			_obb.getPoint( i, point ) ;
			if( point.x < minX )
			{
				minX = point.x ;
			}
			else if( point.x > maxX )
			{
				maxX = point.x ;
			}

			if( point.y < minY )
			{
				minY = point.y ;
			}
			else if( point.y > maxY )
			{
				maxY = point.y ;
			}
		}
	}

	public void set( final float _x1, final float _y1, final float _x2, final float _y2 )
	{
		minX = ( _x1 < _x2 ) ? _x1 : _x2 ;
		minY = ( _y1 < _y2 ) ? _y1 : _y2 ;

		maxX = ( _x1 > _x2 ) ? _x1 : _x2 ;
		maxY = ( _y1 > _y2 ) ? _y1 : _y2 ;
	}

	public void setMax( final float _x, final float _y )
	{
		maxX = _x ;
		maxY = _y ;
	}

	public void addToMax( final float _x, final float _y )
	{
		maxX += _x ;
		maxY += _y ;
	}

	public void setMin( final float _x, final float _y )
	{
		minX = _x ;
		minY = _y ;
	}

	public void addToMin( final float _x, final float _y )
	{
		minX += _x ;
		minY += _y ;
	}

	public AABB copyTo( final AABB _to )
	{
		_to.minX = minX ;
		_to.minY = minY ;
		_to.maxX = maxX ;
		_to.maxY = maxY ;
		return _to ;
	}

	public void addTo( final float _x, final float _y )
	{
		// Add x and y to min and max with one call.
		minX += _x ;
		minY += _y ;
		maxX += _x ;
		maxY += _y ;
	}

	/**
		Cast a ray starting from _position, in the _direction,
		and determine if it intersects the box.
		If it does intersect populate the _intersection.
		If it does intersect isValid() on the intersection will return true.
	*/
	public Intersection ray( final Vector3 _point, final Vector3 _direction, final Intersection _intersection )
	{
		_intersection.reset() ;

		float tmin = 0.0f ;
		float tmax = Float.MAX_VALUE ;

		// The ray runs parallel, only way it can intersect
		// is if the point is within the AABB. 
		if( MathUtil.isZero( _direction.x ) )
		{
			if( _point.x < minX || _point.x > maxX )
			{
				return _intersection ;
			}
		}
		else
		{
			final float ood = 1.0f / _direction.x ;
			float t1 = ( minX - _point.x ) * ood ;
			float t2 = ( maxX - _point.x ) * ood ;
			
			if( t1 > t2 )
			{
				final float temp = t1 ;
				t1 = t2 ;
				t2 = t1 ;
			}

			tmin = MathUtil.max( tmin, t1 ) ;
			tmax = MathUtil.min( tmax, t2 ) ;
			if( tmin > tmax )
			{
				return _intersection ;
			}
		}

		if( MathUtil.isZero( _direction.y ) )
		{
			if( _point.y < minY || _point.y > maxY )
			{
				return _intersection ;
			}
		}
		else
		{
			final float ood = 1.0f / _direction.y ;
			float t1 = ( minY - _point.y ) * ood ;
			float t2 = ( maxY - _point.y ) * ood ;
			
			if( t1 > t2 )
			{
				final float temp = t1 ;
				t1 = t2 ;
				t2 = t1 ;
			}

			tmin = MathUtil.max( tmin, t1 ) ;
			tmax = MathUtil.min( tmax, t2 ) ;
			if( tmin > tmax )
			{
				return _intersection ;
			}
		}

		// Only useful if we make AABB 3D.
		/*if( MathUtil.isZero( _direction.z ) )
		{
			if( _point.y < range[AABB.MIN_Z] || _point.y > range[AABB.MAX_Z] )
			{
				return _intersection ;
			}
		}
		else
		{
			final float ood = 1.0f / _direction.z ;
			float t1 = ( range[AABB.MIN_Z] - _point.z ) * ood ;
			float t2 = ( range[AABB.MAX_Z] - _point.z ) * ood ;
			
			if( t1 > t2 )
			{
				final float temp = t1 ;
				t1 = t2 ;
				t2 = t1 ;
			}

			tmin = MathUtil.max( tmin, t1 ) ;
			tmax = MathUtil.min( tmax, t2 ) ;
			if( tmin > tmax )
			{
				return _intersection ;
			}
		}*/

		final float x = _point.x + ( _direction.x * tmin ) ;
		final float y = _point.y + ( _direction.y * tmin ) ;
		//final float z = _point.z + ( _direction.z * tmin ) ;
		_intersection.setPoint( x, y, 0.0f ) ;
		_intersection.setDistance( tmin ) ;

		return _intersection ;
	}

	/**
		Determine if the point defined is located within the AABB.
	*/
	public boolean intersectPoint( final float _x, final float _y )
	{
		//System.out.println( "X: " + _x + " MIN: " + minX + " MAX: " + maxX ) ;
		if( _x >= minX && _x <= maxX )
		{
			//System.out.println( "Y: " + _y + " MIN: " + minY + " MAX: " + maxY ) ;
			if( _y >= minY && _y <= maxY )
			{
				return true ;
			}
		}
		return false ;
	}

	public boolean intersectAABB( final AABB _aabb )
	{
		final float thatMinX = _aabb.minX ;
		final float thatMaxX = _aabb.maxX ;

		if( ( thatMinX >= minX && thatMinX <= maxX ) ||
			( thatMaxX >= minX && thatMaxX <= maxX ) )
		{
			final float thatMinY = _aabb.minY ;
			final float thatMaxY = _aabb.maxY ;

			if( ( thatMinY >= minY && thatMinY <= maxY ) ||
				( thatMaxY >= minY && thatMaxY <= maxY ) )
			{
				return true ;
			}
		}

		return false ;
	}

	public Vector2 getCenter( final Vector2 _center )
	{
		final float centerX = ( maxX + minX ) * 0.5f ;
		final float centerY = ( maxY + minY ) * 0.5f ;

		_center.x = centerX ;
		_center.y = centerY ;
		return _center ;
	}

	public Vector2 getMin( final Vector2 _min )
	{
		_min.x = minX ;
		_min.y = minY ;
		return _min ;
	}

	public Vector2 getMax( final Vector2 _max )
	{
		_max.x = maxX ;
		_max.y = maxY ;
		return _max ;
	}

	@Override
	public String toString()
	{
		return "MIN: " + minX + " " + minY + "\nMAX: " + maxX + " " + maxY ;
	}
}
