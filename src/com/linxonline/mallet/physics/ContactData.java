package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.physics.hulls.Hull ;

public class ContactData
{
	private static final int MAX_COLLISION_POINTS = 5 ;

	private final ContactPoint[] contacts = new ContactPoint[MAX_COLLISION_POINTS] ;
	private int usedContacts = 0 ;
	private int index = 0 ;

	public ContactData()
	{
		for( int i = 0; i < MAX_COLLISION_POINTS; ++i )
		{
			contacts[i] = new ContactPoint() ;
		}
	}

	public final void addContact( final float _penetration, 
								  final Vector2 _normal, 
								  final boolean _physical,
								  final Hull _collidedWith )
	{
		if( usedContacts < MAX_COLLISION_POINTS )
		{
			ContactPoint contact = contacts[usedContacts++] ;
			contact.penetration = _penetration ;
			contact.contactNormal = _normal ;
			contact.physical = _physical ;
			contact.collidedWith = _collidedWith ;
		}
	}

	public final void addContact( final float _penetration, 
								  final float _normalX, final float _normalY, 
								  final boolean _physical,
								  final Hull _collidedWith )
	{
		if( usedContacts < MAX_COLLISION_POINTS )
		{
			ContactPoint contact = contacts[usedContacts++] ;
			contact.penetration = _penetration ;
			contact.contactNormal.x = _normalX ;
			contact.contactNormal.y = _normalY ;
			contact.physical = _physical ;
			contact.collidedWith = _collidedWith ;
		}
	}

	public ContactPoint next()
	{
		if( index < usedContacts ) { return contacts[index++] ; }

		index = 0 ;
		return null ;
	}

	public void reset()
	{
		usedContacts = 0 ;
		index = 0 ;
	}

	public int size()
	{
		return usedContacts ;
	}
}