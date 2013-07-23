package com.linxonline.mallet.maths ;

public final class Vector3
{
	public float x = 0.0f ;
	public float y = 0.0f ;
	public float z = 0.0f ;

	public Vector3() {}

	public Vector3( final float _x, final float _y, final float _z )
	{
		x = _x ;
		y = _y ;
		z = _z ;
	}

	public Vector3( final int _x, final int _y, final int _z )
	{
		x = ( float )_x ;
		y = ( float )_y ;
		z = ( float )_z ;
	}

	public Vector3( final Vector3 _vec )
	{
		x = _vec.x ;
		y = _vec.y ;
		z = _vec.z ;
	}

	public Vector3( final Vector2 _vec )
	{
		x = _vec.x ;
		y = _vec.y ;
	}

	public void setXYZ( final float _x, final float _y, final float _z )
	{
		x = _x ;
		y = _y ;
		z = _z ;
	}

	public void setXYZ( final Vector3 _vec )
	{
		x = _vec.x ;
		y = _vec.y ;
		z = _vec.z ;
	}

	public final void add( final Vector3 _vec )
	{
		x += _vec.x ;
		y += _vec.y ;
		z += _vec.z ;
	}

	public final void subtract( final Vector3 _vec )
	{
		x -= _vec.x ;
		y -= _vec.y ;
		z -= _vec.z ;
	}

	public static final Vector3 add( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return new Vector3( _vec1.x + _vec2.x, _vec1.y + _vec2.y, _vec1.z + _vec2.z ) ;
	}
	
	public static final Vector3 subtract( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return new Vector3( _vec1.x - _vec2.x, _vec1.y - _vec2.y, _vec1.z - _vec2.z ) ;
	}

	public static final float distance( final Vector3 _vec1, final Vector3 _vec2 )
	{
		float tmp1 = ( _vec2.x - _vec1.x ) ;
		float tmp2 = ( _vec2.y - _vec1.y ) ;
		float tmp3 = ( _vec2.z - _vec1.z ) ;

		return ( float )Math.sqrt( ( tmp1 * tmp1 ) + ( tmp2 * tmp2 ) + ( tmp3 * tmp3 ) ) ;
	}

	public final void multiply( final float _scalar )
	{
		x *= _scalar ;
		y *= _scalar ;
		z *= _scalar ;
	}
	
	public final void multiply( final Vector3 _vec )
	{
		x *= _vec.x ;
		y *= _vec.y ;
		z *= _vec.z ;
	}

	public static final Vector3 multiply( final Vector3 _vec1, final float _scalar )
	{
		return new Vector3( _vec1.x * _scalar, _vec1.y * _scalar, _vec1.z * _scalar ) ;
	}

	public static final float multiply( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return ( ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) + ( _vec1.z * _vec2.z ) ) ;
	}

	public static final Vector3 divide( final Vector3 _vec1, final float _scalar )
	{
		return new Vector3( _vec1.x / _scalar, _vec1.y / _scalar, _vec1.z / _scalar ) ;
	}

	public static final Vector3 divide( final Vector3 _vec1, final Vector3 _vec2 )
	{
		return new Vector3( _vec1.x / _vec2.x, _vec1.y / _vec2.y, _vec1.z / _vec2.z ) ;
	}

	public final float length()
	{
		return ( float )Math.sqrt( ( x * x ) + ( y * y ) + ( z * z ) ) ;
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

	@Override
	public String toString()
	{
		return new String( "X:" + x + " Y: " + y + " Z: " + z ) ;
	}
}