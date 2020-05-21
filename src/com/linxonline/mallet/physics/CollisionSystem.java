package com.linxonline.mallet.physics ;

import java.util.List ;

import com.linxonline.mallet.util.Threaded ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.worker.* ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.physics.hulls.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.Vector2 ;

public class CollisionSystem
{
	private final EventController eventController ;
	private final List<Hull> hulls = MalletList.<Hull>newList() ;
	private final QuadTree treeHulls ;

	public CollisionSystem( final IAddEvent _addInterface )
	{
		this( _addInterface, Threaded.SINGLE ) ;
	}

	public CollisionSystem( final IAddEvent _addInterface, Threaded _type )
	{
		eventController = new EventController( MalletList.toArray(
			Tuple.<String, EventController.IProcessor<?>>build( "ADD_COLLISION_HULL", ( final Hull _hull ) ->
			{
				add( _hull ) ;
			} ),
			Tuple.<String, EventController.IProcessor<?>>build( "REMOVE_COLLISION_HULL", ( final Hull _hull ) ->
			{
				remove( _hull ) ;
			} ),
			Tuple.<String, EventController.IProcessor<?>>build( "COLLISION_DELEGATE", ( final ICollisionDelegate.ICallback _callback ) ->
			{
				_callback.callback( constructCollisionDelegate() ) ;
			} )
		) ) ;

		switch( _type )
		{
			default     :
			case SINGLE :
			{
				treeHulls = new QuadTree() ;
				break ;
			}
			case MULTI  :
			{
				treeHulls = new QuadTree( new WorkerGroup( "COLLISION", 4 ) ) ;
				break ;
			}
		}
		
	}

	public EventController getEventController()
	{
		return eventController ;
	}

	public void add( final Hull _hull )
	{
		if( exists( _hull ) == false )
		{
			hulls.add( _hull ) ;
		}
	}

	public void remove( final Hull _hull )
	{
		hulls.remove( _hull ) ;
		treeHulls.removeHull( _hull ) ;
	}

	public void update( final float _dt )
	{
		eventController.update() ;
		treeHulls.clear() ;

		int collisions = 0 ;
		final int size = hulls.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Hull hull = hulls.get( i ) ;
			final ContactData contact = hull.contactData ;
			collisions += contact.size() ;

			contact.reset() ;
			treeHulls.insertHull( hull ) ;
		}

		treeHulls.update( _dt ) ;
	}

	private ICollisionDelegate constructCollisionDelegate()
	{
		return new ICollisionDelegate()
		{
			private boolean shutdown = false ;

			@Override
			public Hull generateContacts( final Hull _hull )
			{
				if( shutdown == false )
				{
					treeHulls.generateContacts( _hull ) ;
				}
				return _hull ;
			}

			@Override
			public Hull ray( final Vector2 _start, final Vector2 _end )
			{
				return ray( _start, _end, null ) ;
			}

			@Override
			public Hull ray( final Vector2 _start, final Vector2 _end, final Group.ID[] _filters )
			{
				final float step = 1.0f ;
				final float distance = Vector2.distance( _start, _end ) ;

				final Vector2 direction = new Vector2( _end ) ;
				direction.subtract( _start ) ;
				direction.normalise() ;

				final Vector2 point = new Vector2( _start ) ;
				for( float i = 0.0f; i < distance; i += step )
				{
					point.x += direction.x ;
					point.y += direction.y ;

					final Hull hull = treeHulls.getHullWithPoint( point, _filters ) ;
					if( hull != null )
					{
						return hull ;
					}
				}

				return null ;
			}

			@Override
			public void shutdown()
			{
				shutdown = true ;
			}
		} ;
	}

	private static void simpleUpdate( final CollisionCheck _check, final List<Hull> _hulls )
	{
		for( final Hull hull1 : _hulls )
		{
			if( hull1.isCollidable() == false )
			{
				continue ;
			}

			updateCollisions( _check, hull1, _hulls ) ;
		}
	}

	private static void updateCollisions( final CollisionCheck _check, final Hull _hull1, final List<Hull> _hulls )
	{
		for( final Hull hull2 : _hulls )
		{
			if( _hull1 == hull2 )
			{
				continue ;
			}

			if( _hull1.isCollidableWithGroup( hull2.getGroupID() ) == true )
			{
				if( _check.generateContactPoint( _hull1, hull2 ) == true )
				{
					if( _hull1.contactData.size() >= ContactData.MAX_COLLISION_POINTS )
					{
						// No point looking for more contacts if 
						// we've reached maximum.
						return ;
					}
				}
			}
		}
	}

	private final boolean exists( final Hull _hull )
	{
		return hulls.contains( _hull ) ;
	}
}
