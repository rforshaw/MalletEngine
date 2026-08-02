package com.linxonline.mallet.physics ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public abstract class Hull
{
	public final static ICollider SHIFT_COLLIDER = ( final Hull _base, final Hull _collideWith, final ContactPoint _point ) ->
	{
		if( _base.isStatic() )
		{
			return ;
		}

		final AABB a = _base.getAABB( AABB.create() ) ;
		final AABB b = _base.getAABB( AABB.create() ) ;
		if( !a.intersectAABB( b ) )
		{
			return ;
		}

		final float u = _collideWith.isStatic() ? 1.0f : 0.5f ;
		final float x = ( _point.contactNormalX * _point.penetration ) * u ;
		final float y = ( _point.contactNormalY * _point.penetration ) * u ;

		_base.addToPosition( x, y ) ;
	} ;

	public final static ICollider NO_SHIFT_COLLIDER = ( final Hull _base, final Hull _collideWith, final ContactPoint _point ) ->
	{
	
	} ;

	public static final int NO_GROUP = -1 ;
	public static final int VECTOR_TYPE = 2 ;

	public static final int POSITION_X = 0 ;
	public static final int POSITION_Y = 1 ;
	public static final int OFFSET_X = 2 ;
	public static final int OFFSET_Y = 3 ;
	public static final int ROTATION = 4 ;

	public static final int COLLIDABLE = 1 << 0 ;
	public static final int IMMOVABLE = 1 << 1 ;
	public static final int CHANGED = 1 << 2 ;

	private int groupID = NO_GROUP ;					// Defines what Group the Hull is in.
	private final int collidableGroups ;				// Defines the Groups the Hull is affected by.
														// If no group-specified, collides with everything.
	private Object parent ;
	private ICollider collider = SHIFT_COLLIDER ;

	protected int flags = COLLIDABLE | CHANGED ;

	protected float positionX ;
	protected float positionY ;

	protected float offsetX ;
	protected float offsetY ;

	protected float rotation ;

	protected Hull( final float _x, final float _y,
					final float _offsetX, final float _offsetY,
					final float _theta,
					final int _collidables )
	{
		collidableGroups = _collidables ;
		setPosition( _x, _y ) ;
		setOffset( _offsetX, _offsetY ) ;
	}

	public void setCollider( final ICollider _collider )
	{
		collider = ( _collider == null ) ? SHIFT_COLLIDER : _collider ;
	}

	public ICollider getCollider()
	{
		return collider ;
	}

	public final void setGroupID( final int _groupID )
	{
		groupID = _groupID ;
	}

	public void setPosition( final float _x, final float _y )
	{
		positionX = _x ;
		positionY = _y ;
		changed() ;
	}

	public void addToPosition( final float _x, final float _y )
	{
		positionX += _x ;
		positionY += _y ;
		changed() ;
	}

	public void setOffset( final float _x, final float _y )
	{
		offsetX = _x ;
		offsetY = _y ;
		changed() ;
	}

	public void addToOffset( final float _x, final float _y )
	{
		offsetX += _x ;
		offsetY += _y ;
		changed() ;
	}

	public void setRotation( final float _theta )
	{
		rotation = _theta ;
		changed() ;
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

	public final void changed()
	{
		changed( true ) ;
	}

	public final void changed( final boolean _change )
	{
		flags = ( _change ) ? flags | CHANGED : flags & ~CHANGED ;
	}

	public final void setCollidable( final boolean _collidable )
	{
		flags = ( _collidable ) ? flags | COLLIDABLE : flags & ~COLLIDABLE ;
	}

	public final void setStatic( final boolean _static )
	{
		flags = ( _static ) ? flags | IMMOVABLE : flags & ~IMMOVABLE ;
	}

	public final int getGroupID()
	{
		return groupID ;
	}

	public final boolean hasChanged()
	{
		return ( flags & CHANGED ) == CHANGED ;
	}

	public final boolean isStatic()
	{
		return ( flags & IMMOVABLE ) == IMMOVABLE ;
	}

	public final boolean isCollidable()
	{
		return ( flags & COLLIDABLE ) == COLLIDABLE ;
	}

	public final boolean isCollidableWithGroup( final int _groupID )
	{
		if( collidableGroups == 0 )
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

	public static boolean isCollidableWithGroup( final int _id, final int _groups )
	{
		if( _groups == 0 )
		{
			// Groups haven't been specified so it can collide with all
			return true ;
		}

		return ( _groups & _id ) == _id ;
	}

	public interface ICollider
	{
		public void apply( final Hull _base, final Hull _collideWith, final ContactPoint _point ) ;
	}
}
