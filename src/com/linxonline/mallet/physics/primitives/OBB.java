package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public class OBB
{
	public static final int TOP_LEFT = 0 ;
	public static final int TOP_RIGHT = 2 ;
	public static final int BOTTOM_RIGHT = 4 ;
	public static final int BOTTOM_LEFT = 6 ;

	public static final int POINT_NUM = 4 ;
	public static final int AXES_NUM = 2 ;
	public static final int VECTOR_TYPE = 2 ;

	public static final int POSITION_X = 0 ;
	public static final int POSITION_Y = 1 ;
	public static final int OFFSET_X = 2 ;
	public static final int OFFSET_Y = 3 ;
	public static final int ROTATION = 4 ;

	public float[] position = new float[( 2 * VECTOR_TYPE ) + 1] ;
	public float[] points = new float[POINT_NUM * VECTOR_TYPE] ;
	public float[] rotations = new float[POINT_NUM * VECTOR_TYPE] ;
	public float[] axes = new float[AXES_NUM * VECTOR_TYPE] ;

	public OBB()
	{
		FloatBuffer.set( points, TOP_LEFT, 0.0f, 0.0f ) ;
		FloatBuffer.set( points, TOP_RIGHT, 0.0f, 0.0f ) ;
		FloatBuffer.set( points, BOTTOM_LEFT, 0.0f, 0.0f ) ;
		FloatBuffer.set( points, BOTTOM_RIGHT, 0.0f, 0.0f ) ;
		init() ;
	}

	public OBB( final AABB _aabb )
	{
		FloatBuffer.set( points,   OBB.TOP_LEFT,     _aabb.range[AABB.MIN_X],         _aabb.range[AABB.MIN_Y] ) ;
		FloatBuffer.set( points,   OBB.TOP_RIGHT,    _aabb.range[AABB.MAX_X],         _aabb.range[AABB.MIN_Y] ) ;
		FloatBuffer.set( points,   OBB.BOTTOM_LEFT,  _aabb.range[AABB.MIN_X],         _aabb.range[AABB.MAX_Y] ) ;
		FloatBuffer.set( points,   OBB.BOTTOM_RIGHT, _aabb.range[AABB.MAX_X],         _aabb.range[AABB.MAX_Y] ) ;
		FloatBuffer.set( position, OBB.POSITION_X,   _aabb.position[AABB.POSITION_X], _aabb.position[AABB.POSITION_Y] ) ;
		FloatBuffer.set( position, OBB.OFFSET_X,     _aabb.position[AABB.OFFSET_X],   _aabb.position[AABB.OFFSET_Y] ) ;
		init() ;
	}

	public OBB( final Vector2 _topLeft,
				final Vector2 _topRight, 
				final Vector2 _bottomLeft,
				final Vector2 _bottomRight )
	{
		FloatBuffer.set( points, OBB.TOP_LEFT, _topLeft.x, _topLeft.y ) ;
		FloatBuffer.set( points, OBB.TOP_RIGHT, _topRight.x, _topRight.y ) ;
		FloatBuffer.set( points, OBB.BOTTOM_LEFT, _bottomLeft.x, _bottomLeft.y ) ;
		FloatBuffer.set( points, OBB.BOTTOM_RIGHT, _bottomRight.x, _bottomRight.y ) ;
		init() ;
	}

	public void setPosition( final float _x, final float _y )
	{
		FloatBuffer.set( position, OBB.POSITION_X, _x, _y ) ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		FloatBuffer.add( position, OBB.POSITION_X, _x, _y ) ;
	}

	public void translate( final float _x, final float _y )
	{
		FloatBuffer.add( position, OBB.POSITION_X, _x, _y ) ;
	}

	public void setOffset( final float _x, final float _y )
	{
		FloatBuffer.set( position, OBB.OFFSET_X, _x, _y ) ;
	}

	public void addToOffset( final float _x, final float _y )
	{
		FloatBuffer.add( position, OBB.OFFSET_X, _x, _y ) ;
	}

	/**
		Set the points and axes from the OBB being passed in.
		This also includes the position & offset.
	**/
	public void setFromOBB( final OBB _obb )
	{
		FloatBuffer.copy( _obb.position, position ) ;
		setDimensionsFromOBB( _obb ) ;
	}

	/**
		Set the points and axes from the OBB being passed in.
		Keep the original position & offset.
	**/
	public void setDimensionsFromOBB( final OBB _obb )
	{
		FloatBuffer.copy( points, _obb.points ) ;
		FloatBuffer.copy( axes, _obb.axes ) ;
	}

	/**
		Get the points absolute position, this includes the offset 
		and position of the OBB.
		returns a new Vector2().
	**/
	public Vector2 getAbsolutePoint( final int _index )
	{
		final Vector2 result = FloatBuffer.fill( points, new Vector2(), _index ) ;
		result.add( position[OBB.POSITION_X] + position[OBB.OFFSET_X],
					position[OBB.POSITION_Y] + position[OBB.OFFSET_Y] ) ;
		return result ;
	}

	public Vector2 getPosition( final Vector2 _fill )
	{
		_fill.setXY( position[OBB.POSITION_X], position[OBB.POSITION_Y] ) ;
		return _fill ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		_fill.x = position[OBB.POSITION_X] ;
		_fill.y = position[OBB.POSITION_Y] ;
		return _fill ;
	}

	public Vector2 getOffset( final Vector2 _fill )
	{
		_fill.setXY( position[OBB.OFFSET_X], position[OBB.OFFSET_Y] ) ;
		return _fill ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		_fill.x = position[OBB.OFFSET_X] ;
		_fill.y = position[OBB.OFFSET_Y] ;
		return _fill ;
	}

	public float getRotation()
	{
		return position[OBB.ROTATION] ;
	}
	
	public Vector2 getCenter( final Vector2 _center )
	{
		_center.setXY( position[OBB.POSITION_X], position[OBB.POSITION_Y] ) ;
		_center.add( position[OBB.OFFSET_X], position[OBB.OFFSET_Y] ) ;
		return _center ;
	}

	public Vector2 getCenter()
	{
		return getCenter( new Vector2() ) ;
	}

	/**
		NEEDS TO BE REIMPLEMENTED
	**/
	public final void setRotation( final float _theta )
	{
		position[OBB.ROTATION] = _theta ;
		applyRotations() ;
	}

	private void applyRotations()
	{
		final float sin = ( float )Math.sin( position[OBB.ROTATION] ) ;
		final float cos = ( float )Math.cos( position[OBB.ROTATION] ) ;

		final Vector2 point = new Vector2() ;
		final Vector2 offset = new Vector2( position[OBB.OFFSET_X], position[OBB.OFFSET_Y] ) ;

		for( int i = 0; i < points.length; i += 2 )
		{
			FloatBuffer.fill( points, point, i ) ;
			OBB.rotate( point, offset, sin, cos ) ;
			FloatBuffer.set( rotations, i, point.x, point.y ) ;
		}
	}
	
	public static Vector2 rotate( final Vector2 _point, final Vector2 _offset, final float _sin, final float _cos )
	{
		_point.x += _offset.x ;
		_point.y += _offset.y ;

		final float x = ( _point.x * _cos ) - ( _point.y * _sin ) ;
		final float y = ( _point.x * _sin ) + ( _point.y * _cos ) ;

		_point.x = x - _offset.x ;
		_point.y = y - _offset.y ;
		return _point ;
	}

	public final void updateAxesAndEdges()
	{
		//final float sin = ( float )Math.sin( position[OBB.ROTATION] ) ;
		//final float cos = ( float )Math.cos( position[OBB.ROTATION] ) ;

		final float topRightX = rotations[OBB.TOP_RIGHT] ;
		final float topRightY = rotations[OBB.TOP_RIGHT + 1] ;

		final float topLeftX = rotations[OBB.TOP_LEFT] ;
		final float topLeftY = rotations[OBB.TOP_LEFT + 1] ;

		final float bottomRightX = rotations[OBB.BOTTOM_RIGHT] ;
		final float bottomRightY = rotations[OBB.BOTTOM_RIGHT + 1] ;

		axes[1] = topRightX - topLeftX ;			// x
		axes[0] = -( topRightY - topLeftY ) ;		// y

		float length = Vector2.length( axes[0], axes[1] ) ;
		axes[0] /= length ;
		axes[1] /= length ;

		axes[3] = topRightX - bottomRightX ;		// x
		axes[2] = -( topRightY - bottomRightY ) ;	// y

		length = Vector2.length( axes[2], axes[3] ) ;
		axes[2] /= length ;
		axes[3] /= length ;
	}

	private final void init()
	{
		applyRotations() ;
		updateAxesAndEdges() ;
	}
}
