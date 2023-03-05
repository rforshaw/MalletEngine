package com.linxonline.mallet.maths ;

/**
	A plane represents a surface that extends indefinitely. 
*/
public class Plane
{
	private final Vector3 normal = new Vector3() ;
	private float distance ;

	/**
		Create a plane based on 3 points within
		the same coordinate space.
		Assumes the points are in a counter-clockwise rotation. 
	*/
	public Plane( final Vector3 _ptA, final Vector3 _ptB, final Vector3 _ptC )
	{
		updatePlane( _ptA, _ptB, _ptC ) ;
	}

	/**
		Return the direction of the plane.
	*/
	public Vector3 getNormal( final Vector3 _fill )
	{
		_fill.setXYZ( normal ) ;
		return _fill ;
	}

	/**
		Return the distance the plane is from the origin.
	*/
	public float getDistance()
	{
		return distance ;
	}

	/**
		A plane can be reused to represent a different surface.
		Assumes the points are counter-clockwise.
	*/
	public void updatePlane( final Vector3 _ptA, final Vector3 _ptB, final Vector3 _ptC )
	{
		Vector3.cross( _ptB.x - _ptA.x, _ptB.y - _ptA.y, _ptB.z - _ptA.z,
					   _ptC.x - _ptA.x, _ptC.y - _ptA.y, _ptC.z - _ptA.z, normal ) ;
		normal.normalise() ;
		distance = Vector3.dot( normal, _ptA ) ;
	}

	/**
		Project the passed in _pt to the surface of the plane.
		Populate _fill with the result and return.
	*/
	public Vector3 projectOnTo( final Vector3 _pt, final Vector3 _fill )
	{
		_fill.setXYZ( _pt ) ;
		return projectOnTo( _fill ) ;
	}

	/**
		Project the passed in _pt to the surface of the plane.
		Populate _pt with the result and return.
	*/
	public Vector3 projectOnTo( final Vector3 _pt )
	{
		final float t = Vector3.dot( normal, _pt ) - distance ;
		_pt.x = _pt.x - ( t * normal.x ) ;
		_pt.y = _pt.y - ( t * normal.y ) ;
		_pt.z = _pt.z - ( t * normal.z ) ;
		return _pt ;
	}
}
