package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;

public final class ContactPoint
{
	public Hull a = null ;
	public Hull b = null ;

	public float contactNormalX = 0.0f ;
	public float contactNormalY = 0.0f ;
	public float penetration = 0.0f ;

	public ContactPoint() {}

	public Vector2 getContactNormal( final Vector2 _fill )
	{
		_fill.x = contactNormalX ;
		_fill.y = contactNormalY ;
		return _fill ;
	}
}
