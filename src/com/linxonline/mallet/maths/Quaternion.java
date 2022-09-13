package com.linxonline.mallet.maths ;

public final class Quaternion
{
	public float x ;
	public float y ;
	public float z ;
	public float w ;

	public Quaternion()
	{
		this( 0.0f, 0.0f, 0.0f, 0.0f ) ;
	}

	public Quaternion( final Quaternion _quat )
	{
		setXYZW( _quat.x, _quat.y, _quat.z, _quat.w ) ;
	}

	public Quaternion( final float _x, final float _y, final float _z, final float _w )
	{
		setXYZW( _x, _y, _z, _w ) ;
	}

	public Quaternion( final Vector3 _vec, final float _w )
	{
		setXYZW( _vec, _w ) ;
	}

	public final void setXYZW( final Vector3 _vec, final float _w )
	{
		setXYZW( _vec.x, _vec.y, _vec.z, _w ) ;
	}

	public final void setXYZW( final float _x, final float _y, final float _z, final float _w )
	{
		x = _x ;
		y = _y ;
		z = _z ;
		w = _w ;
	}

	public void multiply( final Quaternion _quat )
	{
		multiply( _quat.x, _quat.y, _quat.z, _quat.w ) ;
	}

	/**
		Non-commutative - ( Q1 * Q2 ) is not the same as ( Q2 * Q1 ).
		Quaternions have a bad habbit of loosing cohesion. Should be used sparringly.
	*/
	public void multiply( final float _x, final float _y, final float _z, final float _w )
	{
		final float w1 = ( w * _w ) - ( x * _x ) - ( y * _y ) - ( z * _z ) ;
		final float x1 = ( w * _x ) + ( x * _w ) + ( y * _z ) - ( z * _y ) ;
		final float y1 = ( w * _y ) - ( x * _z ) + ( y * _w ) + ( z * _x ) ;
		final float z1 = ( w * _z ) + ( x * _y ) - ( y * _x ) + ( z * _w ) ;
		w = w1 ; x = x1; y = y1 ; z = z1 ;
	}

	public float length()
	{
		return ( float )Math.sqrt( ( x * x ) + ( y * y ) + ( z * z ) + ( w * w ) ) ;
	}

	public void normalise()
	{
		final float length = length() ;
		if( length > 0 )
		{
			x /= length ;
			y /= length ;
			z /= length ;
			w /= length ;
		}
	}
	
	public final float getW() { return w ; }

	public final static Quaternion genRotation( final float _theta, final Quaternion _quat )
	{
		return genRotation( _theta, _quat.x, _quat.y, _quat.z ) ;
	}

	public final static Quaternion genRotation( final float _theta, final Vector3 _axis )
	{
		return genRotation( _theta, _axis.x, _axis.y, _axis.z ) ;
	}

	/**
		Generates a local rotation quaternion, which can be applied to a Vector3 or Quaternion.
	*/
	public final static Quaternion genRotation( final float _theta, final float _x, final float _y, final float _z )
	{
		final float cos = ( float )Math.cos( _theta / 2.0f ) ;
		final float sin = ( float )Math.sin( _theta / 2.0f ) ;
		final Quaternion quat = new Quaternion( _x * sin, _y * sin, _z * sin, cos ) ;
		quat.normalise() ;			// Ensure is unit quaternion
		return quat ;
	}

	/**
		Convience function - same as quat.multiply( rotate ), provides clearer definition.
		Quaternion.applyRotation( quat, rotate )
	*/
	public final static void applyRotation( final Quaternion _axis, final Quaternion _rotate )
	{
		_axis.multiply( _rotate ) ;
	}

	/**
		Rotate a Vector3 using the provided rotation quaternion.
		Convience method - reduces object creation, converting V3 to Quat then back.
	*/
	public final static void applyRotation( final Vector3 _axis, final Quaternion _rotate )
	{
		final float w1 = ( 0 * _rotate.w ) - ( _axis.x * _rotate.x ) - ( _axis.y * _rotate.y ) - ( _axis.z * _rotate.z ) ;
		final float x1 = ( 0 * _rotate.x ) + ( _axis.x * _rotate.w ) + ( _axis.y * _rotate.z ) - ( _axis.z * _rotate.y ) ;
		final float y1 = ( 0 * _rotate.y ) - ( _axis.x * _rotate.z ) + ( _axis.y * _rotate.w ) + ( _axis.z * _rotate.x ) ;
		final float z1 = ( 0 * _rotate.z ) + ( _axis.x * _rotate.y ) - ( _axis.y * _rotate.x ) + ( _axis.z * _rotate.w ) ;
		_axis.x = x1 ; _axis.y = y1 ; _axis.z = z1 ;
	}
}
