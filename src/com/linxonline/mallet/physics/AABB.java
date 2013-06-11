package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;

public final class AABB
{
	public final Vector2 position = new Vector2() ;
	public final Vector2 offset = new Vector2() ;

	public final Vector2 min = new Vector2() ;
	public final Vector2 max = new Vector2() ;

	public AABB() {}

	public AABB( final Vector2 _min, final Vector2 _max, 
				 final Vector2 _offset, final Vector2 _pos )
	{
		if( _min != null ) { min.setXY( _min.x, _min.y ) ; }
		if( _max != null ) { max.setXY( _max.x, _max.y ) ; }
		if( _offset != null ) { offset.setXY( _offset.x, _offset.y ) ; }
		if( _pos != null ) { position.setXY( _pos.x, _pos.y ) ; }
	}

	public void setFromOBB( final OBB _obb )
	{
		position.setXY( _obb.position.x, _obb.position.y ) ;
		offset.setXY( _obb.offset.x, _obb.offset.y ) ;
		setDimensionsFromOBB( _obb ) ;
	}

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

	public Vector2 getAbsoluteCenter()
	{
		return new Vector2( position.x + offset.x, position.y + offset.y ) ;
	}

	public String toString()
	{
		return new String( "MIN: " + min + "\nMAX: " + max ) ;
	}
}
