package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class ContactData
{
	private static final int BELOW_EXCESS_RESIZE_LIMIT = 1000 ;

	private static final int CONTACT_NORMAL_X = 0 ;
	private static final int CONTACT_NORMAL_Y = 1 ;
	private static final int CONTACT_PENETRATION = 2 ;

	public static final int INITIAL_COLLISION_POINTS = 10 ;
	public static final int MAX_COLLISION_POINTS = 50 ;

	private Hull[] collidedWith = new Hull[INITIAL_COLLISION_POINTS] ;
	private float[] contacts = new float[INITIAL_COLLISION_POINTS * 3] ;
	private boolean[] physical = new boolean[INITIAL_COLLISION_POINTS] ;

	private int belowExcess = 0 ;
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
		if( usedContacts >= collidedWith.length && usedContacts < MAX_COLLISION_POINTS )
		{
			int extra = INITIAL_COLLISION_POINTS ;
			final int total = collidedWith.length + extra ;

			extra = ( total <= MAX_COLLISION_POINTS ) ? extra : ( extra - ( total - MAX_COLLISION_POINTS ) ) ;

			collidedWith = expand( collidedWith, extra ) ;
			contacts = FloatBuffer.expand( contacts, extra * 3 ) ;
			physical = expand( physical, extra ) ;
		}

		if( usedContacts < collidedWith.length )
		{
			final int index = usedContacts * 3 ;
			contacts[index + CONTACT_NORMAL_X] = _normalX ;
			contacts[index + CONTACT_NORMAL_Y] = _normalY ;
			contacts[index + CONTACT_PENETRATION] = _penetration ;
			physical[usedContacts] = _physical ;
			collidedWith[usedContacts] = _collidedWith ;
			return usedContacts++ ;
		}

		return usedContacts ;
	}

	public final ContactPoint get( final int _i, final ContactPoint _point )
	{
		final int index = _i * 3 ;
		_point.contactNormalX = contacts[index + CONTACT_NORMAL_X] ;
		_point.contactNormalY = contacts[index + CONTACT_NORMAL_Y] ;
		_point.penetration = contacts[index + CONTACT_PENETRATION] ;
		_point.physical = physical[_i]  ;
		_point.collidedWith = collidedWith[_i] ;
		return _point ;
	}

	public final synchronized void reset()
	{
		// We want to trim the number of contacts
		// if we are no longer making use of them.
		// We want to ensure this is indeed the case.
		if( collidedWith.length > INITIAL_COLLISION_POINTS )
		{
			belowExcess = ( usedContacts < INITIAL_COLLISION_POINTS ) ? belowExcess + 1 : 0 ;
			if( belowExcess > BELOW_EXCESS_RESIZE_LIMIT )
			{
				usedContacts = 0 ;
				belowExcess = 0 ;

				collidedWith = new Hull[INITIAL_COLLISION_POINTS] ;
				contacts = new float[INITIAL_COLLISION_POINTS * 3] ;
				physical = new boolean[INITIAL_COLLISION_POINTS] ;
				return ;
			}
		}

		for( int i = 0; i < usedContacts; ++i )
		{
			// We want to avoid retaining onto an object
			// that may have been destroyed.
			collidedWith[i] = null ;
		}

		usedContacts = 0 ;
		return ;
	}

	public final synchronized int size()
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

	private static boolean[] expand( final boolean[] _from, final int _extra )
	{
		final int length = _from.length + _extra ;
		final boolean[] to = new boolean[length] ;
		System.arraycopy( _from, 0, to, 0, _from.length ) ;
		return to ;
	}
}
