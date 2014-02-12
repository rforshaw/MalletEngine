package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.physics.primitives.Circle ;

public class Circle2D extends Hull
{
	public final Circle circle ;

	public Circle2D( final Circle _circle )
	{
		super( HullType.CIRCLE2D ) ;
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
}