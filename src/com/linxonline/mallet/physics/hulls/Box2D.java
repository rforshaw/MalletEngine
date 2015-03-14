package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.physics.primitives.OBB ;

public class Box2D extends Hull
{
	public final AABB aabb ;
	public final OBB obb ;

	public Box2D( final AABB _aabb )
	{
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}

	public void setPosition( final float _x, final float _y )
	{
		aabb.setPosition( _x, _y ) ;
		obb.setPosition( _x, _y ) ;
	}

	public void setRotation( final float _theta )
	{
		obb.setRotation( _theta ) ;
		aabb.setDimensionsFromOBB( obb ) ;
	}

	public Vector2 getPosition()
	{
		return obb.getCenter() ;
	}

	@Override
	public Vector2[] getAxes()
	{
		obb.updateAxesAndEdges() ;
		return obb.axes ;
	}

	public Vector2[] getPoints()
	{
		return obb.points ;
	}

	@Override
	public void getAbsoluteCenter( final Vector2 _center )
	{
		aabb.getAbsoluteCenter( _center ) ;
	}

	@Override
	public float projectToAxis( final Vector2 _axis )
	{
		float dp = Vector2.dot( obb.points[0], _axis ) ;
		float max = dp ;
		float min = dp ;

		for( int i = 1; i < obb.points.length; ++i )
		{
			dp = Vector2.dot( obb.points[i], _axis ) ;
			if( dp > max )
			{
				max = dp ;
			}
			else if( dp < min )
			{
				min = dp ;
			}
		}

		return ( max - min ) * 0.5f ;
	}
}
