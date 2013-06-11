package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;

public class Box2D extends Hull
{
	public final AABB aabb ;
	public final OBB obb ;

	public Box2D( final AABB _aabb )
	{
		setHullType( HullType.BOUNDINGBOX2D ) ;
		aabb = _aabb ;
		obb = new OBB( aabb ) ;
	}

	public void setPosition( final float _x, final float _y )
	{
		aabb.setPosition( _x, _y ) ;
		obb.setPosition( _x, _y ) ;
	}

	public void setRotation( final float _theta ) {}
}
