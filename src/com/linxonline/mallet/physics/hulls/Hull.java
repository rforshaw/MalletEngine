package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.physics.* ;
import com.linxonline.mallet.physics.primitives.AABB ;

public abstract class Hull
{
	public static final int NO_GROUP = -1 ;

	private Group.ID groupID = Group.get( -1 ) ;					// Defines what Group the Hull is in.
	private final Group.ID[] collidableGroups ;						// Defines the Groups the Hull is affected by.
																	// If no group-specified, collides with everything.
	private Object parent ;

	private final Vector2 accumulatedPenetration = new Vector2() ;
	public final ContactData contactData = new ContactData() ;

	protected boolean collidable = true ; 							// Allows hull to produce Collision Data.
	protected boolean physical = true ; 							// Allows hull to be affected by a Collision
	protected CollisionCallback callback = null ;					// Allows Owner to be informed of Collisions

	protected Hull( final Group.ID[] _collidables )
	{
		collidableGroups = _collidables ;
	}

	/**
		Update the Contact Data points.
		return the accumulatedPenetration
	**/
	public Vector2 updateContactData()
	{
		accumulatedPenetration.x = 0.0f ;
		accumulatedPenetration.y = 0.0f ;

		final ContactPoint point = new ContactPoint() ;

		final int size = contactData.size() ;
		for( int i = 0; i < size; ++i )
		{
			contactData.get( i, point ) ;
			if( point.physical == true )
			{
				accumulatedPenetration.x += point.contactNormal.x * point.penetration ;
				accumulatedPenetration.y += point.contactNormal.y * point.penetration ;
			}
		}

		contactData.reset() ;
		return accumulatedPenetration ;
	}

	public final void setGroupID( final int _groupID )
	{
		final Group.ID id = Group.get( _groupID ) ;
		if( id == null )
		{
			System.out.println( "Attempted to set collidable group that doesn't exist." ) ;
			return ;
		}

		setGroupID( id ) ;
	}

	public final void setGroupID( final Group.ID _id )
	{
		groupID = _id ;
	}

	public final void setCollisionCallback( final CollisionCallback _callback )
	{
		callback = _callback ;
	}

	public abstract void setPosition( final float _x, final float _y ) ;
	public abstract void addToPosition( final float _x, final float _y ) ;

	public abstract void setOffset( final float _x, final float _y ) ;
	public abstract void addToOffset( final float _x, final float _y ) ;

	public abstract void setRotation( final float _theta ) ;

	public abstract Vector2 getPosition( final Vector2 _fill ) ;
	public abstract Vector2 getOffset( final Vector2 _fill ) ;
	public abstract float getRotation() ;

	public abstract float[] getAxes() ;
	public abstract float[] getPoints() ;

	public abstract void getAbsoluteCenter( final Vector2 _center ) ;
	public abstract float projectToAxis( final Vector2 _axis ) ;

	public abstract AABB getAABB() ;

	public final void setCollidable( final boolean _collidable )
	{
		collidable = _collidable ;
	}

	public final void setPhysical( final boolean _physical )
	{
		physical = _physical ;
	}

	public final Group.ID getGroupID()
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

	public final boolean isCollidableWithGroup( final Group.ID _groupID )
	{
		return isCollidableWithGroup( _groupID, collidableGroups ) ;
	}

	public final CollisionCallback getCallback()
	{
		return callback ;
	}

	public final void setParent( final Object _parent )
	{
		parent = _parent ;
	}

	public final Object getParent()
	{
		return parent ;
	}

	public static boolean isCollidableWithGroup( final Group.ID _id, final Group.ID[] _groups )
	{
		if( _groups == null )
		{
			// Groups haven't been specified so it can collide with all
			return true ;
		}

		for( int i = 0; i < _groups.length; ++i )
		{
			final Group.ID id = _groups[i] ;
			if( id.equals( _id ) == true )
			{
				return true ;
			}
		}

		return false ;
	}
}
