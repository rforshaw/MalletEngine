package com.linxonline.mallet.maths ;

public final class Circle
{
	private final Vector2 temp = new Vector2() ;		// Used in intersectCircle

	public final Vector2 position = new Vector2() ;
	public final float radius ;

	public Circle( final Vector2 _pos, final float _radius )
	{
		position.setXY( _pos ) ;
		radius = _radius ;
	}
	
	public Circle( final float _x, final float _y, final float _radius )
	{
		position.setXY( _x, _y ) ;
		radius = _radius ;
	}
	
	public void setPosition( final float _x, final float _y )
	{
		position.setXY( _x, _y ) ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		position.add( _x, _y ) ;
	}

	public boolean intersectCircle( final Circle _circle )
	{
		temp.x = Math.abs( position.x - _circle.position.x ) ;
		temp.y = Math.abs( position.y - _circle.position.y ) ;
		if( temp.length() <= ( _circle.radius + radius ) )
		{
			return true ;
		}

		return false ;
	}

	public boolean intersectPoint( final float _x, final float _y )
	{
		if( ( _x <= position.x + radius ) && ( _x >= position.x - radius ) )
		{
			if( ( _y <= position.y + radius ) && ( _y >= position.y - radius ) )
			{
				return true ;
			}
		}
		return false ;
	}

	public boolean intersectLine( final Vector2 _a, final Vector2 _b )
	{
		final float x = _a.x - position.x ;
		final float y = _a.y - position.y ;

		final float dx = _b.x - _a.x ;
		final float dy = _b.y - _a.y ;

		final float a = dx * dx + dy * dy ;
		final float b = 2.0f * ( x * dx + y * dy ) ;
		final float c = ( x * x + y * y ) - ( radius * radius ) ;

		final float disc = ( b * b - 4.0f * a * c ) ;
		if( disc < 0.0f )
		{
			return false ;
		}

		final float e = ( float )Math.sqrt( disc ) ;
		final float denom = 2.0f * a ;

		float t = ( -b - e ) / denom ;
		if( t > MathUtil.EPSILON )
		{
			return true ;
		}

		t = ( -b + e ) / denom ;
		if( t > MathUtil.EPSILON )
		{
			return true ;
		}

		return false ;
	}
}
