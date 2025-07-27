package com.linxonline.mallet.maths ;

public final class OBB
{
	private float x, y = 0.0f ;
	private float hWidth, hHeight = 0.0f ;

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

	private OBB( final AABB _aabb )
	{
		setFromAABB( _aabb ) ;
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

	public void setFromAABB( final AABB _aabb )
	{
		hWidth = ( _aabb.maxX - _aabb.minX ) * 0.5f ;
		hHeight = ( _aabb.maxY - _aabb.minY ) * 0.5f ;

		x = _aabb.minX + hWidth ;
		y = _aabb.minY + hHeight ;
		init() ;
	}

	/**
		Set the points and axes from the OBB being passed in.
	**/
	public void setFromOBB( final OBB _obb )
	{
		hWidth = _obb.hWidth ;
		hHeight = _obb.hHeight ;

		x = _obb.x ;
		y = _obb.y ;

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

	public AABB getAsAABB( final AABB _fill )
	{
		float minX = rTLX ;
		float minY = rTLY ;

		if( minX > rTRX ) { minX = rTRX ; }
		if( minX > rBLX ) { minX = rBLX ; }
		if( minX > rBRX ) { minX = rBRX ; }

		if( minY > rTRY ) { minY = rTRY ; }
		if( minY > rBLY ) { minY = rBLY ; }
		if( minY > rBRY ) { minY = rBRY ; }

		_fill.minX = minX ;
		_fill.minY = minY ;

		float maxX = rTLX ;
		float maxY = rTLY ;

		if( maxX < rTRX ) { maxX = rTRX ; }
		if( maxX < rBLX ) { maxX = rBLX ; }
		if( maxX < rBRX ) { minX = rBRX ; }

		if( maxY < rTRY ) { maxY = rTRY ; }
		if( maxY < rBLY ) { maxY = rBLY ; }
		if( maxY < rBRY ) { maxY = rBRY ; }

		_fill.maxX = maxX ;
		_fill.maxY = maxY ;

		return _fill ;
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
				_fill.x = rBRX ;
				_fill.y = rBRY ;
				break ;
			case 3 :
				_fill.x = rBLX ;
				_fill.y = rBLY ;
				break ;
		}

		return _fill ;
	}

	/**
		NEEDS TO BE REIMPLEMENTED
	**/
	public final void setRotation( final float _theta, final float _offsetX, final float _offsetY )
	{
		final float sin = ( float )Math.sin( _theta ) ;
		final float cos = ( float )Math.cos( _theta ) ;

		final float lX = x + -hWidth + _offsetX ;
		final float rX = x + hWidth + _offsetX ;

		final float tY = y + hHeight + _offsetY ;
		final float bY = y + -hHeight + _offsetY ;

		final float lXC = lX * cos ;
		final float rXC = rX * cos ;

		final float tYC = tY * cos ;
		final float bYC = bY * cos ;

		final float lXS = lX * sin ;
		final float rXS = rX * sin ;

		final float tYS = tY * sin ;
		final float bYS = bY * sin ;

		// Top Left
		rTLX = lXC - tYS - _offsetX ;
		rTLY = lXS + tYC - _offsetY ;

		// Top Right
		rTRX = rXC - tYS - _offsetX ;
		rTRY = rXS + tYC - _offsetY ;

		// Bottom Left
		rBLX = lXC - bYS - _offsetX ;
		rBLY = lXS + bYC - _offsetY ;

		// Bottom Right
		rBRX = rXC - bYS - _offsetX ;
		rBRY = rXS + bYC - _offsetY ;
	}

	public final float[] calculateAxes( final float[] _axes )
	{
		_axes[1] = rTRX - rTLX ;		// x
		_axes[0] = -( rTRY - rTLY ) ;	// y

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
		setRotation( 0.0f, 0.0f, 0.0f ) ;
	}

	@Override
	public String toString()
	{
		return "OBB" ;
	}
}
