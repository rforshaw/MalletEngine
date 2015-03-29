package com.linxonline.mallet.physics.hulls ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.physics.* ;

public abstract class Hull
{
	public static final int NO_GROUP = -1 ;

	private int groupID = NO_GROUP ;												// Defines what Group the Hull is in.
	private final ArrayList<Integer> collidableGroups = new ArrayList<Integer>() ;	// Defines the Groups the Hull is affected by.
																					// If no group-specified, collides with everything.

	private final Vector2 accumulatedPenetration = new Vector2() ;
	public final ContactData contactData = new ContactData() ;

	protected boolean collidable = true ; 										// Allows hull to produce Collision Data.
	protected boolean physical = true ; 										// Allows hull to be affected by a Collision
	protected CollisionCallback callback = null ;								// Allows Owner to be informed of Collisions

	protected Hull() {}

	/**
		Update the Contact Data points.
		return the accumulatedPenetration
	**/
	public Vector2 updateContactData()
	{
		accumulatedPenetration.x = 0.0f ;
		accumulatedPenetration.y = 0.0f ;

		ContactPoint point = null ;
		while( ( point = contactData.next() ) != null )
		{
			if( point.physical == true )
			{
				accumulatedPenetration.x += point.contactNormal.x * point.penetration ;
				accumulatedPenetration.y += point.contactNormal.y * point.penetration ;
			}

			if( callback != null )
			{
				callback.collisionCallback( point ) ;
			}
		}

		return accumulatedPenetration ;
	}

	public final void setGroupID( final int _groupID )
	{
		groupID = _groupID ;
	}

	public final void addCollidableGroup( final int _groupID )
	{
		collidableGroups.add( _groupID ) ;
	}

	public final void setCollisionCallback( final CollisionCallback _callback )
	{
		callback = _callback ;
	}

	public abstract void setPosition( final float _x, final float _y ) ;
	public abstract void setRotation( final float _theta ) ;

	public abstract Vector2 getPosition() ;

	public abstract Vector2[] getAxes() ;
	public abstract Vector2[] getPoints() ;

	public abstract void getAbsoluteCenter( final Vector2 _center ) ;
	public abstract float projectToAxis( final Vector2 _axis ) ;
	
	public final void setCollidable( final boolean _collidable )
	{
		collidable = _collidable ;
	}

	public final void setPhysical( final boolean _physical )
	{
		physical = _physical ;
	}

	public final int getGroupID()
	{
		return groupID ;
	}

	public final boolean isPhysical()
	{
		return physical ;
	}

	public final boolean isCollidable()
	{
		return collidable ;
	}

	public final boolean isCollidableWithGroup( final int _groupID )
	{
		if( collidableGroups.size() == 0 )
		{
			// Groups haven't been spcified so it can collide with all
			return true ;
		}

		return collidableGroups.contains( _groupID ) ;
	}
}