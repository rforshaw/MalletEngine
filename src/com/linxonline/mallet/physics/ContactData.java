package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.physics.hulls.Hull ;

public class ContactData
{
	private static final int CONTACT_NORMAL_X = 0 ;
	private static final int CONTACT_NORMAL_Y = 1 ;

	public static final int MAX_COLLISION_POINTS = 5 ;

	private final Hull[] collidedWith = new Hull[MAX_COLLISION_POINTS] ;
	private final float[] contactNormal = new float[MAX_COLLISION_POINTS * 2] ;
	private final float[] penetration = new float[MAX_COLLISION_POINTS] ;
	private final boolean[] physical = new boolean[MAX_COLLISION_POINTS] ;

	private int usedContacts = 0 ;

	public ContactData() {}

	public final int addContact( final float _penetration, 
								  final Vector2 _normal, 
								  final boolean _physical,
								  final Hull _collidedWith )
	{
		return addContact( _penetration, _normal.x, _normal.y, _physical, _collidedWith ) ;
	}

	public final synchronized int addContact( final float _penetration, 
											  final float _normalX,
											  final float _normalY, 
											  final boolean _physical,
											  final Hull _collidedWith )
	{
		if( usedContacts < MAX_COLLISION_POINTS )
		{
			penetration[usedContacts] = _penetration ;
			contactNormal[( usedContacts * 2 ) + CONTACT_NORMAL_X] = _normalX ;
			contactNormal[( usedContacts * 2 ) + CONTACT_NORMAL_Y] = _normalY ;
			physical[usedContacts] = _physical ;
			collidedWith[usedContacts] = _collidedWith ;
			return usedContacts++ ;
		}

		return usedContacts ;
	}

	public final ContactPoint get( final int _i, final ContactPoint _point )
	{
		_point.penetration = penetration[_i] ;
		_point.contactNormal.setXY( contactNormal[( _i * 2 ) + CONTACT_NORMAL_X], contactNormal[( _i * 2 ) + CONTACT_NORMAL_Y] ) ;
		_point.physical = physical[_i]  ;
		_point.collidedWith = collidedWith[_i] ;
		return _point ;
	}

	public final synchronized void reset()
	{
		usedContacts = 0 ;
	}

	public final synchronized int size()
	{
		return usedContacts ;
	}
}
