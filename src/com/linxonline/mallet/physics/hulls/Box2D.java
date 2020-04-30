package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.Group ;
import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.physics.primitives.OBB ;

public class Box2D extends Hull
{
	public final AABB aabb ;
	public final OBB obb ;

	private final Vector2 position = new Vector2() ;		// Used to reduce allocations - Android optimisation

	public Box2D( final AABB _aabb )
	{
		this( _aabb, null ) ;
	}

	public Box2D( final AABB _aabb, final Group.ID[] _collidables )
	{
		super( _collidables ) ;
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
		return obb.getCenter( position ) ;
	}

	@Override
	public float[] getAxes()
	{
		obb.updateAxesAndEdges() ;
		return obb.axes ;
	}

	@Override
	public float[] getPoints()
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
		final Vector2 point = new Vector2() ;
		FloatBuffer.fill( obb.points, point, 0 ) ;
		float dp = Vector2.dot( point, _axis ) ;

		float max = dp ;
		float min = dp ;

		for( int i = 2; i < obb.points.length; i += 2 )
		{
			FloatBuffer.fill( obb.points, point, i ) ;
			dp = Vector2.dot( point, _axis ) ;
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

	@Override
	public AABB getAABB()
	{
		return aabb ;
	}
}
