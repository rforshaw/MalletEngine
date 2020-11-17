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

	private final Vector2 point = new Vector2() ;

	public Box2D( final AABB _aabb )
	{
		this( _aabb, null ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables )
	{
		super( _collidables ) ;
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}

	@Override
	public void setPosition( final float _x, final float _y )
	{
		aabb.setPosition( _x, _y ) ;
		obb.setPosition( _x, _y ) ;
	}

	@Override
	public void addToPosition( final float _x, final float _y )
	{
		aabb.addToPosition( _x, _y ) ;
		obb.addToPosition( _x, _y ) ;
	}

	@Override
	public void setOffset( final float _x, final float _y )
	{
		aabb.setOffset( _x, _y ) ;
		obb.setOffset( _x, _y ) ;
	}

	@Override
	public void addToOffset( final float _x, final float _y )
	{
		aabb.addToOffset( _x, _y ) ;
		obb.addToOffset( _x, _y ) ;
	}

	@Override
	public void setRotation( final float _theta )
	{
		obb.setRotation( _theta ) ;
		aabb.setDimensionsFromOBB( obb ) ;
	}

	@Override
	public Vector2 getPosition( final Vector2 _fill )
	{
		return obb.getPosition( _fill ) ;
	}

	@Override
	public Vector2 getOffset( final Vector2 _fill )
	{
		return obb.getOffset( _fill ) ;
	}

	@Override
	public float getRotation()
	{
		return obb.getRotation() ;
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
		return obb.rotations ;
	}

	@Override
	public void getAbsoluteCenter( final Vector2 _center )
	{
		aabb.getAbsoluteCenter( _center ) ;
	}

	@Override
	public float projectToAxis( final Vector2 _axis )
	{
		FloatBuffer.fill( obb.rotations, point, 0 ) ;
		float dp = Vector2.dot( point, _axis ) ;

		float max = dp ;
		float min = dp ;

		for( int i = 2; i < obb.rotations.length; i += 2 )
		{
			FloatBuffer.fill( obb.rotations, point, i ) ;
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
