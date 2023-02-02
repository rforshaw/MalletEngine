package com.linxonline.mallet.ecs ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.event.* ;

public final class ECSEvent implements IECS<ECSEvent.Component>
{
	public enum Type
	{
		GAMESTATE,		// Allow components to pass events to the game-state
		SYSTEM,			// Allow components to pass events to the system
		ENTITY			// Allow components to pass events to the entity.
	}

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final Map<ECSEntity, IEventSystem> entityLookup = MalletMap.<ECSEntity, IEventSystem>newMap() ;
	private final List<IEventSystem> entities = MalletList.<IEventSystem>newList() ;

	private final List<Component> components = MalletList.<Component>newList() ;

	private final IEventSystem state ;
	private final IEventSystem system ;

	public ECSEvent( final IEventSystem _state, final IEventSystem _system )
	{
		state = _state ;
		system = _system ;
	}

	@Override
	public ECSEvent.Component create( final ECSEntity _parent )
	{
		return create( _parent, Type.ENTITY ) ;
	}

	public ECSEvent.Component create( final ECSEntity _parent, final Type _type, final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		final IEventSystem sys = getEventSystem( _type, _parent ) ;
		final Component component = new Component( _parent, _type, sys, _processors ) ;
		invokeLater( () ->
		{
			components.add( component ) ;
		} ) ;
		return component ;
	}

	@Override
	public void remove( final ECSEvent.Component _component )
	{
		invokeLater( () ->
		{
			if( components.remove( _component ) )
			{
				switch( _component.getType() )
				{
					default     : break ;
					case ENTITY :
					{
						final ECSEntity entity = _component.getParent() ;
						final IEventSystem sys = entityLookup.remove( entity ) ;
						entities.remove( sys ) ;
						break ;
					}
				}
			}
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;

		int size = entities.size() ;
		for( int i = 0; i < size; ++i )
		{
			final IEventSystem sys = entities.get( i ) ;
			sys.sendEvents() ;
		}

		size = components.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Component component = components.get( i ) ;
			component.update() ;
		}
	}

	private IEventSystem getEventSystem( final Type _type, ECSEntity _entity )
	{
		switch( _type )
		{
			default        : return state ;
			case GAMESTATE : return state ;
			case SYSTEM    : return system ;
			case ENTITY    :
			{
				IEventSystem sys = entityLookup.get( _entity ) ;
				if( sys == null )
				{
					sys = new EventSystem() ;
					entityLookup.put( _entity, sys ) ;
					entities.add( sys ) ;
				}
				return sys ;
			}
		}
	}

	private void invokeLater( final Runnable _run )
	{
		if( _run != null )
		{
			executions.add( _run ) ;
		}
	}

	private void updateExecutions()
	{
		executions.update() ;
		final List<Runnable> runnables = executions.getCurrentData() ;
		if( runnables.isEmpty() )
		{
			return ;
		}

		final int size = runnables.size() ;
		for( int i = 0; i < size; i++ )
		{
			runnables.get( i ).run() ;
		}
		runnables.clear() ;
	}

	public class Component extends ECSEntity.Component
	{
		private final Type type ;
		private final IEventSystem system ;

		private IEventController controller = NullEventController.FALLBACK  ;

		private Component( final ECSEntity _parent,
						   final Type _type,
						   final IEventSystem _system,
						   final Tuple<String, EventController.IProcessor<?>> ... _processors )
		{
			_parent.super() ;
			type = _type ;
			system = _system ;

			controller = new EventController( _processors ) ;
			controller.setAddEventInterface( _system ) ;
			_system.addHandler( controller ) ;
		}

		private Type getType()
		{
			return type ;
		}

		public void passEvent( final Event<?> _event )
		{
			controller.passEvent( _event ) ;
		}

		private void remove()
		{
			system.removeHandler( controller ) ;
			controller.setAddEventInterface( null ) ;
		}

		private void update()
		{
			controller.update() ;
		}
	}
}


