package com.linxonline.mallet.entity ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.id.ID ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.event.* ;

/**
	Entity is a container class for Componets.
	By default an Entity should have a name, and a global family name.
	For example an Entity could be named HENCHMAN, and be in the ENEMY family.
	
	Entity also allows an internal component to send messages to other components,
	it operates in a similar basis to the EventSystem.
	
	Entity does not have the capabilities to recieve Events from a Game State, this 
	should be done through a component like EventComponent that can then route it 
	through the Component Event System.
**/
public final class Entity
{
	private final List<Component> components = MalletList.<Component>newList() ;
	private final IEventSystem eventSystem = new EventSystem( "COMPONENT_EVENT_SYSTEM" ) ;		// Component Event System

	public final ID id ;							// ID for this Entity: Name:Family
	public Vector3 position = new Vector3() ;		// Position of Entity in world space
	private boolean destroy = false ;				// Is the Entity to be destroyed and subsequently removed?

	public Entity()
	{
		this( "NONE", "NONE" ) ;
	}

	public Entity( final String _name )
	{
		this( _name, "NONE" ) ;
	}

	public Entity( final String _name, final String _family )
	{
		id = new ID( _name, _family ) ;
	}

	public final void setPosition( final float _x, final float _y, final float _z )
	{
		position.x = _x ;
		position.y = _y ;
		position.z = _z ;
	}

	public final void addToPosition( final float _x, final float _y, final float _z )
	{
		position.x += _x ;
		position.y += _y ;
		position.z += _z ;
	}

	/**
		Add a component to the Entity and set its parent to the Entity
		The Component should not be owned by another Component, it could get messy!
	**/
	private final void addComponent( final Component _component )
	{
		components.add( _component ) ;

		final EventController controller = _component.getComponentEventController() ;
		controller.setAddEventInterface( eventSystem ) ;
		eventSystem.addEventHandler( controller ) ;
	}

	/**
		Update the message system of the Entity
		and update the Components
	**/
	public final void update( final float _dt )
	{
		eventSystem.update() ;
		// Update Components
		final int size = components.size() ;
		for( int i = 0; i < size; ++i )
		{
			components.get( i ).update( _dt ) ;
		}
	}

	/**
		Return all the components with the designated name
	**/
	public final List<Component> getComponentsByName( final String _name, final List<Component> _components )
	{
		for( final Component component : components )
		{
			if( component.isName( _name ) == true )
			{
				_components.add( component ) ;
			}
		}

		return _components ;
	}

	/**
		Get the Components that have the same Group name and return them in 
		a List.
	**/
	public final List<Component> getComponentByGroup( final String _group, final List<Component> _components )
	{
		for( final Component component : components )
		{
			if( component.isGroup( _group ) == true )
			{
				_components.add( component ) ;
			}
		}

		return _components ;
	}

	/**
		Returns the List that contains all the Entity's Components.
		NOTE: the List is NOT a copy.
	**/
	public final List<Component> getAllComponents( final List<Component> _components )
	{
		_components.addAll( components ) ;
		return _components ;
	}

	/** 
		Returns the Entiy's current position
	**/
	public final Vector3 getPosition()
	{
		return position ;
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
		final Entity.ReadyCallback readyDestroy = new Entity.ReadyCallback()
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

		final int size = components.size() ;
		for( int i = 0; i < size; i++ )
		{
			components.get( i ).readyToDestroy( readyDestroy ) ;
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
		protected final EventController componentEvents = new EventController() ;	// Handles events from parent
		protected final ID id ;														// Name and Group Name

		public Component()
		{
			this( null, null ) ;
		}

		/**
			Engine specified codes for nameID and groupID.
			Not guaranteed to be unique over 16 characters.
		*/
		public Component( final String _name, final String _group )
		{
			id = new ID( ( _name != null ) ? _name : "NONE", ( _group != null ) ? _group : "NONE" ) ;
			initComponentEventProcessors( getComponentEventController() ) ;

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

		public final boolean isName( final String _name )
		{
			return id.isName( _name ) ;
		}

		public final boolean isGroup( final String _group )
		{
			return id.isGroup( _group ) ;
		}

		/**
			Override to add Event Processors to the component's
			Event Controller.
			Make sure to call super to ensure parents 
			component Event Processors are added.
		*/
		public void initComponentEventProcessors( final EventController _controller ) {}

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
		}

		/**
			Return the internal Event Controller for this component.
			Passes and Receives event components within the parent Entity. 
		*/
		public EventController getComponentEventController()
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
