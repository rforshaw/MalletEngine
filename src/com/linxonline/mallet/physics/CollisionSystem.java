package com.linxonline.mallet.physics ;

import java.util.List ;

import com.linxonline.mallet.util.Threaded ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.worker.* ;

import com.linxonline.mallet.physics.hulls.* ;
import com.linxonline.mallet.event.* ;

public class CollisionSystem extends EventController
{
	private final List<Hull> hulls = MalletList.<Hull>newList() ;
	private final QuadTree treeHulls ;

	public CollisionSystem( final IAddEvent _addInterface )
	{
		this( _addInterface, Threaded.SINGLE ) ;
	}

	public CollisionSystem( final IAddEvent _addInterface, Threaded _type )
	{
		setAddEventInterface( _addInterface ) ;
		initEventProcessors() ;

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

	private void initEventProcessors()
	{
		addProcessor( "ADD_COLLISION_HULL", ( Hull _hull ) ->
		{
			add( _hull ) ;
		} ) ;

		addProcessor( "REMOVE_COLLISION_HULL", ( Hull _hull ) ->
		{
			remove( _hull ) ;
		} ) ;
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

	private CollisionCheck check = new CollisionCheck() ;

	public void update( final float _dt )
	{
		update() ;			// Update Event Controller

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

		//simpleUpdate( check, hulls ) ;
		//System.out.println( "Collisions: " + collisions ) ;
		treeHulls.update( _dt ) ;
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
