package com.linxonline.mallet.maths ;

public class IntVector3
{
	public int x ;
	public int y ;
	public int z ;

	public IntVector3()
	{
		this( 0.0f, 0.0f, 0.0f ) ;
	}

	public IntVector3( final int _x, final int _y, final int _z )
	{
		setXYZ( _x, _y, _z ) ;
	}

	public IntVector3( final float _x, final float _y, final float _z )
	{
		setXYZ( ( int )_x, ( int )_y, ( int )_z ) ;
	}

	public IntVector3( final Vector3 _vec )
	{
		setXYZ( ( int )_vec.x, ( int )_vec.y, ( int )_vec.z ) ;
	}

	public IntVector3( final IntVector3 _vec )
	{
		setXYZ( _vec.x, _vec.y, _vec.z ) ;
	}

	public IntVector3( final IntVector2 _vec )
	{
		setXYZ( _vec.x, _vec.y, 0 ) ;
	}

	public IntVector3( final IntVector3 _point1, final IntVector3 _point2 )
	{
		setXYZ( _point1.x, _point1.y, _point1.z ) ;
		subtract( _point2 ) ;
	}

	public void setXYZ( final IntVector3 _vec )
	{
		setXYZ( _vec.x, _vec.y, _vec.z ) ;
	}

	public void setXYZ( final int _x, final int _y, final int _z )
	{
		x = _x ;
		y = _y ;
		z = _z ;
	}

	public final void add( final IntVector3 _vec )
	{
		add( _vec.x, _vec.y, _vec.z ) ;
	}

	public final void add( final int _x, final int _y, final int _z )
	{
		x += _x ;
		y += _y ;
		z += _z ;
	}

	public final void subtract( final IntVector3 _vec )
	{
		subtract( _vec.x, _vec.y, _vec.z ) ;
	}

	public final void subtract( final int _x, final int _y, final int _z )
	{
		x -= _x ;
		y -= _y ;
		z -= _z ;
	}

	public final void multiply( final int _scalar )
	{
		x *= _scalar ;
		y *= _scalar ;
		z *= _scalar ;
	}

	public final void divide( final int _scalar )
	{
		x /= _scalar ;
		y /= _scalar ;
		z /= _scalar ;
	}

	public final int length()
	{
		return ( int )Math.sqrt( ( x * x ) + ( y * y ) + ( z * z ) ) ;
	}

	/**
		Unit vector
	*/
	public final void normalise()
	{
		final int length = length() ;
		if( length > 0.0f )
		{
			x /= length ;
			y /= length ;
			z /= length ;
		}
	}

	// These should really never be used.
	public final int getX()
	{
		return x ;
	}

	public final int getY()
	{
		return y ;
	}

	public final int getZ()
	{
		return z ;
	}

	public boolean equals( final int _x, final int _y, final int _z )
	{
		return x == _x && y == _y && z == _z ;
	}

	@Override
	public boolean equals( final Object _compare )
	{
		if( _compare != null )
		{
			if( _compare instanceof IntVector3 )
			{
				final IntVector3 c = ( IntVector3 )_compare ;
				return equals( c.x, c.y, c.z ) ;
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
		result = prime * ( result + z ) ;

		return result ;
	}

	@Override
	public String toString()
	{
		return "X:" + x + " Y: " + y + " Z: " + z ;
	}

	public static final IntVector3 add( final IntVector3 _vec1, final IntVector3 _vec2 )
	{
		return new IntVector3( _vec1.x + _vec2.x, _vec1.y + _vec2.y, _vec1.z + _vec2.z ) ;
	}

	public static final IntVector3 subtract( final IntVector3 _vec1, final IntVector3 _vec2 )
	{
		return new IntVector3( _vec1.x - _vec2.x, _vec1.y - _vec2.y, _vec1.z - _vec2.z ) ;
	}

	public static final IntVector3 multiply( final IntVector3 _vec1, final int _scalar )
	{
		return new IntVector3( _vec1.x * _scalar, _vec1.y * _scalar, _vec1.z * _scalar ) ;
	}

	/**
		Scalar Product
	*/
	public static final int multiply( final IntVector3 _vec1, final IntVector3 _vec2 )
	{
		return ( ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) + ( _vec1.z * _vec2.z ) ) ;
	}

	/**
		Dot Product
	*/
	public static final int dot( final IntVector3 _vec1, final IntVector3 _vec2 )
	{
		return ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) + ( _vec1.z * _vec2.z ) ;
	}

	/**
		Cross Product
		Assumes vector starts at the origin
	*/
	public static final IntVector3 cross( final IntVector3 _vec1, final IntVector3 _vec2 )
	{
		return new IntVector3( ( _vec1.y * _vec2.z ) - ( _vec1.z * _vec2.y ),
							( _vec1.z * _vec2.x ) - ( _vec1.x * _vec2.z ),
							( _vec1.x * _vec2.y ) - ( _vec1.y * _vec2.x ) ) ;
	}

	/**
		Calculate the distance between _vec1 and _vec2
	*/
	public static final int distance( final IntVector3 _vec1, final IntVector3 _vec2 )
	{
		final int tmp1 = ( _vec2.x - _vec1.x ) ;
		final int tmp2 = ( _vec2.y - _vec1.y ) ;
		final int tmp3 = ( _vec2.z - _vec1.z ) ;

		return ( int )Math.sqrt( ( tmp1 * tmp1 ) + ( tmp2 * tmp2 ) + ( tmp3 * tmp3 ) ) ;
	}

	public static final IntVector3 parseIntVector3( final String _text )
	{
		if( _text == null )
		{
			return null ;
		}

		if( _text.isEmpty() == true )
		{
			return null ;
		}

		final IntVector3 num = new IntVector3() ;
		final String[] split = _text.split( "," ) ;

		if( split.length >= 3 )
		{
			num.x = Integer.parseInt( split[0] ) ;
			num.y = Integer.parseInt( split[1] ) ;
			num.z = Integer.parseInt( split[2] ) ;
		}

		return num ;
	}
}
