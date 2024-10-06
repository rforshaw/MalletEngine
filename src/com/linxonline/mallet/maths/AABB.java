package com.linxonline.mallet.maths ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class AABB
{
	public static final int RANGE_NUM = 2 ;
	public static final int VECTOR_TYPE = 2 ;

	public static final int MIN_X = 0 ;
	public static final int MIN_Y = 1 ;
	public static final int MAX_X = 2 ;
	public static final int MAX_Y = 3 ;

	// min then max
	public final float[] range = new float[RANGE_NUM * VECTOR_TYPE] ;

	private AABB( final float _minX, final float _minY,
				  final float _maxX, final float _maxY )
	{
		FloatBuffer.set( range, AABB.MIN_X, _minX, _minY ) ;
		FloatBuffer.set( range, AABB.MAX_X, _maxX, _maxY ) ;
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
		final Vector2 point = FloatBuffer.fill( _obb.rotations, new Vector2(), 0 ) ;
		FloatBuffer.set( range, AABB.MIN_X, point.x, point.y ) ;
		FloatBuffer.set( range, AABB.MAX_X, point.x, point.y ) ;

		for( int i = 2; i < _obb.rotations.length; i += 2 )
		{
			FloatBuffer.fill( _obb.rotations, point, i ) ;
			if( point.x < range[AABB.MIN_X] )
			{
				range[AABB.MIN_X] = point.x ;
			}
			else if( point.x > range[AABB.MAX_X] )
			{
				range[AABB.MAX_X] = point.x ;
			}

			if( point.y < range[AABB.MIN_Y] )
			{
				range[AABB.MIN_Y] = point.y ;
			}
			else if( point.y > range[AABB.MAX_Y] )
			{
				range[AABB.MAX_Y] = point.y ;
			}
		}
	}

	public void set( final float _x1, final float _y1, final float _x2, final float _y2 )
	{
		range[AABB.MIN_X] = ( _x1 < _x2 ) ? _x1 : _x2 ;
		range[AABB.MIN_Y] = ( _y1 < _y2 ) ? _y1 : _y2 ;

		range[AABB.MAX_X] = ( _x1 > _x2 ) ? _x1 : _x2 ;
		range[AABB.MAX_Y] = ( _y1 > _y2 ) ? _y1 : _y2 ;
	}

	public void setMax( final float _x, final float _y )
	{
		FloatBuffer.set( range, AABB.MAX_X, _x, _y ) ;
	}

	public void addToMax( final float _x, final float _y )
	{
		FloatBuffer.add( range, AABB.MAX_X, _x, _y ) ;
	}

	public void setMin( final float _x, final float _y )
	{
		FloatBuffer.set( range, AABB.MIN_X, _x, _y ) ;
	}

	public void addToMin( final float _x, final float _y )
	{
		FloatBuffer.add( range, AABB.MIN_X, _x, _y ) ;
	}

	public AABB copyTo( final AABB _to )
	{
		System.arraycopy( range, 0, _to.range, 0, range.length ) ;
		return _to ;
	}

	public void addTo( final float _x, final float _y )
	{
		// Add x and y to min and max with one call.
		FloatBuffer.add( range, AABB.MIN_X, _x, _y, _x, _y ) ;
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
			if( _point.x < range[AABB.MIN_X] || _point.x > range[AABB.MAX_X] )
			{
				return _intersection ;
			}
		}
		else
		{
			final float ood = 1.0f / _direction.x ;
			float t1 = ( range[AABB.MIN_X] - _point.x ) * ood ;
			float t2 = ( range[AABB.MAX_X] - _point.x ) * ood ;
			
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
			if( _point.y < range[AABB.MIN_Y] || _point.y > range[AABB.MAX_Y] )
			{
				return _intersection ;
			}
		}
		else
		{
			final float ood = 1.0f / _direction.y ;
			float t1 = ( range[AABB.MIN_Y] - _point.y ) * ood ;
			float t2 = ( range[AABB.MAX_Y] - _point.y ) * ood ;
			
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
		final float minX = range[AABB.MIN_X] ;
		final float maxX = range[AABB.MAX_X] ;

		//System.out.println( "X: " + _x + " MIN: " + minX + " MAX: " + maxX ) ;
		if( _x >= minX && _x <= maxX )
		{
			final float minY = range[AABB.MIN_Y] ;
			final float maxY = range[AABB.MAX_Y] ;

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
		final float minX = _aabb.range[AABB.MIN_X] ;
		final float maxX = _aabb.range[AABB.MAX_X] ;

		final float thisMinX = range[AABB.MIN_X] ;
		final float thisMaxX = range[AABB.MAX_X] ;

		if( ( minX >= thisMinX && minX <= thisMaxX ) ||
			( maxX >= thisMinX && maxX <= thisMaxX ) )
		{
			final float minY = _aabb.range[AABB.MIN_Y] ;
			final float maxY = _aabb.range[AABB.MAX_Y] ;

			final float thisMinY = range[AABB.MIN_Y] ;
			final float thisMaxY = range[AABB.MAX_Y] ;

			if( ( minY >= thisMinY && minY <= thisMaxY ) ||
				( maxY >= thisMinY && maxY <= thisMaxY ) )
			{
				return true ;
			}
		}

		return false ;
	}

	public Vector2 getCenter( final Vector2 _center )
	{
		final float centerX = ( range[AABB.MAX_X] + range[AABB.MIN_X] ) * 0.5f ;
		final float centerY = ( range[AABB.MAX_Y] + range[AABB.MIN_Y] ) * 0.5f ;

		_center.x = centerX ;
		_center.y = centerY ;
		return _center ;
	}

	public Vector2 getMin( final Vector2 _min )
	{
		_min.x = range[AABB.MIN_X] ;
		_min.y = range[AABB.MIN_Y] ;
		return _min ;
	}

	public Vector2 getMax( final Vector2 _max )
	{
		_max.x = range[AABB.MAX_X] ;
		_max.y = range[AABB.MAX_Y] ;
		return _max ;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31 ;

		int result = 1 ;
		for( int i = 0; i < range.length; ++i )
		{
			result = prime * result + Float.floatToIntBits( range[i] ) ;
		}

		return result ;
	}

	@Override
	public boolean equals( final Object _obj )
	{
		if( !( _obj instanceof AABB ) )
		{
			return false ;
		}

		final AABB b = ( AABB )_obj ;
		for( int i = 0; i < range.length; ++i )
		{
			if( range[i] != b.range[i] )
			{
				return false ;
			}
		}

		return true ;
	}

	@Override
	public String toString()
	{
		return "MIN: " + range[AABB.MIN_X] + " " + range[AABB.MIN_Y] + "\nMAX: " + range[AABB.MAX_X] + " " + range[AABB.MAX_Y] ;
	}
}
