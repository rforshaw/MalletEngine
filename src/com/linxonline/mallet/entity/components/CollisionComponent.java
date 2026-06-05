package com.linxonline.mallet.entity.components ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.physics.CollisionAssist ;
import com.linxonline.mallet.physics.ContactPoint ;
import com.linxonline.mallet.physics.Hull ;
import com.linxonline.mallet.physics.Box2D ;

public class CollisionComponent extends Component
{
	public final Hull[] hulls ;

	public CollisionComponent( final Entity _parent, final Hull ... _hulls )
	{
		this( _parent, Entity.AllowEvents.YES, _hulls ) ;
	}

	public CollisionComponent( final Entity _parent,
							   final Entity.AllowEvents _allow,
							   final Hull ... _hulls )
	{
		super( _parent, _allow ) ;
		hulls = _hulls ;
	}

	@Override
	public void readyToDestroy( final Entity.ReadyCallback _callback )
	{
		CollisionAssist.remove( hulls ) ;
		super.readyToDestroy( _callback ) ;
	}


	public static CollisionComponent createWithNoShift( final Entity _parent, final Hull ... _hulls )
	{
		for( final Hull hull : _hulls )
		{
			hull.setCollider( Hull.NO_SHIFT_COLLIDER ) ;
		}

		return new CollisionComponent( _parent, _hulls ) ;
	}

	public static CollisionComponent generateBox2D( final Entity _parent,
													final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		final Box2D hull = CollisionAssist.add( new Box2D( AABB.create( _min, _max ), 0 ) ) ;
		hull.setPosition( _position.x, _position.y ) ;
		hull.setOffset( _offset.x, _offset.y ) ;

		final CollisionComponent comp = new CollisionComponent( _parent, hull ) ;
		return comp ;
	}

	public static CollisionComponent generateBox2D( final Entity _parent,
													final Entity.AllowEvents _allow,
													final Vector2 _min,
													final Vector2 _max,
													final Vector2 _position,
													final Vector2 _offset )
	{
		final Box2D hull = CollisionAssist.add( new Box2D( AABB.create( _min, _max ), 0 ) ) ;
		hull.setPosition( _position.x, _position.y ) ;
		hull.setOffset( _offset.x, _offset.y ) ;

		final CollisionComponent comp = new CollisionComponent( _parent, _allow, hull ) ;
		return comp ;
	}
}
