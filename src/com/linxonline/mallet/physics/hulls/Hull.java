package com.linxonline.mallet.physics.hulls ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.physics.* ;
import com.linxonline.mallet.physics.primitives.AABB ;

public abstract class Hull
{
	public static final int NO_GROUP = -1 ;
	public static final int VECTOR_TYPE = 2 ;

	public static final int POSITION_X = 0 ;
	public static final int POSITION_Y = 1 ;
	public static final int OFFSET_X = 2 ;
	public static final int OFFSET_Y = 3 ;
	public static final int ROTATION = 4 ;

	private int groupID = NO_GROUP ;					// Defines what Group the Hull is in.
	private final int[] collidableGroups ;				// Defines the Groups the Hull is affected by.
														// If no group-specified, collides with everything.
	private Object parent ;

	private final Vector2 accumulatedPenetration = new Vector2() ;
	public final ContactData contactData = new ContactData() ;

	protected boolean collidable = true ; 							// Allows hull to produce Collision Data.
	protected boolean physical = true ; 							// Allows hull to be affected by a Collision
	protected CollisionCallback callback = null ;					// Allows Owner to be informed of Collisions

	protected final float[] position = new float[( 2 * VECTOR_TYPE ) + 1] ;

	protected Hull( final float _x, final float _y,
					final float _offsetX, final float _offsetY,
					final float _theta,
					final int[] _collidables )
	{
		collidableGroups = _collidables ;
		setPosition( _x, _y ) ;
		setOffset( _offsetX, _offsetY ) ;
	}

	/**
		Iterate over the contact points and accumulate the overall 
		penetration depth and direction. 
	*/
	public static void calculatePenetrationDepth( final ContactData _contacts, final ContactPoint _point, final Vector2 _accumulatedPenetration )
	{
		final int size = _contacts.size() ;
		for( int i = 0; i < size; ++i )
		{
			_contacts.get( i, _point ) ;
			if( _point.physical == true )
			{
				_accumulatedPenetration.x += _point.contactNormalX * _point.penetration ;
				_accumulatedPenetration.y += _point.contactNormalY * _point.penetration ;
			}
		}
	}

	public final void setGroupID( final int _groupID )
	{
		groupID = _groupID ;
	}

	public final void setCollisionCallback( final CollisionCallback _callback )
	{
		callback = _callback ;
	}

	public void setPosition( final float _x, final float _y )
	{
		FloatBuffer.set( position, POSITION_X, _x, _y ) ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		FloatBuffer.add( position, POSITION_X, _x, _y ) ;
	}

	public void setOffset( final float _x, final float _y )
	{
		FloatBuffer.set( position, OFFSET_X, _x, _y ) ;
	}

	public void addToOffset( final float _x, final float _y )
	{
		FloatBuffer.add( position, OFFSET_X, _x, _y ) ;
	}

	public void setRotation( final float _theta )
	{
		position[ROTATION] = _theta ;
	}

	public Vector2 getPosition( final Vector2 _fill )
	{
		_fill.x = position[POSITION_X] ;
		_fill.y = position[POSITION_Y] ;
		return _fill ;
	}

	public Vector2 getOffset( final Vector2 _fill )
	{
		_fill.x = position[OFFSET_X] ;
		_fill.y = position[OFFSET_Y] ;
		return _fill ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		_fill.x = position[POSITION_X] ;
		_fill.y = position[POSITION_Y] ;
		_fill.z = 0.0f ;
		return _fill ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		_fill.x = position[OFFSET_X] ;
		_fill.y = position[OFFSET_Y] ;
		_fill.z = 0.0f ;
		return _fill ;
	}

	public float getRotation()
	{
		return position[ROTATION] ;
	}

	public abstract float[] getAxes() ;
	public abstract float[] getPoints() ;
	public abstract float projectToAxis( final Vector2 _axis ) ;

	public abstract AABB getAABB( final AABB _fill ) ;

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
		if( collidableGroups == null )
		{
			// Groups haven't been specified so it can collide with all
			return true ;
		}
	
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

	public static boolean isCollidableWithGroup( final int _id, final int[] _groups )
	{
		if( _groups == null )
		{
			// Groups haven't been specified so it can collide with all
			return true ;
		}

		for( int i = 0; i < _groups.length; ++i )
		{
			if( _id == _groups[i] )
			{
				return true ;
			}
		}

		return false ;
	}
}
