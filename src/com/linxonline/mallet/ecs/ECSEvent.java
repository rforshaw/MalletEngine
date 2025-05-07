package com.linxonline.mallet.ecs ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Parallel ;

import com.linxonline.mallet.event.* ;

public final class ECSEvent implements IECS<ECSEvent.Component>
{
	public enum Type
	{
		GLOBAL,
		ENTITY			// Allow components to pass events to the entity.
	}

	private final static ComponentUpdater componentUpdater = new ComponentUpdater() ;

	private final BufferedList<Runnable> executions = new BufferedList<Runnable>() ;

	private final Map<ECSEntity, EventState> lookup = MalletMap.<ECSEntity, EventState>newMap() ;
	private final List<EventState> states = MalletList.<EventState>newList() ;
	private final List<Component> components = MalletList.<Component>newList() ;

	public ECSEvent() {}

	@Override
	public ECSEvent.Component create( final ECSEntity _parent )
	{
		return create( _parent, Type.ENTITY ) ;
	}

	@SafeVarargs
	public final ECSEvent.Component create( final ECSEntity _parent, final Type _type, final Tuple<String, Event.IProcess<?>> ... _processors )
	{
		final EventState state = getEventState( _parent, _type ) ;
		final EventBlock block = new EventBlock( state, _processors ) ;

		final Component component = new Component( _parent, _type, block ) ;
		invokeLater( () ->
		{
			components.add( component ) ;
		} ) ;
		return component ;
	}

	public static Tuple<String, Event.IProcess<?>> create( final String _name, final Event.IProcess<?> _processor )
	{
		return Tuple.<String, Event.IProcess<?>>build( _name, _processor ) ;
	}

	@Override
	public void remove( final ECSEvent.Component _component )
	{
		invokeLater( () ->
		{
			if( components.remove( _component ) )
			{
				
			}
		} ) ;
	}

	@Override
	public void update( final double _dt )
	{
		updateExecutions() ;

		final int size = states.size() ;
		for( int i = 0; i < size; ++i )
		{
			final EventState state = states.get( i ) ;
			state.swap() ;
		}

		Parallel.forBatch( components, 1000, componentUpdater ) ;
	}

	private EventState getEventState( final ECSEntity _parent, final Type _type )
	{
		switch( _type )
		{
			default     :
			case GLOBAL : return Event.getGlobalState() ;
			case ENTITY :
			{
				EventState state = lookup.get( _parent ) ;
				if( state == null )
				{
					state = new EventState() ;
					states.add( state ) ;
					lookup.put( _parent, state ) ;
				}

				return state ;
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
		private final EventBlock block  ;

		private Component( final ECSEntity _parent, final Type _type, final EventBlock _block )
		{
			_parent.super() ;
			type = _type ;
			block = _block ;
		}

		public void passEvent( final Event<?> _event )
		{
			final EventState state = block.getEventState() ;
			state.addEvent( _event ) ;
		}

		public EventBlock getEventBlock()
		{
			return block ;
		}

		private Type getType()
		{
			return type ;
		}

		private void update()
		{
			block.update() ;
		}
	}

	private static final class ComponentUpdater implements Parallel.IListRun<Component>
	{
		@Override
		public void run( final int _start, final int _end, final List<Component> _components )
		{
			for( int i = _start; i < _end; ++i )
			{
				final Component component = _components.get( i ) ;
				component.update() ;
			}
		}
	}
}


