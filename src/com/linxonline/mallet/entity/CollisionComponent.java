package com.linxonline.mallet.entity ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.physics.* ;

public class CollisionComponent extends Component implements CollisionCallback
{
	public Hull hull = null ;

	public CollisionComponent()
	{
		super( "COLLISION", "COLLISIONCOMPONENT" ) ;
	}

	public CollisionComponent( final String _name )
	{
		super( _name, "COLLISIONCOMPONENT" ) ;
	}

	// Must be added to Entity before called!
	public void init( final Vector2 _min, final Vector2 _max, final Vector2 _position, final Vector2 _offset )
	{
		hull = new Box2D( new AABB( _min, _max, _position, _offset ) ) ;
		hull.setCollisionCallback( this ) ;
	}

	@Override
	public void setParent( final Entity _parent )
	{
		super.setParent( _parent ) ;
		hull.setParent( _parent ) ;
	}

	public void update( final float _dt )
	{
		super.update( _dt ) ;
		final int size = hull.getContactSize() ;
		if( size > 0 )
		{
			final Vector2 accumulated = hull.updateContactData() ;
			parent.addToPosition( accumulated.x / size, accumulated.y / size, 0.0f ) ;
		}
		hull.setPosition( parent.position.x, parent.position.y ) ;
	}

	public void setRotate( final float _theta )
	{
		hull.setRotation( _theta ) ;
	}

	@Override
	public void collisionCallback( final ContactPoint _point ) {}
}