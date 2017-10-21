package com.linxonline.mallet.physics ;

import java.util.List ;

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
		setAddEventInterface( _addInterface ) ;
		initEventProcessors() ;

		treeHulls = new QuadTree() ;
	}

	public CollisionSystem( final IAddEvent _addInterface, final WorkerGroup _workers )
	{
		setAddEventInterface( _addInterface ) ;
		initEventProcessors() ;

		treeHulls = new QuadTree( ( _workers != null ) ? _workers : new WorkerGroup( 4 ) ) ;
	}

	private void initEventProcessors()
	{
		addEventProcessor( new EventProcessor<Hull>( "COLLISION_SYSTEM", "ADD_COLLISION_HULL" )
		{
			public void processEvent( final Event<Hull> _event )
			{
				add( _event.getVariable() ) ;
			}
		} ) ;

		addEventProcessor( new EventProcessor<Hull>( "COLLISION_SYSTEM", "REMOVE_COLLISION_HULL" )
		{
			public void processEvent( final Event<Hull> _event )
			{
				remove( _event.getVariable() ) ;
			}
		} ) ;
	}

	public void add( final Hull _hull )
	{
		if( exists( _hull ) == false )
		{
			hulls.add( _hull ) ;
			treeHulls.insertHull( _hull ) ;
		}
	}

	public void remove( final Hull _hull )
	{
		hulls.remove( _hull ) ;
		treeHulls.removeHull( _hull ) ;
	}

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

		//System.out.println( "Collisions: " + collisions ) ;
		treeHulls.update( _dt ) ;
	}

	private final boolean exists( final Hull _hull )
	{
		return hulls.contains( _hull ) ;
	}
}
