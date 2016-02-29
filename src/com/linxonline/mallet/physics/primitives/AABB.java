package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;

public class AABB
{
	private final Vector2 absoluteCenter = new Vector2() ;
	public final Vector2 position = new Vector2() ;
	public final Vector2 offset = new Vector2() ;

	public final Vector2 min = new Vector2() ;
	public final Vector2 max = new Vector2() ;

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
		if( _min != null ) { min.setXY( _min.x, _min.y ) ; }
		if( _max != null ) { max.setXY( _max.x, _max.y ) ; }
		if( _offset != null ) { offset.setXY( _offset.x, _offset.y ) ; }
		if( _pos != null ) { position.setXY( _pos.x, _pos.y ) ; }
	}

	/**
		Calculate the dimensions of the AABB from the OBB.
		Also apply the OBB's position and offset to the AABB.
	*/
	public void setFromOBB( final OBB _obb )
	{
		position.setXY( _obb.position.x, _obb.position.y ) ;
		offset.setXY( _obb.offset.x, _obb.offset.y ) ;
		setDimensionsFromOBB( _obb ) ;
	}

	/**
		Calculate the dimensions of the AABB from the OBB.
		Doesn't apply the OBB's position and offset to the AABB.
	*/
	public void setDimensionsFromOBB( final OBB _obb )
	{
		min.setXY( _obb.points[0].x, _obb.points[0].y ) ;
		max.setXY( _obb.points[0].x, _obb.points[0].y ) ;

		Vector2 point = null ;
		for( int i = 1; i < _obb.points.length; ++i )
		{
			point = _obb.points[i] ;
			if( point.x < min.x )
			{
				min.x = point.x ;
			}
			else if( point.x > max.x )
			{
				max.x = point.x ;
			}

			if( point.y < min.y )
			{
				min.y = point.y ;
			}
			else if( point.y > max.y )
			{
				max.y = point.y ;
			}
		}
	}
	
	public void setPosition( final float _x, final float _y )
	{
		position.setXY( _x, _y ) ;
	}

	public void translate( final float _x, final float _y )
	{
		position.add( _x, _y ) ;
	}

	/**
		Determine if the point defined is located within the AABB.
	*/
	public boolean intersectPoint( final float _x, final float _y )
	{
		final float x = position.x + offset.x ;
		final float y = position.y + offset.y ;
		if( _x >= x + min.x && _x <= x + max.x )
		{
			if( _y >= y + min.y && _y <= y + max.y )
			{
				return true ;
			}
		}
		return false ;
	}

	public boolean intersectAABB( final AABB _aabb )
	{
		final float x = _aabb.position.x + _aabb.offset.x ;
		final float y = _aabb.position.y + _aabb.offset.y ;

		final float minX = x + _aabb.min.x ;
		final float minY = y + _aabb.min.y ;

		final float maxX = x + _aabb.max.x ;
		final float maxY = y + _aabb.max.y ;

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
		final float centerX = ( max.x + min.x ) * 0.5f ;
		final float centerY = ( max.y + min.y ) * 0.5f ;
		_center.setXY( position.x + offset.x + centerX, position.y + offset.y + centerY ) ;
	}

	public String toString()
	{
		return "POSITION: " + position + " MIN: " + min + "\nMAX: " + max ;
	}
}
