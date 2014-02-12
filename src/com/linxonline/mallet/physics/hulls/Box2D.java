package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.primitives.AABB ;
import com.linxonline.mallet.physics.primitives.OBB ;

public class Box2D extends Hull
{
	public final AABB aabb ;
	public final OBB obb ;

	public Box2D( final AABB _aabb )
	{
		super( HullType.BOUNDINGBOX2D ) ;
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
}
