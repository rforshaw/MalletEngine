package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.physics.Group ;

public class Box2D extends Hull
{
	public final AABB aabb ;
	public final OBB obb ;

	private final Vector2 point = new Vector2() ;

	public Box2D( final AABB _aabb, final Vector2 _position, final Vector2 _offset )
	{
		this( _aabb, null, _position, _offset ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables, final Vector2 _position, final Vector2 _offset )
	{
		super( _position.x, _position.y, _offset.x, _offset.y, 0.0f, _collidables ) ;
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables, final Vector3 _position, final Vector3 _offset )
	{
		super( _position.x, _position.y, _offset.x, _offset.y, 0.0f, _collidables ) ;
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}

	public Box2D( final AABB _aabb, final float _x, final float _y, final float _offsetX, final float _offsetY )
	{
		this( _aabb, null, _x, _y, _offsetX, _offsetY ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables, final float _x, final float _y, final float _offsetX, final float _offsetY )
	{
		super( _x, _y, _offsetX, _offsetY, 0.0f, _collidables ) ;
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables )
	{
		super( 0.0f, 0.0f,
			   0.0f, 0.0f,
			   0.0f,
			   _collidables ) ;
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}
	
	public Box2D( final OBB _obb, int[] _collidables )
	{
		super( 0.0f, 0.0f,
			   0.0f, 0.0f,
			   0.0f,
			   _collidables ) ;
		obb = _obb ;
		aabb = new AABB( obb ) ;
	}

	@Override
	public void setRotation( final float _theta )
	{
		super.setRotation( _theta ) ;
		obb.setRotation( _theta, position[OFFSET_X], position[OFFSET_Y] ) ;
		aabb.setFromOBB( obb ) ;
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
	public AABB getAABB( final AABB _fill )
	{
		aabb.copyTo( _fill ) ;

		final float x = position[POSITION_X] + position[OFFSET_X] ;
		final float y = position[POSITION_Y] + position[OFFSET_Y] ;

		_fill.addTo( x, y ) ;

		return _fill ;
	}
}
