package com.linxonline.mallet.physics.primitives ;

import com.linxonline.mallet.maths.Vector2 ;

public final class Circle
{
	public final Vector2 position = new Vector2() ;
	public final float radius ;

	public Circle( final Vector2 _pos, final float _radius )
	{
		position.setXY( _pos ) ;
		radius = _radius ;
	}
	
	public void setPosition( final float _x, final float _y )
	{
		position.setXY( _x, _y ) ;
	}

	public void setRotation( final float _theta )
	{
		
	}
}