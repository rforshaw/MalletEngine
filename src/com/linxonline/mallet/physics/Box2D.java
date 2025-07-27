package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class Box2D extends Hull
{
	public final AABB aabb ;
	public final OBB obb ;

	public Box2D( final AABB _aabb, final Vector2 _position, final Vector2 _offset )
	{
		this( _aabb, null, _position, _offset ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables, final Vector2 _position, final Vector2 _offset )
	{
		super( _position.x, _position.y, _offset.x, _offset.y, 0.0f, _collidables ) ;
		aabb = _aabb ;
		obb = OBB.create( aabb ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables, final Vector3 _position, final Vector3 _offset )
	{
		super( _position.x, _position.y, _offset.x, _offset.y, 0.0f, _collidables ) ;
		aabb = _aabb ;
		obb = OBB.create( aabb ) ;
	}

	public Box2D( final AABB _aabb, final float _x, final float _y, final float _offsetX, final float _offsetY )
	{
		this( _aabb, null, _x, _y, _offsetX, _offsetY ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables, final float _x, final float _y, final float _offsetX, final float _offsetY )
	{
		super( _x, _y, _offsetX, _offsetY, 0.0f, _collidables ) ;
		aabb = _aabb ;
		obb = OBB.create( aabb ) ;
	}

	public Box2D( final AABB _aabb, int[] _collidables )
	{
		super( 0.0f, 0.0f,
			   0.0f, 0.0f,
			   0.0f,
			   _collidables ) ;
		aabb = _aabb ;
		obb = OBB.create( aabb ) ;
	}
	
	public Box2D( final OBB _obb, int[] _collidables )
	{
		super( 0.0f, 0.0f,
			   0.0f, 0.0f,
			   0.0f,
			   _collidables ) ;
		obb = _obb ;
		aabb = AABB.create( obb ) ;
	}

	@Override
	public void setRotation( final float _theta )
	{
		super.setRotation( _theta ) ;
		obb.setRotation( _theta, offsetX, offsetY ) ;
		obb.getAsAABB( aabb ) ;
	}

	@Override
	public int getPointsLength()
	{
		return obb.getLength() ;
	}

	@Override
	public Vector2 getPoint( final int _index, final Vector2 _fill )
	{
		return obb.getPoint( _index, _fill ) ;
	}

	@Override
	public float[] calculateAxes( final float[] _axes )
	{
		return obb.calculateAxes( _axes ) ;
	}

	@Override
	public float projectToAxis( final Vector2 _axis )
	{
		final float aX = _axis.x ;
		final float aY = _axis.y ;

		obb.getPoint( 0, _axis ) ;
		float pX = _axis.x ;
		float pY = _axis.y ;

		float dp = Vector2.dot( pX, pY, aX, aY ) ;

		float max = dp ;
		float min = dp ;

		final int length = obb.getLength() ;
		for( int i = 1; i < length; ++i )
		{
			obb.getPoint( i, _axis ) ;
			pX = _axis.x ;
			pY = _axis.y ;

			dp = Vector2.dot( pX, pY, aX, aY ) ;

			if( dp > max )
			{
				max = dp ;
			}
			else if( dp < min )
			{
				min = dp ;
			}
		}

		_axis.x = aX ;
		_axis.y = aY ;
		return ( max - min ) * 0.5f ;
	}

	@Override
	public AABB getAABB( final AABB _fill )
	{
		final float x = positionX + offsetX ;
		final float y = positionY + offsetY ;

		_fill.minX = aabb.minX + x ;
		_fill.minY = aabb.minY + y ;
		_fill.maxX = aabb.maxX + x ;
		_fill.maxY = aabb.maxY + y ;

		return _fill ;
	}

	@Override
	public boolean ray( final Ray _ray )
	{
		final AABB temp = getAABB( _ray.getTempAABB() ) ;
		final Intersection intersection = _ray.getIntersection() ;

		temp.ray( _ray.getPoint(), _ray.getDirection(), intersection ) ;
		return intersection.isValid() ;
	}
}
