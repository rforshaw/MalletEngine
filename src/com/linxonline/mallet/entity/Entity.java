package com.linxonline.mallet.entity ;

import java.util.List ;
import java.util.Collections;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.* ;

/**
	Entity is a container class for Components.

	Entity also allows an internal component to send messages to other components,
	it operates in a similar basis to the EventSystem.

	Entity has the ability to receive and send events to the system and/or
	game-state if specified. A default construction of an entity will only
	allow for local communication between components.
**/
public class Entity
{
	public enum AllowEvents
	{
		LOCAL,			// Allow components to communicate directly with each other.
		YES,			// Same as Local
		NO				// No events can be called without crashing.
	}

	private final Component[] components ;
	private EventState eventState = null ;			// Component Event System

	private boolean destroy = false ;				// Is the Entity to be destroyed and subsequently removed?
	private Entity.ReadyCallback readyDestroy ;

	public Entity( final int _capacity )
	{
		this( _capacity, AllowEvents.LOCAL ) ;
	}

	public Entity( final int _capacity, final AllowEvents ... _allow )
	{
		components = new Component[_capacity] ;
		final int size = _allow.length ;
		for( int i = 0; i < size; ++i )
		{
			final AllowEvents allow = _allow[i] ;
			switch( allow )
			{
				default        :
				case YES       :
				case LOCAL     : eventState = new EventState() ; break ;
				case NO        : break ;
			}
		}
	}

	private void addComponent( Component _component )
	{
		for( int i = 0; i < components.length; ++i )
		{
			if( components[i] == null )
			{
				components[i] = _component ;
				return ;
			}
		}

		Logger.println( "Failed to add " + _component + " to entity.", Logger.Verbosity.MAJOR ) ;
		throw new RuntimeException( "Failed to add component" ) ;
	}

	protected EventState getEventState()
	{
		if( eventState == null )
		{
			return Event.getGlobalState() ;
		}

		return eventState ;
	}

	/**
		Create events and add them to _events.
		The events will be passed to the Event System
		of the Game State. Use an Event to register the 
		component or variables it holds to external systems.
		Can also be used for any other events that may need 
		to be passed when the component is added to the Entity System. 
	*/
	public void passInitialEvents( final List<Event<?>> _events )
	{
		final int size = components.length ;
		for( int i = 0; i < size; ++i )
		{
			final Component component = components[i] ;
			component.passInitialEvents( _events ) ;
		}
	}

	/**
		Create events and add them to _events.
		The events will be passed to the Event System
		of the Game State. Use an Event to cleanup the 
		component or variables it holds to external systems.
		Can also be used for any other events that may need 
		to be passed when the component is removed from the Entity System.
		super.passFinalEvents(), must be called.
	*/
	public void passFinalEvents( final List<Event<?>> _events )
	{
		final int size = components.length ;
		for( int i = 0; i < size; ++i )
		{
			final Component component = components[i] ;
			component.passFinalEvents( _events ) ;
		}
	}

	/**
		Update the message system of the Entity
		and update the Components
	**/
	public final void update( final float _dt )
	{
		// Update Components
		final int size = components.length ;
		for( int i = 0; i < size; ++i )
		{
			components[i].update( _dt ) ;
		}

		if( eventState != null )
		{
			// We need to swap the back buffer with
			// the front buffer.
			eventState.swap() ;
		}
	}

	/**
		Returns the List that contains all the Entity's Components.
	**/
	public final List<Component> getAllComponents( final List<Component> _components )
	{
		Collections.addAll( _components, components ) ;
		return _components ;
	}

	/**
		Return the first component in the entity that matches
		the requested class type. If no component matches the
		criteria return null instead.
	*/
	public final <T extends Component> T getComponentByType( final Class<T> _clazz )
	{
		final int size = components.length ;
		for( int i = 0; i < size; ++i )
		{
			final Component component = components[i] ;
			if( _clazz.isInstance( component ) == true )
			{
				return _clazz.cast( component ) ;
			}
		}

		return null ;
	}

	/** 
		Call when you wish the Entity to be decalred dead.
		An Entity decalred destroyed, will be removed by the
		Entity System when appropriate.
		isDead will return true when the entity can be removed 
		from the EntitySystem.
	*/
	public final void destroy()
	{
		if( components.length <= 0 )
		{
			// The entity contains no components so
			// we can destroy the entity straight away.
			destroy = true ;
			return ;
		}
	
		if( readyDestroy != null )
		{
			return ;
		}

		readyDestroy = new Entity.ReadyCallback()
		{
			private final List<Entity.Component> toDestroy = MalletList.<Entity.Component>newList( components ) ;

			@Override
			public void ready( final Entity.Component _component )
			{
				// toDestroy should not need to be synchronised 
				// as Components of an Entity are not updated 
				// a-synchronously.
				if( toDestroy.remove( _component ) == true )
				{
					if( toDestroy.isEmpty() == true )
					{
						//System.out.println( "Entity destroyed." ) ;
						destroy = true ;
					}
				}
			}
		} ;

		final int size = components.length ;
		for( int i = 0; i < size; i++ )
		{
			components[i].readyToDestroy( readyDestroy ) ;
		}
	}

	/**
		Returns true when the entity can be removed 
		from the EntitySystem.
		Call destroy() if you want the Entity to clean-up 
		any resources it may be accessing before it is 
		completely removed from the EntitySystem.
	*/
	public boolean isDead()
	{
		return destroy ;
	}

	/**
		Implemented in Entity.
		The Entity will call Component.readyToDestroy when,
		it has been flagged for destruction. When the Component 
		is ready, it should call ReadyCallback.ready.
		The Entity will track which components have readied themselves, 
		once all components are readied it will destroy itself.
	*/
	public interface ReadyCallback
	{
		public void ready( final Entity.Component _component ) ;
	}

	/*==============================================================*/
	// Component - Root class for all Components used by Entity.	  //
	/*==============================================================*/

	public abstract class Component
	{
		private final IEventBlock componentEvents ;	// Handles events from parent
		private boolean disabled = false ;

		public Component()
		{
			this( AllowEvents.YES ) ;
		}

		public Component( final AllowEvents _allow )
		{
			switch( _allow )
			{
				default  :
				case YES : componentEvents = createEventBlock() ; break ;
				case NO  : componentEvents = NullEventBlock.create() ; break ;
			}

			getParent().addComponent( this ) ;
		}

		/**
			If overriding, you must call super.update( _dt ), 
			else componentEvents will not be updated.
		**/
		public void update( final float _dt )
		{
			componentEvents.update() ;
		}

		/**
			If the component requires an Event Controller construct one
			and add any required Event Processors to it.
		*/
		public EventBlock createEventBlock( final Tuple<String, Event.IProcess<?>> ... _processors )
		{
			final EventState state = getParent().getEventState() ;

			if( _processors == null )
			{
				return new EventBlock( state ) ;
			}

			if( _processors.length == 0 )
			{
				return new EventBlock( state ) ;
			}

			return new EventBlock( state, _processors ) ;
		}

		/**
			Create events and add them to _events.
			The events will be passed to the Event System
			of the Game State. Use an Event to register the 
			component or variables it holds to external systems.
			Can also be used for any other events that may need 
			to be passed when the component is added to the Entity System. 
		*/
		public void passInitialEvents( final List<Event<?>> _events ) {}

		/**
			Create events and add them to _events.
			The events will be passed to the Event System
			of the Game State. Use an Event to cleanup the 
			component or variables it holds to external systems.
			Can also be used for any other events that may need 
			to be passed when the component is removed from the Entity System.
			super.passFinalEvents(), must be called.
		*/
		public void passFinalEvents( final List<Event<?>> _events ) {}

		/**
			The parent can be flagged for destruction at anytime, a 
			component could be in an unstable state, for example 
			waiting for the id of a render request.
			readyToDestroy allows the component to inform the parent 
			it is safe to destroy itself. When overriding don't call super.
		*/
		public void readyToDestroy( final ReadyCallback _callback )
		{
			_callback.ready( this ) ;
			disabled = true ;
		}

		public boolean isDisabled()
		{
			return disabled ;
		}

		public void passComponentEvent( final Event<?> _event )
		{
			final EventState state = componentEvents.getEventState() ;
			state.addEvent( _event ) ;
		}

		/**
			Return the internal Event Controller for this component.
			Passes and Receives event components within the parent Entity. 
		*/
		public IEventBlock getComponentEventBlock()
		{
			return componentEvents ;
		}

		/**
			Return the parent of the component, 
			this should be guaranteed if the component 
			has been added to an Entity.
		*/
		public Entity getParent()
		{
			return Entity.this ;
		}
	}
}
