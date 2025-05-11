package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

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

	public final ContactData contactData = new ContactData() ;

	protected boolean collidable = true ; 							// Allows hull to produce Collision Data.
	protected boolean physical = true ; 							// Allows hull to be affected by a Collision

	protected float positionX ;
	protected float positionY ;

	protected float offsetX ;
	protected float offsetY ;
	
	protected float rotation ;
	
	protected Hull( final float _x, final float _y,
					final float _offsetX, final float _offsetY,
					final float _theta,
					final int[] _collidables )
	{
		collidableGroups = _collidables ;
		setPosition( _x, _y ) ;
		setOffset( _offsetX, _offsetY ) ;
	}

	public final void setGroupID( final int _groupID )
	{
		groupID = _groupID ;
	}

	public void setPosition( final float _x, final float _y )
	{
		positionX = _x ;
		positionY = _y ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		positionX += _x ;
		positionY += _y ;
	}

	public void setOffset( final float _x, final float _y )
	{
		offsetX = _x ;
		offsetY = _y ;
	}

	public void addToOffset( final float _x, final float _y )
	{
		offsetX += _x ;
		offsetY += _y ;
	}

	public void setRotation( final float _theta )
	{
		rotation = _theta ;
	}

	public Vector2 getPosition( final Vector2 _fill )
	{
		_fill.x = positionX ;
		_fill.y = positionY ;
		return _fill ;
	}

	public Vector2 getOffset( final Vector2 _fill )
	{
		_fill.x = offsetX ;
		_fill.y = offsetY ;
		return _fill ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		_fill.x = positionX ;
		_fill.y = positionY ;
		_fill.z = 0.0f ;
		return _fill ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		_fill.x = offsetX ;
		_fill.y = offsetY ;
		_fill.z = 0.0f ;
		return _fill ;
	}

	public float getRotation()
	{
		return rotation ;
	}

	public abstract int getPointsLength() ;
	public abstract Vector2 getPoint( final int _index, final Vector2 _fill ) ;

	public abstract float[] calculateAxes( final float[] _axes ) ;
	public abstract float projectToAxis( final Vector2 _axis ) ;

	public abstract AABB getAABB( final AABB _fill ) ;

	/**
		Does the hull interset with the passed in ray.
		Return true if it does, use getIntersection() on
		the ray to return intersection details.
	*/
	public abstract boolean ray( final Ray _ray ) ;

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
