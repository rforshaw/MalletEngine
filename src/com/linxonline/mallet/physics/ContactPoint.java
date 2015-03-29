package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;

import com.linxonline.mallet.physics.hulls.Hull ;

public final class ContactPoint
{
	public Hull collidedWith = null ;
	public Vector2 contactNormal = new Vector2( 0.0f, 0.0f ) ;
	public float penetration = 0.0f ;
	public boolean physical = true ;

	public ContactPoint() {}

	public ContactPoint( final float _penetration, 
						 final Vector2 _contactNormal )
	{
		contactNormal = _contactNormal ;
		penetration = _penetration ;
	}

	public ContactPoint( final float _penetration, 
						 final Vector2 _contactNormal,
						 final boolean _physical )
	{
		contactNormal = _contactNormal ;
		penetration = _penetration ;
		physical = _physical ;
	}

	public ContactPoint( final float _penetration, 
						 final Vector2 _contactNormal,
						 final boolean _physical,
						 final Hull _collidedWith )
	{
		contactNormal = _contactNormal ;
		penetration = _penetration ;
		physical = _physical ;
		collidedWith = _collidedWith ;
	}
}