package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class AABB
{
	public static final int RANGE_NUM = 2 ;
	public static final int VECTOR_TYPE = 2 ;

	public static final int MIN_X = 0 ;
	public static final int MIN_Y = 1 ;
	public static final int MAX_X = 2 ;
	public static final int MAX_Y = 3 ;

	public static final int POSITION_X = 0 ;
	public static final int POSITION_Y = 1 ;
	public static final int OFFSET_X = 2 ;
	public static final int OFFSET_Y = 3 ;

	public float[] position = new float[2 * VECTOR_TYPE] ;

	// min then max
	public float[] range = new float[RANGE_NUM * VECTOR_TYPE] ;

	public AABB() {}

	public AABB( final Vector2 _length,
				 final Vector2 _pos,
				 final Vector2 _offset )
	{
		this( new Vector2(), _length, _pos, _offset ) ;
	}

	public AABB( final Vector2 _min, final Vector2 _max, 
				 final Vector2 _pos, final Vector2 _offset )
	{
		if( _min != null ) { FloatBuffer.set( range, AABB.MIN_X, _min.x, _min.y ) ; }
		if( _max != null ) { FloatBuffer.set( range, AABB.MAX_X, _max.x, _max.y ) ; }
		if( _pos != null ) { FloatBuffer.set( position, AABB.POSITION_X, _pos.x, _pos.y ) ; }
		if( _offset != null ) { FloatBuffer.set( position, AABB.OFFSET_X, _offset.x, _offset.y ) ; }
	}

	/**
		Calculate the dimensions of the AABB from the OBB.
		Also apply the OBB's position and offset to the AABB.
	*/
	public void setFromOBB( final OBB _obb )
	{
		FloatBuffer.copy( _obb.position, position ) ;
		setDimensionsFromOBB( _obb ) ;
	}

	/**
		Calculate the dimensions of the AABB from the OBB.
		Doesn't apply the OBB's position and offset to the AABB.
	*/
	public void setDimensionsFromOBB( final OBB _obb )
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
	
	public void setPosition( final float _x, final float _y )
	{
		FloatBuffer.set( position, AABB.POSITION_X, _x, _y ) ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		FloatBuffer.add( position, AABB.POSITION_X, _x, _y ) ;
	}

	/**
		Determine if the point defined is located within the AABB.
	*/
	public boolean intersectPoint( final float _x, final float _y )
	{
		final float x = position[AABB.POSITION_X] + position[AABB.OFFSET_X] ;
		final float y = position[AABB.POSITION_Y] + position[AABB.OFFSET_Y] ;

		final float minX = x + range[AABB.MIN_X] ;
		final float maxX = x + range[AABB.MAX_X] ;

		if( _x >= minX && _x <= maxX )
		{
			final float minY = y + range[AABB.MIN_Y] ;
			final float maxY = y + range[AABB.MAX_Y] ;

			if( _y >= minY && _y <= maxY )
			{
				return true ;
			}
		}
		return false ;
	}

	public boolean intersectAABB( final AABB _aabb )
	{
		final float x = _aabb.position[AABB.POSITION_X] + _aabb.position[AABB.OFFSET_X] ;
		final float y = _aabb.position[AABB.POSITION_Y] + _aabb.position[AABB.OFFSET_Y] ;

		final float minX = x + _aabb.range[AABB.MIN_X] ;
		final float minY = y + _aabb.range[AABB.MIN_Y] ;

		final float maxX = x + _aabb.range[AABB.MAX_X] ;
		final float maxY = y + _aabb.range[AABB.MAX_Y] ;

		if( intersectPoint( minX, minY ) == true )
		{
			return true ;
		}
		else if( intersectPoint( maxX, maxY ) == true )
		{
			return true ;
		}
		else if( intersectPoint( minX, maxY ) == true )
		{
			return true ;
		}
		else if( intersectPoint( maxX, minY ) == true )
		{
			return true ;
		}

		return false ;
	}

	public void getAbsoluteCenter( final Vector2 _center )
	{
		final float centerX = ( range[AABB.MAX_X] + range[AABB.MIN_X] ) * 0.5f ;
		final float centerY = ( range[AABB.MAX_Y] + range[AABB.MIN_Y] ) * 0.5f ;
		_center.setXY( position[AABB.POSITION_X] + position[AABB.OFFSET_X] + centerX, 
					   position[AABB.POSITION_Y] + position[AABB.OFFSET_Y] + centerY ) ;
	}

	public String toString()
	{
		return "POSITION: " + position + " MIN: " + range[AABB.MIN_X] + " " + range[AABB.MIN_Y] + "\nMAX: " + range[AABB.MAX_X] + " " + range[AABB.MAX_Y] ;
	}
}
