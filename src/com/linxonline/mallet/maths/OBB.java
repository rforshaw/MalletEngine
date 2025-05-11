package com.linxonline.mallet.maths ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class OBB
{
	// Contains the original point data
	// before rotational transformations
	// are applied.
	private float tLX, tLY = 0.0f ;	// top left
	private float tRX, tRY = 0.0f ;	// top right
	private float bLX, bLY = 0.0f ;	// bottom left
	private float bRX, bRY = 0.0f ;	// bottom right

	// Contains the points after
	// rotational transformations have
	// been applied.
	private float rTLX, rTLY ;	// result top left
	private float rTRX, rTRY ;
	private float rBLX, rBLY ;
	private float rBRX, rBRY ;

	private OBB()
	{
		init() ;
	}

	private OBB( final Vector2 _topLeft,
				 final Vector2 _topRight, 
				 final Vector2 _bottomLeft,
				 final Vector2 _bottomRight )
	{
		tLX = _topLeft.x ;
		tLY = _topLeft.y ;

		tRX = _topRight.x ;
		tRY = _topRight.y ;

		bLX = _bottomLeft.x ;
		bLY = _bottomLeft.y ;

		bRX = _bottomRight.x ;
		bRY = _bottomRight.y ;
		init() ;
	}

	public static OBB create()
	{
		return new OBB() ;
	}

	public static OBB create( final AABB _aabb )
	{
		final OBB o = new OBB() ;
		o.setFromAABB( _aabb ) ;
		return o ;
	}

	public static OBB create( final Vector2 _topLeft,
							  final Vector2 _topRight, 
							  final Vector2 _bottomLeft,
							  final Vector2 _bottomRight )
	{
		return new OBB( _topLeft, _topRight, _bottomLeft, _bottomRight ) ;
	}

	public void setFromAABB( final AABB _aabb )
	{
		tLX = _aabb.minX ;
		tLY = _aabb.minY ;

		tRX = _aabb.maxX ;
		tRY = _aabb.minY ;

		bLX = _aabb.minX ;
		bLY = _aabb.maxY ;

		bRX = _aabb.maxX ;
		bRY = _aabb.maxY ;
		init() ;
	}

	/**
		Set the points and axes from the OBB being passed in.
	**/
	public void setFromOBB( final OBB _obb )
	{
		tLX = _obb.tLX ;
		tLY = _obb.tLY ;

		tRX = _obb.tRX ;
		tRY = _obb.tRY ;

		bLX = _obb.bLX ;
		bLY = _obb.bLY ;

		bRX = _obb.bRX ;
		bRY = _obb.bRY ;

		rTLX = _obb.rTLX ;
		rTLY = _obb.rTLY ;

		rTRX = _obb.rTRX ;
		rTRY = _obb.rTRY ;

		rBLX = _obb.rBLX ;
		rBLY = _obb.rBLY ;

		rBRX = _obb.rBRX ;
		rBRY = _obb.rBRY ;
	}

	public int getLength()
	{
		return 4 ;
	}

	/**
		Get the points absolute position, this includes the offset 
		and position of the OBB.
		returns a new Vector2().
	**/
	public Vector2 getPoint( final int _index, final Vector2 _fill )
	{
		switch( _index )
		{
			case 0 :
				_fill.x = rTLX ;
				_fill.y = rTLY ;
				break ;
			case 1 :
				_fill.x = rTRX ;
				_fill.y = rTRY ;
				break ;

			case 2 :
				_fill.x = rBLX ;
				_fill.y = rBLY ;
				break ;

			case 3 :
				_fill.x = rBRX ;
				_fill.y = rBRY ;
				break ;
		}

		return _fill ;
	}

	/**
		NEEDS TO BE REIMPLEMENTED
	**/
	public final void setRotation( final float _theta, final float _offsetX, final float _offsetY )
	{
		applyRotations( _theta, _offsetX, _offsetY ) ;
	}

	private void applyRotations( final float _theta, final float _offsetX, final float _offsetY )
	{
		final float sin = ( float )Math.sin( _theta ) ;
		final float cos = ( float )Math.cos( _theta ) ;

		// Top Left
		rTLX = tLX + _offsetX ;
		rTLY = tLY + _offsetY ;

		float x = ( rTLX * cos ) - ( rTLY * sin ) ;
		float y = ( rTLX * sin ) + ( rTLY * cos ) ;

		rTLX = x - _offsetX ;
		rTLY = y - _offsetY ;

		// Top Right
		rTRX = tRX + _offsetX ;
		rTRY = tRY + _offsetY ;

		x = ( rTRX * cos ) - ( rTRY * sin ) ;
		y = ( rTRX * sin ) + ( rTRY * cos ) ;

		rTRX = x - _offsetX ;
		rTRY = y - _offsetY ;

		// Bottom Left
		rBLX = bLX + _offsetX ;
		rBLY = bLY + _offsetY ;

		x = ( rBLX * cos ) - ( rBLY * sin ) ;
		y = ( rBLX * sin ) + ( rBLY * cos ) ;

		rBLX = x - _offsetX ;
		rBLY = y - _offsetY ;

		// Bottom Right
		rBRX = bRX + _offsetX ;
		rBRY = bRY + _offsetY ;

		x = ( rBRX * cos ) - ( rBRY * sin ) ;
		y = ( rBRX * sin ) + ( rBRY * cos ) ;

		rBRX = x - _offsetX ;
		rBRY = y - _offsetY ;
	}

	public final float[] calculateAxes( final float[] _axes )
	{
		_axes[1] = rTRX - rTLX ;			// x
		_axes[0] = -( rTRY - rTLY ) ;		// y

		float length = Vector2.length( _axes[0], _axes[1] ) ;
		_axes[0] /= length ;
		_axes[1] /= length ;

		_axes[3] = rTRX - rBRX ;		// x
		_axes[2] = -( rTRY - rBRY ) ;	// y

		length = Vector2.length( _axes[2], _axes[3] ) ;
		_axes[2] /= length ;
		_axes[3] /= length ;

		return _axes ;
	}

	private final void init()
	{
		applyRotations( 0.0f, 0.0f, 0.0f ) ;
	}

	@Override
	public String toString()
	{
		return "OBB" ;
	}
}
