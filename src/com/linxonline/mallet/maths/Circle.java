package com.linxonline.mallet.maths ;

public final class Circle
{
	private final Vector2 temp = new Vector2() ;		// Used in intersectCircle

	public final Vector2 position = new Vector2() ;
	public float radius = 0.0f ;

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

	public Circle() {}

	public void setPosition( final float _x, final float _y )
	{
		position.setXY( _x, _y ) ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		position.add( _x, _y ) ;
	}

	public void setRadius( final float _radius )
	{
		radius = _radius ;
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

	public boolean intersectPoint( final Vector2 _p )
	{
		return intersectPoint( _p.x, _p.y ) ;
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

	public boolean intersectLineSegment( final Vector2 _p, final Vector2 _q )
	{
		return intersectLineSegment( _p.x, _p.y, _q.x, _q.y ) ;
	}

	public boolean intersectLineSegment( final float _px, final float _py, final float _qx, final float _qy )
	{
		final float opx = position.x - _px ;
		final float opy = position.y - _py ;

		final float qpx = _qx - _px ;
		final float qpy = _qy - _py ;

		final float oqx = position.x - _qx ;
		final float oqy = position.y - _qy ;

		final float pqx = _px - _qx ;
		final float pqy = _py - _qy ;

		final float a = ( opx * qpx ) + ( opy * qpy ) ;
		final float b = ( oqx * pqx ) + ( oqy * pqy ) ;

		final float distOP = ( float )Math.sqrt( ( opx * opx ) + ( opy * opy ) ) ;
		final float distOQ = ( float )Math.sqrt( ( oqx * oqx ) + ( oqy * oqy ) ) ;

		final float max = MathUtil.max( distOP, distOQ ) ;
		float min = MathUtil.min( distOP, distOQ ) ;

		//System.out.println( "A: " + a + " B: " + b ) ;

		if( a > 0.0f && b > 0.0f )
		{
			final float distQP = ( float )Math.sqrt( ( qpx * qpx ) + ( qpy * qpy ) ) ;
			
			final float abx = _px - position.x ;
			final float aby = _py - position.y ;

			final float acx = _qx - position.x ;
			final float acy = _qy - position.y ;

			final float area = ( ( abx * acy ) - ( aby * acx ) ) / 2.0f ;
			//final float area = ( ( opx * oqy ) - ( opy * oqx ) ) / 2.0f ;
			//System.out.println( "Area: " + area + " " + distQP ) ;

			min = 2.0f * area / distQP ;
		}

		//System.out.println( "Min: " + min + " Max: " + max ) ;
		return min >= 0.0f && min <= radius && max >= radius ;
	}

	public boolean ray( final Vector2 _origin, final Vector2 _direction )
	{
		return ray( _origin.x, _origin.y, _direction.x, _direction.y ) ;
	}

	public boolean ray( final float _ox, final float _oy, final float _dx, final float _dy )
	{
		final float x = _ox - position.x ;
		final float y = _oy - position.y ;

		final float a = _dx * _dx + _dy * _dy ;
		final float b = 2.0f * ( x * _dx + y * _dy ) ;
		final float c = ( x * x + y * y ) - ( radius * radius ) ;

		final float disc = b * b - 4.0f * a * c ;
		if( disc < 0.0f )
		{
			return false ;
		}

		final float e = ( float )Math.sqrt( disc ) ;
		final float denom = 2.0f * a ;

		final float t1 = ( -b - e ) / denom ;
		if( t1 > MathUtil.EPSILON )
		{
			return true ;
		}

		final float t2 = ( -b + e ) / denom ;
		if( t2 > MathUtil.EPSILON )
		{
			return true ;
		}

		return false ;
	}
}
