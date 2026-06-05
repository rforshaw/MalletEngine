package com.linxonline.mallet.physics ;

import java.util.Arrays ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class ContactData
{
	private static final int CONTACT_NORMAL_X = 0 ;
	private static final int CONTACT_NORMAL_Y = 1 ;
	private static final int CONTACT_PENETRATION = 2 ;

	public static final int INITIAL_COLLISION_POINTS = 1000 ;
	public static final int MAX_COLLISION_POINTS = Integer.MAX_VALUE / 3 ;

	private Hull[] hulls = new Hull[INITIAL_COLLISION_POINTS * 2] ;
	private float[] contacts = new float[INITIAL_COLLISION_POINTS * 3] ;

	private int usedContacts = 0 ;

	public ContactData() {}

	public final int addContact( final float _penetration,
								 final Vector2 _normal,
								 final Hull _a,
								 final Hull _b )
	{
		return addContact( _penetration, _normal.x, _normal.y, _a, _b ) ;
	}

	public final int addContact( final float _penetration,
											  final float _normalX,
											  final float _normalY,
											  final Hull _a,
											  final Hull _b )
	{
		int hullSize = hulls.length / 2 ;
		if( usedContacts >= hullSize && usedContacts < MAX_COLLISION_POINTS )
		{
			int extra = hullSize ;
			final int total = hullSize + extra ;

			extra = ( total <= MAX_COLLISION_POINTS ) ? extra : ( extra - ( total - MAX_COLLISION_POINTS ) ) ;

			hulls = expand( hulls, extra * 2 ) ;
			contacts = FloatBuffer.expand( contacts, extra * 3 ) ;
		}

		hullSize = hulls.length / 2 ;
		if( usedContacts < hullSize )
		{
			final int cIndex = usedContacts * 3 ;
			contacts[cIndex + CONTACT_NORMAL_X] = _normalX ;
			contacts[cIndex + CONTACT_NORMAL_Y] = _normalY ;
			contacts[cIndex + CONTACT_PENETRATION] = _penetration ;

			final int hIndex = usedContacts * 2 ;
			hulls[hIndex] = _a ;
			hulls[hIndex + 1] = _b ;

			return usedContacts++ ;
		}

		return usedContacts ;
	}

	public final int addContacts( final ContactData _contacts )
	{
		final int toAddSize = _contacts.size() ;
		final int futureSize = usedContacts + toAddSize ;

		int hullSize = hulls.length / 2 ;
		if( futureSize > hullSize )
		{
			final int extra = ( toAddSize > hullSize ) ? toAddSize + hullSize : hullSize ;

			hulls = expand( hulls, extra * 2 ) ;
			contacts = FloatBuffer.expand( contacts, extra * 3 ) ;
		}

		System.arraycopy( _contacts.hulls, 0, hulls, usedContacts * 2, toAddSize * 2 ) ;
		System.arraycopy( _contacts.contacts, 0, contacts, usedContacts * 3, toAddSize * 3 ) ;

		usedContacts = futureSize;
		return usedContacts ;
	}

	public final ContactPoint get( final int _i, final ContactPoint _point )
	{
		final int cIndex = _i * 3 ;
		_point.contactNormalX = contacts[cIndex + CONTACT_NORMAL_X] ;
		_point.contactNormalY = contacts[cIndex + CONTACT_NORMAL_Y] ;
		_point.penetration = contacts[cIndex + CONTACT_PENETRATION] ;

		final int hIndex = _i * 2 ;
		_point.a = hulls[hIndex] ;
		_point.b = hulls[hIndex + 1] ;

		return _point ;
	}

	public final void reset()
	{
		// We want to trim the number of contacts
		// if we are no longer making use of them.
		// We want to ensure this is indeed the case.
		Arrays.fill( hulls, 0, usedContacts * 2, null ) ;

		usedContacts = 0 ;
		return ;
	}

	public final int size()
	{
		return usedContacts ;
	}

	private static Hull[] expand( final Hull[] _from, final int _extra )
	{
		final int length = _from.length + _extra ;
		final Hull[] to = new Hull[length] ;
		System.arraycopy( _from, 0, to, 0, _from.length ) ;
		return to ;
	}
}
