package com.linxonline.mallet.maths ;

public final class Vector2
{
	public float x = 0.0f ;
	public float y = 0.0f ;

	public Vector2() {}

	public Vector2( final float _x, final float _y )
	{
		setXY( _x, _y ) ;
	}

	public Vector2( final int _x, final int _y )
	{
		setXY( ( float )_x, ( float )_y ) ;
	}

	public Vector2( final Vector2 _vec )
	{
		setXY( _vec ) ;
	}

	public void setXY( final Vector2 _vec )
	{
		setXY( _vec.x, _vec.y ) ;
	}

	public void setXY( final float _x, final float _y )
	{
		x = _x ;
		y = _y ;
	}

	public final void add( final Vector2 _vec )
	{
		add( _vec.x, _vec.y ) ;
	}

	public final void add( final float _x, final float _y )
	{
		x += _x ;
		y += _y ;
	}

	public final void subtract( final Vector2 _vec )
	{
		subtract( _vec.x, _vec.y ) ;
	}

	public final void subtract( final float _x, final float _y )
	{
		x -= _x ;
		y -= _y ;
	}

	public final void multiply( final float _scalar )
	{
		x *= _scalar ;
		y *= _scalar ;
	}

	public final void divide( final float _scalar )
	{
		x /= _scalar ;
		y /= _scalar ;
	}

	public final float length()
	{
		return ( float )Math.sqrt( ( x * x ) + ( y * y ) ) ;
	}

	public final void normalise()
	{
		final float length = length() ;
		if( length > 0.0f )
		{
			x /= length ;
			y /= length ;
		}
	}

	public final float getX()
	{
		return x ;
	}

	public final float getY()
	{
		return y ;
	}

	@Override
	public String toString()
	{
		return new String( "X:" + x + " Y: " + y ) ;
	}

	/**
		Dot Product
	*/
	public static final float dot( final Vector2 _vec1, final Vector2 _vec2 )
	{
		return ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) ;
	}
	
	public static final float distance( final Vector2 _vec1, final Vector2 _vec2 )
	{
		final float tmp1 = ( _vec2.x - _vec1.x ) ;
		final float tmp2 = ( _vec2.y - _vec1.y ) ;

		return ( float )Math.sqrt( ( tmp1 * tmp1 ) + ( tmp2 * tmp2 ) ) ;
	}
	
	public static final Vector2 add( final Vector2 _vec1, final Vector2 _vec2 )
	{
		return new Vector2( _vec1.x + _vec2.x, _vec1.y + _vec2.y ) ;
	}
	
	public static final Vector2 subtract( final Vector2 _vec1, final Vector2 _vec2 )
	{
		return new Vector2( _vec1.x - _vec2.x, _vec1.y - _vec2.y ) ;
	}

	public static final Vector2 multiply( final Vector2 _vec1, final float _scalar )
	{
		return new Vector2( _vec1.x * _scalar, _vec1.y * _scalar ) ;
	}

	/**
		Scalar Product
	*/
	public static final float multiply( final Vector2 _vec1, final Vector2 _vec2 )
	{
		return ( ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) ) ;
	}
	
	/**
		Convience function - used for variables masquerading as vectors but are really 2 scalars.
		Shouldn't be used by anyone sensible. Used by RenderInfo.
	*/
	public static final Vector2 divide( final Vector2 _vec1, final Vector2 _vec2 )
	{
		return new Vector2( _vec1.x / _vec2.x, _vec1.y / _vec2.y ) ;
	}
	
	public static final Vector2 parseVector2( final String _text )
	{
		if( _text == null ) { return null ; }
		final Vector2 num = new Vector2() ;
		String[] split = _text.split( "," ) ;

		if( split.length >= 2 )
		{
			num.x = Float.parseFloat( split[0] ) ;
			num.y = Float.parseFloat( split[1] ) ;
		}

		return num ;
	}
}