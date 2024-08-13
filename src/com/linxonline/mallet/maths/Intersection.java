package com.linxonline.mallet.maths ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class Intersection
{
	private final Vector3 point = new Vector3() ;
	private float tmin = -1.0f ;

	public Intersection() {}

	public void reset()
	{
		point.setXYZ( 0, 0, 0 ) ;
		tmin = -1.0f ;
	}

	public boolean isValid()
	{
		return tmin >= 0.0f ;
	}

	public void setPoint( final float _x, final float _y, final float _z )
	{
		point.setXYZ( _x, _y, _z ) ;
	}

	public void setDistance( final float _tmin )
	{
		tmin = _tmin ;
	}

	public Vector3 getPoint( final Vector3 _fill )
	{
		_fill.x = point.x ;
		_fill.y = point.y ;
		_fill.z = point.z ;
		return _fill ;
	}

	public float getDistance()
	{
		return tmin ;
	}

	@Override
	public String toString()
	{
		return "[point: " + point.toString() + " distance: " + tmin + "]" ;
	}
}
