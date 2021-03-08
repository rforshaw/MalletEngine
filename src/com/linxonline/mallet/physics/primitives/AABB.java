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
				 final Vector2 _off )
	{
		this( 0.0f, 0.0f,
			  _length.x, _length.y,
			  _pos.x, _pos.y,
			  _off.x, _off.y ) ;
	}

	public AABB( final Vector2 _min, final Vector2 _max, 
				 final Vector2 _pos, final Vector2 _off )
	{
		this( _min.x, _min.y,
			  _max.x, _max.y,
			  _pos.x, _pos.y,
			  _off.x, _off.y ) ;
	}

	public AABB( final float _maxX, final float _maxY,
				 final float _posX, final float _posY,
				 final float _offX, final float _offY )
	{
		this( 0.0f, 0.0f,
			  _maxX, _maxY,
			  _posX, _posY,
			  _offX, _offY ) ;
	}

	public AABB( final float _minX, final float _minY,
				 final float _maxX, final float _maxY,
				 final float _posX, final float _posY,
				 final float _offX, final float _offY )
	{
		FloatBuffer.set( range, AABB.MIN_X, _minX, _minY ) ;
		FloatBuffer.set( range, AABB.MAX_X, _maxX, _maxY ) ;
		FloatBuffer.set( position, AABB.POSITION_X, _posX, _posY ) ;
		FloatBuffer.set( position, AABB.OFFSET_X, _offX, _offY ) ;
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

	public void setOffset( final float _x, final float _y )
	{
		FloatBuffer.set( position, AABB.OFFSET_X, _x, _y ) ;
	}

	public void addToOffset( final float _x, final float _y )
	{
		FloatBuffer.add( position, AABB.OFFSET_X, _x, _y ) ;
	}

	public void setMax( final float _x, final float _y )
	{
		FloatBuffer.set( range, AABB.MAX_X, _x, _y ) ;
	}

	public void setMin( final float _x, final float _y )
	{
		FloatBuffer.set( range, AABB.MIN_X, _x, _y ) ;
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

		//System.out.println( "X: " + _x + " MIN: " + minX + " MAX: " + maxX ) ;
		if( _x >= minX && _x <= maxX )
		{
			final float minY = y + range[AABB.MIN_Y] ;
			final float maxY = y + range[AABB.MAX_Y] ;

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
		final float x = _aabb.position[AABB.POSITION_X] + _aabb.position[AABB.OFFSET_X] ;
		final float y = _aabb.position[AABB.POSITION_Y] + _aabb.position[AABB.OFFSET_Y] ;

		final float minX = x + _aabb.range[AABB.MIN_X] ;
		final float maxX = x + _aabb.range[AABB.MAX_X] ;

		final float thisX = position[AABB.POSITION_X] + position[AABB.OFFSET_X] ;
		final float thisY = position[AABB.POSITION_Y] + position[AABB.OFFSET_Y] ;

		final float thisMinX = thisX + range[AABB.MIN_X] ;
		final float thisMaxX = thisX + range[AABB.MAX_X] ;

		if( ( minX >= thisMinX && minX <= thisMaxX ) ||
			( maxX >= thisMinX && maxX <= thisMaxX ) )
		{
			final float minY = y + _aabb.range[AABB.MIN_Y] ;
			final float maxY = y + _aabb.range[AABB.MAX_Y] ;
		
			final float thisMinY = thisY + range[AABB.MIN_Y] ;
			final float thisMaxY = thisY + range[AABB.MAX_Y] ;

			if( ( minY >= thisMinY && minY <= thisMaxY ) ||
				( maxY >= thisMinY && maxY <= thisMaxY ) )
			{
				return true ;
			}
		}

		return false ;
	}

	public void getAbsoluteCenter( final Vector2 _center )
	{
		final float centerX = ( range[AABB.MAX_X] + range[AABB.MIN_X] ) * 0.5f ;
		final float centerY = ( range[AABB.MAX_Y] + range[AABB.MIN_Y] ) * 0.5f ;

		_center.x = position[AABB.POSITION_X] + position[AABB.OFFSET_X] + centerX ;
		_center.y = position[AABB.POSITION_Y] + position[AABB.OFFSET_Y] + centerY ;
	}

	public String toString()
	{
		return "POSITION: " + position[POSITION_X] + " " + position[POSITION_Y] + "\nOFFSET: " + position[OFFSET_X] + " " + position[OFFSET_Y] + "\nMIN: " + range[AABB.MIN_X] + " " + range[AABB.MIN_Y] + "\nMAX: " + range[AABB.MAX_X] + " " + range[AABB.MAX_Y] ;
	}
}
