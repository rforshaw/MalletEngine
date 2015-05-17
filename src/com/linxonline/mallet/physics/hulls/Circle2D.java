package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.io.save.SaveClass ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.physics.primitives.Circle ;

public @SaveClass class Circle2D extends Hull
{
	public final Circle circle ;

	public Circle2D( final Circle _circle )
	{
		circle = _circle ;
	}

	public void setPosition( final float _x, final float _y )
	{
		circle.setPosition( _x, _y ) ;
	}

	public void setRotation( final float _theta )
	{
		circle.setRotation( _theta ) ;
	}
	
	@Override
	public Vector2[] getAxes()
	{
		return null ;
	}

	public Vector2[] getPoints()
	{
		return null ;
	}

	public Vector2 getPosition()
	{
		return circle.position ;
	}

	@Override
	public void getAbsoluteCenter( final Vector2 _center )
	{
		_center.setXY( 0.0f, 0.0f ) ;
	}

	@Override
	public float projectToAxis( final Vector2 _axis )
	{
		return -1.0f ;
	}
}