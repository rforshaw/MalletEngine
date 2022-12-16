package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;
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
	public float[] range = new float[RANGE_NUM * VECTOR_TYPE] ;

	public AABB() {}

	public AABB( final Vector2 _length )
	{
		this( 0.0f, 0.0f, _length.x, _length.y ) ;
	}

	public AABB( final Vector2 _min, final Vector2 _max )
	{
		this( _min.x, _min.y, _max.x, _max.y ) ;
	}

	public AABB( final float _maxX, final float _maxY )
	{
		this( 0.0f, 0.0f, _maxX, _maxY ) ;
	}

	public AABB( final float _minX, final float _minY,
				 final float _maxX, final float _maxY )
	{
		FloatBuffer.set( range, AABB.MIN_X, _minX, _minY ) ;
		FloatBuffer.set( range, AABB.MAX_X, _maxX, _maxY ) ;
	}

	public AABB( final OBB _obb )
	{
		setFromOBB( _obb ) ;
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

	public static AABB create( final Vector2 _area, final Vector2 _position )
	{
		return new AABB( _position.x, _position.y,
						 _position.x + _area.x, _position.y + _area.y ) ;
	}

	public String toString()
	{
		return "MIN: " + range[AABB.MIN_X] + " " + range[AABB.MIN_Y] + "\nMAX: " + range[AABB.MAX_X] + " " + range[AABB.MAX_Y] ;
	}
}
