package com.linxonline.mallet.entity.system ;

import java.util.List ;

import com.linxonline.mallet.util.Utility ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.entity.query.* ;
import com.linxonline.mallet.entity.* ;

/**
	The EntitySystem stores and updates Entities that are being used in the current running Game State.
	Use the QuerySystem to effeciently search for entities.
	You can also extends the EntitySystem so it updates certain entities before others, by default
	it simply stores them all in a massive List.
**/
public class EntitySystem implements EntitySystemInterface
{
	protected final HookEntity state ;												// Interface to Game State hook functions
	protected final QuerySystem querySystem = new QuerySystem() ;					// Should be thread safe
	protected final Settings hashQuery = new Settings() ;							// Not thread safe

	protected EntityUpdateInterface entities = new DefaultSTUpdate() ;				// Entities update protocol
	protected final List<Entity> entitiesToAdd = Utility.<Entity>newArrayList() ;
	protected final List<Entity> cleanup = Utility.<Entity>newArrayList() ;

	public EntitySystem( final HookEntity _state )
	{
		state = _state ;
		querySystem.addQuery( new HashMapQuery( "HASHMAP" ) ) ;
	}

	/**
		Add the Entity to the entity list at the next appropriate 
		moment. This prevents an Entity from being added during 
		the update of the EntitySystem.
	**/
	@Override
	public void addEntity( final Entity _entity )
	{
		if( _entity != null )
		{
			entitiesToAdd.add( _entity ) ;
		}
	}

	/**
		Add the Entity directly to the entity list. 
		NOTE: This should be used carefully.
	**/
	@Override
	public void addEntityNow( final Entity _entity )
	{
		if( _entity != null )
		{
			injectEntity( _entity ) ;
		}
	}

	@Override
	public void removeEntity( final Entity _entity )
	{
		if( _entity != null )
		{
			cleanup.add( _entity ) ;
		}
	}

	/**
		Add a QueryInterface to the QuerySystem.
		All entities are added to this new QueryInterface
	**/
	@Override
	public void addQuery( final QueryInterface _interface )
	{
		querySystem.addQuery( _interface ) ;
		final List<Entity> ents = entities.getEntities() ;
		for( final Entity entity : ents )
		{
			_interface.addEntity( entity ) ;
		}
	}

	@Override
	public void update( final float _dt )
	{
		toBeAddedEntities() ;
		entities.update( _dt ) ;
		cleanupEntities() ;
	}

	/**
		Loop through the entitiesToAdd and hook them up 
		and add them to the entity list.
	**/
	protected void toBeAddedEntities()
	{
		final int size = entitiesToAdd.size() ;
		for( int i = 0; i < size; ++i )
		{
			injectEntity( entitiesToAdd.get( i ) ) ;
		}
		entitiesToAdd.clear() ;
	}

	protected void cleanupEntities()
	{
		if( entities.getCleanup( cleanup ) == true )
		{
			final int cleanupSize = cleanup.size() ;
			Entity entity = null ;

			for( int i = 0; i < cleanupSize; ++i )
			{
				entity = cleanup.get( i ) ;
				
				state.unhookEntity( entity ) ;
				entities.removeEntity( entity ) ;
				querySystem.removeEntity( entity ) ;
			}
			cleanup.clear() ;
		}
	}

	@Override
	public void clear()
	{
		final List<Entity> ents = entities.getEntities() ;
		for( final Entity entity : ents )
		{
			entity.destroy() ;
		}

		querySystem.clear() ;
		entitiesToAdd.clear() ;
		cleanup.clear() ;
	}

	/** 
		Returns a SearchInterface
		The Entity System returns an Interface of the QuerySystem
		to allow other systems to search the entities effectively.
	**/
	@Override
	public SearchInterface getSearch()
	{
		return querySystem ;
	}

	/**
		Returns a list of all entities, including those to be added 
		to the main list.
	**/
	@Override
	public List<Entity> getEntities()
	{
		final List<Entity> ents = Utility.<Entity>newArrayList() ;
		ents.addAll( entities.getEntities() ) ;
		ents.addAll( entitiesToAdd ) ;
		return ents ;
	}

	@Override
	public Entity getEntityByName( final String _name )
	{
		hashQuery.addString( HashMapQuery.NAME, _name ) ;
		return querySystem.queryForEntity( "HASHMAP", hashQuery ) ;
	}

	/**
		Does the actual hard work of adding the entity to the 
		required systems.
	*/
	private void injectEntity( final Entity _entity )
	{
		ensureEntityNameIsUnique( _entity ) ;
		state.hookEntity( _entity ) ;			// Hook the Entity components to required systems
		entities.addEntity( _entity ) ;			// Add Entity to Entities update list
		querySystem.addEntity( _entity ) ;		// Add Entity to the Query System
	}

	/**
		Used to ensure entity has a unique name.
		If the name already exists, it will add a number to the name
		NOTE: Horribly inefficient
	**/
	private final void ensureEntityNameIsUnique( final Entity _entity )
	{
		final String originalName = _entity.id.name ;
		String newName = originalName ;
		int iterations = 1 ;

		while( getEntityByName( newName ) != null )
		{
			newName = originalName + iterations++ ;
		}

		_entity.id.setName( newName ) ;
	}
}
