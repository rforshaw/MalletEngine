package com.linxonline.mallet.entity ;

import java.util.List ;

import com.linxonline.mallet.entity.Entity.Component ;

import com.linxonline.mallet.event.IEventSystem ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

/**
	The EntitySystem stores and updates Entities that are being 
	used in the current running Game State.
**/
public class EntitySystem implements IEntitySystem
{
	private final IEventSystem eventSystem ;
	private final IEntityUpdate updater ;							// Entities update protocol

	private final List<Entity> entitiesToAdd = MalletList.<Entity>newList() ;
	private final List<Entity> cleanup = MalletList.<Entity>newList() ;

	private final List<Entity> entities = MalletList.<Entity>newList() ;		// Active entities

	public EntitySystem( final IEventSystem _eventSystem )
	{
		this( _eventSystem, Threaded.SINGLE ) ;
	}

	public EntitySystem( IEventSystem _eventSystem, final Threaded _mode )
	{
		eventSystem = _eventSystem ;
		switch( _mode )
		{
			default     :
			case SINGLE :
			{
				updater = new DefaultSTUpdate() ;
				break ;
			}
			case MULTI  :
			{
				updater = new DefaultMTUpdate() ;
				break ;
			}
		}
	}

	/**
		Add the Entity to the entity list at the next appropriate 
		moment. This prevents an Entity from being added during 
		the update of the EntitySystem.
	**/
	@Override
	public void addEntity( final Entity _entity )
	{
		if( _entity == null )
		{
			Logger.println( "Attempting to add null entity.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		entitiesToAdd.add( _entity ) ;
	}

	@Override
	public void removeEntity( final Entity _entity )
	{
		if( _entity == null )
		{
			Logger.println( "Attempting to remove null entity.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		cleanup.add( _entity ) ;
	}

	@Override
	public void update( final float _dt )
	{
		toBeAddedEntities() ;
		updater.update( _dt, entities, cleanup ) ;
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
		final List<Component> components = MalletList.<Component>newList() ;

		final int size = entitiesToAdd.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = entitiesToAdd.get( i ) ;
			hookEntity( eventSystem, events, entity, components ) ;
			entities.add( entity ) ;
		}
		entitiesToAdd.clear() ;
	}

	private void cleanupEntities()
	{
		if( cleanup.isEmpty() == true )
		{
			return ;
		}

		final List<Event<?>> events = MalletList.<Event<?>>newList() ;
		final List<Component> components = MalletList.<Component>newList() ;

		final int size = cleanup.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Entity entity = cleanup.get( i ) ;

			unhookEntity( eventSystem, events, entity, components ) ;
			entities.remove( entity ) ;
		}
		cleanup.clear() ;
	}

	@Override
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
	@Override
	public List<Entity> getEntities( final List<Entity> _entities )
	{
		_entities.addAll( entities ) ;
		_entities.addAll( entitiesToAdd ) ;
		return _entities ;
	}

	private void hookEntity( final IEventSystem _eventSystem, final List<Event<?>> _events, final Entity _entity, final List<Component> _components )
	{
		{
			// Retrieve component system-registering events.
			final int size = _entity.getAllComponents( _components ).size() ;
			for( int i = 0; i < size; ++i )
			{
				final Component component = _components.get( i ) ;
				component.passInitialEvents( _events ) ;
			}
		}

		{
			final int size = _events.size() ;
			for( int i = 0; i < size; i++ )
			{
				_eventSystem.addEvent( _events.get( i ) ) ;
			}
		}

		_events.clear() ;
		_components.clear() ;
	}

	private void unhookEntity( final IEventSystem _eventSystem, final List<Event<?>> _events, final Entity _entity, final List<Component> _components )
	{
		{
			// Retrieve component system-registering events.
			final int size = _entity.getAllComponents( _components ).size() ;
			for( int i = 0; i < size; ++i )
			{
				final Component component = _components.get( i ) ;
				component.passFinalEvents( _events ) ;		// Retrieve the Events that will unregister the components
			}
		}

		{
			final int size = _events.size() ;
			for( int i = 0; i < size; i++ )
			{
				_eventSystem.addEvent( _events.get( i ) ) ;
			}
		}

		_events.clear() ;
		_components.clear() ;
	}

	public enum Threaded
	{
		SINGLE,
		MULTI
	} ;
}
