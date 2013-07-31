package com.linxonline.mallet.maths ;

public final class Vector4
{
	public float x = 0.0f ;
	public float y = 0.0f ;
	public float z = 0.0f ;
	public float w = 0.0f ;

	public Vector4() {}

	public Vector4( final float _x, final float _y, final float _z, final float _w )
	{
		setXYZW( _x, _y, _z, _w ) ;
	}

	public Vector4( final int _x, final int _y, final int _z, final int _w )
	{
		setXYZW( ( float )_x, ( float )_y, ( float )_z, ( float )_w ) ;
	}

	public Vector4( final Vector4 _vec )
	{
		setXYZW( _vec.x, _vec.y, _vec.z, _vec.w ) ;
	}

	public Vector4( final Vector3 _vec )
	{
		setXYZW( _vec.x, _vec.y, _vec.z, 0.0f ) ;
	}

	public void setXYZW( final Vector4 _vec )
	{
		setXYZW( _vec.x, _vec.y, _vec.z, _vec.w ) ;
	}

	public void setXYZW( final float _x, final float _y, final float _z, final float _w )
	{
		x = _x ;
		y = _y ;
		z = _z ;
		w = _w ;
	}

	public final void add( final Vector4 _vec )
	{
		add( _vec.x, _vec.y, _vec.z, _vec.w ) ;
	}

	public final void add( final float _x, final float _y, final float _z, final float _w )
	{
		x += _x ;
		y += _y ;
		z += _z ;
		w += _w ;
	}

	public final void subtract( final Vector4 _vec )
	{
		subtract( _vec.x, _vec.y, _vec.z, _vec.w ) ;
	}

	public final void subtract( final float _x, final float _y, final float _z, final float _w )
	{
		x -= _x ;
		y -= _y ;
		z -= _z ;
		w -= _w ;
	}

	public final void multiply( final Vector4 _vec )
	{
		multiply( _vec.x, _vec.y, _vec.z, _vec.w ) ;
	}

	public final void multiply( final float _scalar )
	{
		multiply( _scalar, _scalar, _scalar, _scalar ) ;
	}

	public final void multiply( final float _x, final float _y, final float _z, final float _w )
	{
		x *= _x ;
		y *= _y ;
		z *= _z ;
		w *= _w ;
	}

	public final float length()
	{
		return ( float )Math.sqrt( ( x * x ) + ( y * y ) + ( z * z ) + ( w * w ) ) ;
	}

	public final void normalise()
	{
		final float length = length() ;
		if( length > 0.0f )
		{
			x /= length ;
			y /= length ;
			z /= length ;
			w /= length ;
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

	public final float getW()
	{
		return w ;
	}
	
	@Override
	public String toString()
	{
		return new String( "X:" + x + " Y: " + y + " Z: " + z + " W: " + w ) ;
	}

	public static final Vector4 add( final Vector4 _vec1, final Vector4 _vec2 )
	{
		return new Vector4( _vec1.x + _vec2.x, _vec1.y + _vec2.y, _vec1.z + _vec2.z, _vec1.w + _vec2.w ) ;
	}

	public static final Vector4 subtract( final Vector4 _vec1, final Vector4 _vec2 )
	{
		return new Vector4( _vec1.x - _vec2.x, _vec1.y - _vec2.y, _vec1.z - _vec2.z, _vec1.w - _vec1.w ) ;
	}

	public static final float distance( final Vector4 _vec1, final Vector4 _vec2 )
	{
		float tmp1 = ( _vec2.x - _vec1.x ) ;
		float tmp2 = ( _vec2.y - _vec1.y ) ;
		float tmp3 = ( _vec2.z - _vec1.z ) ;
		float tmp4 = ( _vec2.w - _vec1.w ) ;

		return ( float )Math.sqrt( ( tmp1 * tmp1 ) + ( tmp2 * tmp2 ) + ( tmp3 * tmp3 ) + ( tmp4 * tmp4 ) ) ;
	}

	public static final float multiply( final Vector4 _vec1, final Vector4 _vec2 )
	{
		return ( ( _vec1.x * _vec2.x ) + ( _vec1.y * _vec2.y ) + ( _vec1.z * _vec2.z ) + ( _vec1.w * _vec2.w ) ) ;
	}

	public static final Vector4 multiply( final Vector4 _vec1, final float _scalar )
	{
		return new Vector4( _vec1.x * _scalar, _vec1.y * _scalar, _vec1.z * _scalar, _vec1.w * _scalar ) ;
	}

	public static final Vector4 divide( final Vector4 _vec1, final Vector4 _vec2 )
	{
		return new Vector4( _vec1.x / _vec2.x, _vec1.y / _vec2.y, _vec1.z / _vec2.z, _vec1.w / _vec2.w ) ;
	}

	public static final Vector4 divide( final Vector4 _vec1, final float _scalar )
	{
		return new Vector4( _vec1.x / _scalar, _vec1.y / _scalar, _vec1.z / _scalar, _vec1.w / _scalar ) ;
	}

	public static final Vector4 parseVector3( final String _text )
	{
		final Vector4 num = new Vector4() ;
		String[] split = _text.split( "," ) ;

		if( split.length >= 4 )
		{
			num.x = Float.parseFloat( split[0] ) ;
			num.y = Float.parseFloat( split[1] ) ;
			num.z = Float.parseFloat( split[2] ) ;
			num.w = Float.parseFloat( split[3] ) ;
		}

		return num ;
	}
}