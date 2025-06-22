package com.linxonline.mallet.maths ;

public final class Intersection
{
	private float x = 0 ;
	private float y = 0 ;
	private float z = 0 ;
	private float tmin = -1.0f ;

	public Intersection() {}

	public void reset()
	{
		x = 0.0f ;
		y = 0.0f ;
		z = 0.0f ;
		tmin = -1.0f ;
	}

	public boolean isValid()
	{
		return tmin >= 0.0f ;
	}

	public void setPoint( final float _x, final float _y, final float _z )
	{
		x = _x ;
		y = _y ;
		z = _z ;
	}

	public void setDistance( final float _tmin )
	{
		tmin = _tmin ;
	}

	public Vector3 getPoint( final Vector3 _fill )
	{
		_fill.x = x ;
		_fill.y = y ;
		_fill.z = z ;
		return _fill ;
	}

	public float getDistance()
	{
		return tmin ;
	}

	@Override
	public String toString()
	{
		return "[point: X:" + x + " Y: " + y + " Z: " + z + " distance: " + tmin + "]" ;
	}
}
