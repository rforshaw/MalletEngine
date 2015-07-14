package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;

public class Circle
{
	private final Vector2 temp = new Vector2() ;		// Used in intersectCircle

	public final Vector2 position = new Vector2() ;
	public final Vector2 offset = new Vector2() ;
	public final float radius ;

	public Circle( final Vector2 _pos, final Vector2 _offset, final float _radius )
	{
		position.setXY( _pos ) ;
		radius = _radius ;
	}
	
	public void setPosition( final float _x, final float _y )
	{
		position.setXY( _x, _y ) ;
	}

	public void setRotation( final float _theta ) {}

	public boolean intersectCircle( final Circle _circle )
	{
		temp.x = Math.abs( ( position.x + offset.x ) - ( _circle.position.x + _circle.offset.x ) ) ;
		temp.y = Math.abs( ( position.y + offset.y ) - ( _circle.position.x + _circle.offset.x ) ) ;
		if( temp.length() <= ( _circle.radius + radius ) )
		{
			return true ;
		}

		return false ;
	}

	public boolean intersectPoint( final float _x, final float _y )
	{
		temp.x = position.x + offset.x ;
		temp.y = position.y + offset.y ;

		if( ( _x <= temp.x + radius ) && ( _x >= temp.x - radius ) )
		{
			if( ( _y <= temp.y + radius ) && ( _y >= temp.y - radius ) )
			{
				return true ;
			}
		}
		return false ;
	}
}