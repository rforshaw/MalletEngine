package com.linxonline.mallet.physics ;

import java.util.ArrayList ;

import com.linxonline.mallet.physics.hulls.* ;
import com.linxonline.mallet.event.* ;

public class CollisionSystem extends EventController
{
	private final ArrayList<Hull> hulls = new ArrayList<Hull>() ;
	private final QuadTree treeHulls = new QuadTree() ;

	public CollisionSystem( final AddEventInterface _addInterface )
	{
		setAddEventInterface( _addInterface ) ;
		initEventProcessors() ;
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
		if( exists( _hull ) == true )
		{
			hulls.remove( _hull ) ;
			treeHulls.removeHull( _hull ) ;
		}
	}

	public void update( final float _dt )
	{
		update() ;			// Update Event Controller

		treeHulls.clear() ;
		final int size = hulls.size() ;
		for( int i = 0; i < size; i++ )
		{
			treeHulls.insertHull( hulls.get( i ) ) ;
		}

		treeHulls.update( _dt ) ;
	}

	private final boolean exists( final Hull _hull )
	{
		return treeHulls.exists( _hull ) ;
	}
}
