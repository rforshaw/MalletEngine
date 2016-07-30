package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class OBB
{
	public static final int TOP_LEFT = 0 ;
	public static final int TOP_RIGHT = 1 ;
	public static final int BOTTOM_LEFT = 2 ;
	public static final int BOTTOM_RIGHT = 3 ;

	public float rotation = 0.0f ;
	public Vector2 position = new Vector2() ;
	public Vector2 offset = new Vector2() ;

	public Vector2[] points = new Vector2[4] ;
	public Vector2[] axes = new Vector2[2] ;

	public OBB()
	{
		points[TOP_LEFT] = new Vector2() ;
		points[TOP_RIGHT] = new Vector2() ;
		points[BOTTOM_LEFT] = new Vector2() ;
		points[BOTTOM_RIGHT] = new Vector2() ;
		init() ;
	}

	public OBB( final AABB _aabb )
	{
		points[TOP_LEFT] = new Vector2( _aabb.min.x, _aabb.min.y ) ;
		points[TOP_RIGHT] = new Vector2( _aabb.max.x, _aabb.min.y ) ;
		points[BOTTOM_LEFT] = new Vector2( _aabb.min.x, _aabb.max.y ) ;
		points[BOTTOM_RIGHT] = new Vector2( _aabb.max.x, _aabb.max.y ) ;
		offset.setXY( _aabb.offset ) ;
		init() ;
	}

	public OBB( final Vector2 _topLeft, final Vector2 _topRight, 
				final Vector2 _bottomLeft, final Vector2 _bottomRight )
	{
		points[TOP_LEFT] = _topLeft ;
		points[TOP_RIGHT] = _topRight ;
		points[BOTTOM_LEFT] = _bottomLeft ;
		points[BOTTOM_RIGHT] = _bottomRight ;
		init() ;
	}

	public void setPosition( final float _x, final float _y )
	{
		position.setXY( _x, _y ) ;
	}
	
	public void translate( final float _x, final float _y )
	{
		position.add( _x, _y ) ;
	}

	public void setOffset( final float _x, final float _y )
	{
		offset.setXY( _x, _y ) ;
	}

	/**
		Set the points and axes from the OBB being passed in.
		This also includes the position & offset.
	**/
	public void setFromOBB( final OBB _obb )
	{
		position.setXY( _obb.position.x, _obb.position.y ) ;
		offset.setXY( _obb.offset.x, _obb.offset.y ) ;
		for( int i = 0; i < points.length; ++i )
		{
			points[i].setXY( _obb.points[i].x, _obb.points[i].y ) ;
			axes[i].setXY( _obb.axes[i].x, _obb.axes[i].y ) ;
		}
	}

	/**
		Set the points and axes from the OBB being passed in.
		Keep the original position & offset.
	**/
	public void setDimensionsFromOBB( final OBB _obb )
	{
		for( int i = 0; i < points.length; ++i )
		{
			points[i].setXY( _obb.points[i].x, _obb.points[i].y ) ;
			axes[i].setXY( _obb.axes[i].x, _obb.axes[i].y ) ;
		}
	}

	/**
		Get the points absolute position, this includes the offset 
		and position of the OBB.
		returns a new Vector2().
	**/
	public Vector2 getAbsolutePoint( final int _index )
	{
		final float x = position.x + offset.x + points[_index].x ;
		final float y = position.y + offset.y + points[_index].y ;
		return new Vector2( x, y ) ;
	}

	public Vector2 getCenter( final Vector2 _center )
	{
		_center.setXY( position ) ;
		_center.add( offset ) ;
		return _center ;
	}

	public Vector2 getCenter()
	{
		return getCenter( new Vector2() ) ;//Vector2.add( position, offset ) ;
	}

	/**
		NEEDS TO BE REIMPLEMENTED
	**/
	public final void setRotation( final float _theta )
	{
		final float diff = _theta - rotation ;
		final float cosAngle = ( float )Math.cos( diff ) ;
		final float sinAngle = ( float )Math.sin( diff ) ;
		rotation = _theta ;

		Vector2 point = null ;
		for( int i = 0; i < points.length; ++i )
		{
			point = points[i] ;
			final float x = point.x + offset.x ;
			final float y = point.y + offset.y ;
			
			final float x2 = ( x * cosAngle ) - ( y * sinAngle ) - offset.x ;
			final float y2 = ( y * cosAngle ) + ( x * sinAngle ) - offset.y ;
			point.setXY( x2, y2 ) ;
		}
	}

	public final void updateAxesAndEdges()
	{
		axes[0].y = points[TOP_RIGHT].x - points[TOP_LEFT].x ;
		axes[0].x = -( points[TOP_RIGHT].y - points[TOP_LEFT].y ) ;
		axes[0].normalise() ;

		axes[1].y = points[TOP_RIGHT].x - points[BOTTOM_RIGHT].x ;
		axes[1].x = -( points[TOP_RIGHT].y - points[BOTTOM_RIGHT].y ) ;
		axes[1].normalise() ;
	}

	private final void init()
	{
		Vector2 point = new Vector2() ;

		point.x = points[TOP_RIGHT].x - points[TOP_LEFT].x ;
		point.y = points[TOP_RIGHT].y - points[TOP_LEFT].y ;
		axes[0] = new Vector2( point.y, -point.x ) ;
		axes[0].normalise() ;

		point.x = points[TOP_RIGHT].x - points[BOTTOM_RIGHT].x ;
		point.y = points[TOP_RIGHT].y - points[BOTTOM_RIGHT].y ;
		axes[1] = new Vector2( point.y, -point.x ) ;
		axes[1].normalise() ;
	}
}