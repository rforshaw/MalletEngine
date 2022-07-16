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
	By default an Entity should have a name, and a global family name.
	For example an Entity could be named HENCHMAN, and be in the ENEMY family.

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
		GAMESTATE,		// Allow components to pass events to the game-state
		SYSTEM,			// Allow components to pass events to the system
		YES,			// Same as Local
		NO				// No events can be called without crashing.
	}

	private final Component[] components ;
	private IEventSystem eventSystem = null ;										// Component Event System
	private IEventController stateController = NullEventController.FALLBACK  ;		// Used to talk to GameState
	private IEventController systemController = NullEventController.FALLBACK  ;		// Used to talk to GLDefaultSystem

	private boolean destroy = false ;				// Is the Entity to be destroyed and subsequently removed?
	private Entity.ReadyCallback readyDestroy ;

	public Entity( final int _capacity )
	{
		this( _capacity, AllowEvents.LOCAL ) ;
	}

	public Entity( final int _capacity, final AllowEvents ... _allow )
	{
		components = new Component[_capacity] ;
		for( final AllowEvents allow : _allow )
		{
			switch( allow )
			{
				default        :
				case YES       :
				case LOCAL     : eventSystem = new EventSystem( _capacity ) ; break ;
				case GAMESTATE : stateController = createStateEventController() ; break ;
				case SYSTEM    : systemController = createSystemEventController() ; break ;
			}
		}
	}

	/**
		Override to add Event Processors to the component's
		State Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public EventController createStateEventController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		return createController( _processors ) ;
	}

	/**
		Override to add Event Processors to the component's
		Backend Event Controller.
		Make sure to call super to ensure parents 
		component Event Processors are added.
	*/
	public EventController createSystemEventController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		return createController( _processors ) ;
	}

	private static EventController createController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
	{
		if( _processors == null )
		{
			return new EventController() ;
		}

		if( _processors.length == 0 )
		{
			return new EventController() ;
		}

		return new EventController( _processors ) ;
	}

	/**
		Convienience method to EventController's passEvent.
	**/
	public void passBackendEvent( final Event _event )
	{
		systemController.passEvent( _event ) ;
	}

	/**
		Convienience method to EventController's passEvent.
	**/
	public void passStateEvent( final Event _event )
	{
		stateController.passEvent( _event ) ;
	}

	private void addComponent( Component _component )
	{
		for( int i = 0; i < components.length; ++i )
		{
			if( components[i] == null )
			{
				components[i] = _component ;
				if( eventSystem != null )
				{
					final IEventController controller = _component.getComponentEventController() ;
					if( controller != null )
					{
						controller.setAddEventInterface( eventSystem ) ;
						eventSystem.addHandler( controller ) ;
					}
				}
				return ;
			}
		}

		Logger.println( "Failed to add " + _component + " to entity.", Logger.Verbosity.MAJOR ) ;
		throw new RuntimeException( "Failed to add component" ) ;
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
		_events.add( new Event<IEventController>( "ADD_BACKEND_EVENT", systemController ) ) ;
		_events.add( new Event<IEventController>( "ADD_GAME_STATE_EVENT", stateController ) ) ;

		for( final Component component : components )
		{
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
		_events.add( new Event<IEventController>( "REMOVE_BACKEND_EVENT", systemController )  ) ;
		_events.add( new Event<IEventController>( "REMOVE_GAME_STATE_EVENT", stateController )  ) ;

		for( final Component component : components )
		{
			component.passFinalEvents( _events ) ;
		}
	}

	/**
		Update the message system of the Entity
		and update the Components
	**/
	public final void update( final float _dt )
	{
		if( eventSystem != null )
		{
			// The entity can be constructed without an EventSystem.
			// Some entities will be constructed with components that 
			// do not inter-communicate.
			eventSystem.sendEvents() ;
		}

		systemController.update() ;
		stateController.update() ;

		// Update Components
		final int size = components.length ;
		for( int i = 0; i < size; ++i )
		{
			components[i].update( _dt ) ;
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
		for( final Component component : components )
		{
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
		protected final IEventController componentEvents ;	// Handles events from parent
		private boolean disabled = false ;

		public Component()
		{
			this( AllowEvents.YES ) ;
		}

		public Component( final AllowEvents _allow )
		{
			switch( _allow )
			{
				default :
				case YES     : componentEvents = createEventController() ; break ;
				case NO      : componentEvents = NullEventController.FALLBACK ;
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
		public EventController createEventController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
		{
			if( _processors == null )
			{
				return new EventController() ;
			}

			if( _processors.length == 0 )
			{
				return new EventController() ;
			}

			return new EventController( _processors ) ;
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
		public void passFinalEvents( final List<Event<?>> _events )
		{
			componentEvents.clearEvents() ;			// Clear any lingering events that may reside in the buffers.
		}

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

		/**
			Return the internal Event Controller for this component.
			Passes and Receives event components within the parent Entity. 
		*/
		public IEventController getComponentEventController()
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
