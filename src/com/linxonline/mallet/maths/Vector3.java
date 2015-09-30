package com.linxonline.mallet.maths ;

public class Vector3
{
	public float x = 0.0f ;
	public float y = 0.0f ;
	public float z = 0.0f ;

	public Vector3() {}

	public Vector3( final float _x, final float _y, final float _z )
	{
		setXYZ( _x, _y, _z ) ;
	}

	public Vector3( final int _x, final int _y, final int _z )
	{
		setXYZ( ( float )_x, ( float )_y, ( float )_z ) ;
	}

	public Vector3( final Vector3 _vec )
	{
		setXYZ( _vec.x, _vec.y, _vec.z ) ;
	}

	public Vector3( final Vector2 _vec )
	{
		setXYZ( _vec.x, _vec.y, 0.0f ) ;
	}

	public Vector3( final Vector3 _point1, final Vector3 _point2 )
	{
		setXYZ( _point1.x, _point1.y, _point1.z ) ;
		subtract( _point2 ) ;
	}

	public void setXYZ( final Vector3 _vec )
	{
		setXYZ( _vec.x, _vec.y, _vec.z ) ;
	}

	public void setXYZ( final float _x, final float _y, final float _z )
	{
		x = _x ;
		y = _y ;
		z = _z ;
	}

	public final void add( final Vector3 _vec )
	{
		add( _vec.x, _vec.y, _vec.z ) ;
	}

	public final void add( final float _x, final float _y, final float _z )
	{
		x += _x ;
		y += _y ;
		z += _z ;
	}

	public final void subtract( final Vector3 _vec )
	{
		subtract( _vec.x, _vec.y, _vec.z ) ;
	}

	public final void subtract( final float _x, final float _y, final float _z )
	{
		x -= _x ;
		y -= _y ;
		z -= _z ;
	}

	public final void multiply( final float _scalar )
	{
		x *= _scalar ;
		y *= _scalar ;
		z *= _scalar ;
	}

	public final void divide( final float _scalar )
	{
		x /= _scalar ;
		y /= _scalar ;
		z /= _scalar ;
	}

	public final float length()
	{
		return ( float )Math.sqrt( ( x * x ) + ( y * y ) + ( z * z ) ) ;
	}

	/**
		Unit vector
	*/
	public final void normalise()
	{
		final float length = length() ;
		if( length > 0.0f )
		{
			x /= length ;
			y /= length ;
			z /= length ;
		}
	}

	// These should really never be used.
	public final float getX()
	{
		return x ;
	}

	public final float getY()
	{
		return y ;
	}

	public final float getZ()
	{
		return z ;
	}

	public boolean equals( final float _x, final float _y, final float _z )
	{
		return ( Float.compare( x, _x ) == 0 ) && 
			   ( Float.compare( y, _y ) == 0 ) &&
			   ( Float.compare( z, _z ) == 0 ) ;
	}

	@Override
	public boolean equals( Object _compare )
	{
		if( _compare != null )
		{
			if( _compare instanceof Vector3 )
			{
				final Vector3 c = ( Vector3 )_compare ;
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
		result = prime * ( result + Float.floatToIntBits( x ) ) ;
		result = prime * ( result + Float.floatToIntBits( y ) ) ;
		result = prime * ( result + Float.floatToIntBits( z ) ) ;

		return result ;
	}

	@Override
	public String toString()
	{
		return "X:" + x + " Y: " + y + " Z: " + z ;
	}

	public static final Vector3 add( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return new Vector3( _vec1.x + _vec2.x, _vec1.y + _vec2.y, _vec1.z + _vec2.z ) ;
	}

	public static final Vector3 subtract( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return new Vector3( _vec1.x - _vec2.x, _vec1.y - _vec2.y, _vec1.z - _vec2.z ) ;
	}

	/**
		Scalar Product
	*/
	public static final float multiply( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return ( ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) + ( _vec1.z * _vec2.z ) ) ;
	}

	/**
		Dot Product
	*/
	public static final float dot( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) + ( _vec1.z * _vec2.z ) ;
	}

	/**
		Cross Product
		Assumes vector starts at the origin
	*/
	public static final Vector3 cross( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return new Vector3( ( _vec1.y * _vec2.z ) - ( _vec1.z * _vec2.y ),
							( _vec1.z * _vec2.x ) - ( _vec1.x * _vec2.z ),
							( _vec1.x * _vec2.y ) - ( _vec1.y * _vec2.x ) ) ;
	}

	/**
		Calculate the distance between _vec1 and _vec2
	*/
	public static final float distance( final Vector3 _vec1, final Vector3 _vec2 )
	{
		float tmp1 = ( _vec2.x - _vec1.x ) ;
		float tmp2 = ( _vec2.y - _vec1.y ) ;
		float tmp3 = ( _vec2.z - _vec1.z ) ;

		return ( float )Math.sqrt( ( tmp1 * tmp1 ) + ( tmp2 * tmp2 ) + ( tmp3 * tmp3 ) ) ;
	}

	public static final Vector3 parseVector3( final String _text )
	{
		final Vector3 num = new Vector3() ;
		String[] split = _text.split( "," ) ;

		if( split.length >= 3 )
		{
			num.x = Float.parseFloat( split[0] ) ;
			num.y = Float.parseFloat( split[1] ) ;
			num.z = Float.parseFloat( split[2] ) ;
		}

		return num ;
	}
}