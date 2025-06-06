package com.linxonline.mallet.entity ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity.Component ;

import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

/**
	The EntitySystem stores and updates Entities that are being 
	used in the current running Game State.
**/
public final class EntitySystem
{
	public final int capacity ;

	private List<Entity> entitiesToAdd = null ;
	private List<Entity> cleanup = null ;
	private List<Entity> entities = null ;		// Active entities

	public EntitySystem()
	{
		this( 100 ) ;
	}

	public EntitySystem( final int _initialCapacity )
	{
		capacity = _initialCapacity ;

		entitiesToAdd = MalletList.<Entity>newList( capacity ) ;
		cleanup = MalletList.<Entity>newList( capacity ) ;
		entities = MalletList.<Entity>newList( capacity ) ;
	}

	/**
		Add the Entity to the entity list at the next appropriate 
		moment. This prevents an Entity from being added during 
		the update of the EntitySystem.
	**/
	public void addEntity( final Entity _entity )
	{
		if( _entity == null )
		{
			Logger.println( "Attempting to add null entity.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		entitiesToAdd.add( _entity ) ;
	}

	public void removeEntity( final Entity _entity )
	{
		if( _entity == null )
		{
			Logger.println( "Attempting to remove null entity.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		cleanup.add( _entity ) ;
	}

	public void update( final float _dt )
	{
		toBeAddedEntities() ;

		Entity entity = null ;
		final int entitySize = entities.size() ;
		for( int i = 0; i < entitySize; ++i )
		{
			entity = entities.get( i ) ;
			entity.update( _dt ) ;

			if( entity.isDead() == true )
			{
				cleanup.add( entity ) ;
			}
		}

		cleanupEntities() ;
	}

	/**
		Loop through the entitiesToAdd and hook them up 
		and add them to the entity list.
	**/
	private void toBeAddedEntities()
	{
		if( entitiesToAdd.isEmpty() )
		{
			return ;
		}

		final List<Event<?>> events = MalletList.<Event<?>>newList() ;

		final int size = entitiesToAdd.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = entitiesToAdd.get( i ) ;
			hookEntity( events, entity ) ;
			entities.add( entity ) ;
		}
		entitiesToAdd.clear() ;
		
		if( size > capacity )
		{
			// If the size of entitiesToAdd exceeds our capacity then 
			// we want to resize the array - it's easy for an 
			// array to expand, it's much harder to shrink it!
			entitiesToAdd = MalletList.<Entity>newList( capacity ) ;
		}
	}

	private void cleanupEntities()
	{
		if( cleanup.isEmpty() == true )
		{
			return ;
		}

		final List<Event<?>> events = MalletList.<Event<?>>newList() ;

		final int size = cleanup.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = cleanup.get( i ) ;

			unhookEntity( events, entity ) ;
			entities.remove( entity ) ;
		}
		cleanup.clear() ;

		if( size > capacity )
		{
			// If the size of cleanup exceeds our capacity then 
			// we want to resize the array - it's easy for an 
			// array to expand, it's much harder to shrink it!
			cleanup = MalletList.<Entity>newList( capacity ) ;
		}
	}

	public void clear()
	{
		for( final Entity entity : entities )
		{
			entity.destroy() ;
		}

		entitiesToAdd.clear() ;
		cleanup.clear() ;
	}

	/**
		Returns a list of all entities, including those to be added 
		to the main list.
	**/
	public List<Entity> getEntities( final List<Entity> _entities )
	{
		_entities.addAll( entities ) ;
		_entities.addAll( entitiesToAdd ) ;
		return _entities ;
	}

	private void hookEntity( final List<Event<?>> _events, final Entity _entity )
	{
		_entity.passInitialEvents( _events ) ;

		Event.addEvents( _events ) ;
		_events.clear() ;
	}

	private void unhookEntity( final List<Event<?>> _events, final Entity _entity )
	{
		_entity.passFinalEvents( _events ) ;

		Event.addEvents( _events ) ;
		_events.clear() ;
	}
}
