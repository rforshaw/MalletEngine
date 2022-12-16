package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class OBB
{
	public static final int TOP_LEFT = 0 ;
	public static final int TOP_RIGHT = 2 ;
	public static final int BOTTOM_RIGHT = 4 ;
	public static final int BOTTOM_LEFT = 6 ;

	public static final int POINT_NUM = 4 ;
	public static final int AXES_NUM = 2 ;
	public static final int VECTOR_TYPE = 2 ;

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
		setFromAABB( _aabb ) ;
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

	public void setFromAABB( final AABB _aabb )
	{
		FloatBuffer.set( points,   OBB.TOP_LEFT,     _aabb.range[AABB.MIN_X],         _aabb.range[AABB.MIN_Y] ) ;
		FloatBuffer.set( points,   OBB.TOP_RIGHT,    _aabb.range[AABB.MAX_X],         _aabb.range[AABB.MIN_Y] ) ;
		FloatBuffer.set( points,   OBB.BOTTOM_LEFT,  _aabb.range[AABB.MIN_X],         _aabb.range[AABB.MAX_Y] ) ;
		FloatBuffer.set( points,   OBB.BOTTOM_RIGHT, _aabb.range[AABB.MAX_X],         _aabb.range[AABB.MAX_Y] ) ;
		init() ;
	}

	/**
		Set the points and axes from the OBB being passed in.
	**/
	public void setFromOBB( final OBB _obb )
	{
		FloatBuffer.copy( points, _obb.points ) ;
		FloatBuffer.copy( axes, _obb.axes ) ;
	}

	/**
		Get the points absolute position, this includes the offset 
		and position of the OBB.
		returns a new Vector2().
	**/
	public Vector2 getPoint( final int _index, final Vector2 _fill )
	{
		FloatBuffer.fill( rotations, _fill, _index ) ;
		return _fill ;
	}

	/**
		NEEDS TO BE REIMPLEMENTED
	**/
	public final void setRotation( final float _theta, final float _offsetX, final float _offsetY )
	{
		applyRotations( _theta, _offsetX, _offsetY ) ;
	}

	private void applyRotations( final float _theta, final float _offsetX, final float _offsetY )
	{
		final float sin = ( float )Math.sin( _theta ) ;
		final float cos = ( float )Math.cos( _theta ) ;

		final Vector2 point = new Vector2() ;
		final Vector2 offset = new Vector2( _offsetX, _offsetY ) ;

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
		applyRotations( 0.0f, 0.0f, 0.0f ) ;
		updateAxesAndEdges() ;
	}
}
