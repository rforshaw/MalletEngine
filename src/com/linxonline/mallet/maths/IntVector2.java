package com.linxonline.mallet.maths ;

import com.linxonline.mallet.renderer.IUniform ;

public final class IntVector2 implements IUniform
{
	public int x ;
	public int y ;

	public IntVector2()
	{
		this( 0.0f, 0.0f ) ;
	}

	public IntVector2( final int _x, final int _y )
	{
		setXY( _x, _y ) ;
	}

	public IntVector2( final float _x, final float _y )
	{
		setXY( ( int )_x, ( int )_y ) ;
	}

	public IntVector2( final Vector2 _vec )
	{
		setXY( ( int )_vec.x, ( int )_vec.y ) ;
	}
	
	public IntVector2( final IntVector2 _vec )
	{
		setXY( _vec ) ;
	}

	public IntVector2( final IntVector2 _point1, final IntVector2 _point2 )
	{
		setXY( _point1 ) ;
		subtract( _point2 ) ;
	}

	public void setXY( final IntVector2 _vec )
	{
		setXY( _vec.x, _vec.y ) ;
	}

	public void setXY( final int _x, final int _y )
	{
		x = _x ;
		y = _y ;
	}

	public final void add( final IntVector2 _vec )
	{
		add( _vec.x, _vec.y ) ;
	}

	public final void add( final int _x, final int _y )
	{
		x += _x ;
		y += _y ;
	}

	public final void subtract( final IntVector2 _vec )
	{
		subtract( _vec.x, _vec.y ) ;
	}

	public final void subtract( final int _x, final int _y )
	{
		x -= _x ;
		y -= _y ;
	}

	public final void multiply( final int _scalar )
	{
		x *= _scalar ;
		y *= _scalar ;
	}

	public final void divide( final int _scalar )
	{
		x /= _scalar ;
		y /= _scalar ;
	}

	public final int length()
	{
		return ( int )Math.sqrt( ( x * x ) + ( y * y ) ) ;
	}

	public final void normalise()
	{
		final int length = length() ;
		if( length > 0.0f )
		{
			x /= length ;
			y /= length ;
		}
	}

	public final int getX()
	{
		return x ;
	}

	public final int getY()
	{
		return y ;
	}

	public boolean equals( final int _x, final int _y )
	{
		return x == _x && y == _y ;
	}

	@Override
	public boolean equals( final Object _compare )
	{
		if( _compare != null )
		{
			if( _compare instanceof IntVector2 )
			{
				final IntVector2 c = ( IntVector2 )_compare ;
				return equals( c.x, c.y ) ;
			}
		}

		return false ;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31 ;
		int result = 1 ;
		result = prime * ( result + x ) ;
		result = prime * ( result + y ) ;

		return result ;
	}

	@Override
	public String toString()
	{
		return "X:" + x + " Y: " + y ;
	}

	/**
		Dot Product
	*/
	public static final int dot( final IntVector2 _vec1, final IntVector2 _vec2 )
	{
		return ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) ;
	}
	
	public static final int distance( final IntVector2 _vec1, final IntVector2 _vec2 )
	{
		final int tmp1 = ( _vec2.x - _vec1.x ) ;
		final int tmp2 = ( _vec2.y - _vec1.y ) ;

		return ( int )Math.sqrt( ( tmp1 * tmp1 ) + ( tmp2 * tmp2 ) ) ;
	}
	
	public static final IntVector2 add( final IntVector2 _vec1, final IntVector2 _vec2 )
	{
		return new IntVector2( _vec1.x + _vec2.x, _vec1.y + _vec2.y ) ;
	}
	
	public static final IntVector2 subtract( final IntVector2 _vec1, final IntVector2 _vec2 )
	{
		return new IntVector2( _vec1.x - _vec2.x, _vec1.y - _vec2.y ) ;
	}

	public static final IntVector2 multiply( final IntVector2 _vec1, final int _scalar )
	{
		return new IntVector2( _vec1.x * _scalar, _vec1.y * _scalar ) ;
	}

	/**
		Scalar Product
	*/
	public static final int multiply( final IntVector2 _vec1, final IntVector2 _vec2 )
	{
		return ( ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) ) ;
	}
	
	public static final int cross( final IntVector2 _vec1, final IntVector2 _vec2 )
	{
		return ( _vec1.x * _vec2.y ) - ( _vec1.y * _vec2.x ) ;
	}

	public static final IntVector2 parseVector2( final String _text )
	{
		if( _text == null ) { return null ; }
		final IntVector2 num = new IntVector2() ;
		final String[] split = _text.split( "," ) ;

		if( split.length >= 2 )
		{
			num.x = Integer.parseInt( split[0] ) ;
			num.y = Integer.parseInt( split[1] ) ;
		}

		return num ;
	}
}
