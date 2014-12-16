package com.linxonline.mallet.game ;

import java.util.ArrayList ;

import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.game.statemachine.State ;

import com.linxonline.mallet.input.InputHandler ;
import com.linxonline.mallet.input.InputState ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventSystem ;
import com.linxonline.mallet.event.EventProcessor ;
import com.linxonline.mallet.event.EventController ;

import com.linxonline.mallet.physics.CollisionSystem ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.system.* ;
import com.linxonline.mallet.entity.components.* ;
import com.linxonline.mallet.animation.* ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.util.locks.Locks ;

import com.linxonline.mallet.util.factory.creators.* ;
import com.linxonline.mallet.util.factory.EntityFactory ;

public class GameState extends State implements HookEntity
{
	protected float DEFAULT_TIMESTEP = 1.0f / 20.0f ;					// 20Hz
	protected float DEFAULT_FRAMERATE = 1.0f / 60.0f ;					// 60Hz
	protected float DEFAULT_ESCAPE_TIME = 0.25f ;						// Escape threshold, if delta spirals out of control with no way to catchup
	protected long DEFAULT_SLEEP = 10L ;								// Duration to sleep before continuing update cycle

	protected UpdateInterface currentUpdate = null ;												// Current Running Mode

	protected final InputState inputSystem = new InputState() ;										// Internal Input System
	protected final EventSystem eventSystem = new EventSystem( "GAME_STATE_EVENT_SYSTEM" ) ;		// Internal Event System
	protected final EventController eventController = new EventController() ;						// Used to process Events, gateway between internal eventSystem and root event-system

	protected SystemInterface system = null ;														// Provides access to Root systems
	protected final AudioSystem audioSystem = new AudioSystem() ;									// Must specify a SourceGenerator
	protected final EntitySystemInterface entitySystem = new EntitySystem( this ) ;
	protected final AnimationSystem animationSystem = new AnimationSystem( eventSystem ) ;
	protected final CollisionSystem collisionSystem = new CollisionSystem( eventSystem ) ;

	protected boolean paused = false ;									// Determine whether state was paused.
	protected boolean draw = true ;										// Used to force a Draw
	protected double updateAccumulator = 0.0f ;							// Current dt update
	protected double renderAccumulator = 0.0f ;							// Current dt render

	public GameState( final String _name )
	{
		super( _name ) ;
		initModes() ;
		initEventProcessors() ;

		setFrameRate( GlobalConfig.getInteger( "MAXFPS", 60 ) ) ;
	}

	/**
		Add content here.
		Create entities, load resources, etc.
	*/
	public void initGame() {}

	/**
		Set any content required on resume after the state was paused.
	*/
	public void resumeGame() {}

	@Override
	public void startState( final Settings _package )
	{
		hookHandlerSystems() ;
		if( paused == true )
		{
			paused = false ;
			resumeGame() ;
		}
		else
		{
			initGame() ;
		}
	}

	@Override
	public Settings shutdownState()
	{
		unhookHandlerSystems() ;		// Prevent system from recieving external events
		clear() ;						// Remove all content
		return null ;
	}

	@Override
	public Settings pauseState()
	{
		unhookHandlerSystems() ;		// Prevent system from recieving external events
		paused = true ;
		return null ;
	}

	@Override
	public void update( final double _dt )
	{
		if( _dt < DEFAULT_ESCAPE_TIME && system != null )
		{
			currentUpdate.update( _dt ) ;
		}
	}

	/**
		Add entities at the most opportune moment.
		Should be used during game-logic update.
	*/
	public final void addEntities( final ArrayList<Entity> _entities )
	{
		for( final Entity entity : _entities )
		{
			addEntity( entity ) ;
		}
	}

	/**
		Add entities straight away.
		Should be used before the first game-logic update.
	*/
	public final void addEntitiesNow( final ArrayList<Entity> _entities )
	{
		for( final Entity entity : _entities )
		{
			addEntityNow( entity ) ;
		}
	}

	/**
		Add an entity at the most opportune moment.
		Should be used during game-logic update.
	*/
	public final void addEntity( final Entity _entity )
	{
		assert _entity != null ;
		entitySystem.addEntity( _entity ) ;
	}

	/**
		Add an entity straight away.
		Should be used before the first game-logic update.
	*/
	public final void addEntityNow( final Entity _entity )
	{
		assert _entity != null ;
		entitySystem.addEntityNow( _entity ) ;
	}

	/**
		Remove entities at the most opportune moment.
	*/
	public final void removeEntities( final ArrayList<Entity> _entities )
	{
		for( final Entity entity : _entities )
		{
			removeEntity( entity ) ;
		}
	}

	/**
		Remove an entity at the most opportune moment.
	*/
	public final void removeEntity( final Entity _entity )
	{
		assert _entity != null ;
		entitySystem.removeEntity( _entity ) ;
	}

	/**
		Hook Entity into relevant systems.
	*/
	public void hookEntity( final Entity _entity )
	{
		final ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;
		{
			// Retrieve component system-registering events.
			final ArrayList<Component> entityComponents = _entity.getAllComponents() ; 
			Component component = null ;
			final int size = entityComponents.size() ;
			for( int i = 0; i < size; ++i )
			{
				component = entityComponents.get( i ) ;
				component.passInitialEvents( events ) ;
			}
		}

		{
			final int size = events.size() ;
			for( int i = 0; i < size; i++ )
			{
				eventSystem.addEvent( events.get( i ) ) ;
			}
		}

		eventSystem.update() ;			// Update the Event System so other systems can process them asap.
	}

	/**
		Unhook Entity from systems.
	*/
	public void unhookEntity( final Entity _entity )
	{
		final ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;
		{
			// Retrieve component system-registering events.
			final ArrayList<Component> entityComponents = _entity.getAllComponents() ; 
			Component component = null ;
			final int size = entityComponents.size() ;
			for( int i = 0; i < size; ++i )
			{
				component = entityComponents.get( i ) ;
				component.passFinalEvents( events ) ;		// Retrieve the Events that will unregister the components
			}
		}

		{
			final int size = events.size() ;
			for( int i = 0; i < size; i++ )
			{
				eventSystem.addEvent( events.get( i ) ) ;
			}
		}

		_entity.clear() ;			// Unregister any Resources this Component may have acquired.
		eventSystem.update() ;		// Update the Event System so other systems can process them asap.
	}

	/**
		Force the Game State to call system.draw(), on next update.
		Not necessarily used by all UpdateInterface types.
	*/
	protected final void forceDraw()
	{
		draw = true ;
	}

	public final void setTimeStep( final int _timestep )
	{
		DEFAULT_TIMESTEP = 1.0f / _timestep ;
	}

	public final void setFrameRate( final int _framerate )
	{
		DEFAULT_FRAMERATE = 1.0f / _framerate ;
	}

	/**
		Provides access to the renderer.
		Audio source generator, inputs and the root event system.
	*/
	public final void setSystem( final SystemInterface _system )
	{
		system = _system ;
		audioSystem.setAudioGenerator( _system.getAudioGenerator() ) ;
	}

	/**
		Enable event-based systems to recieve events.
		Also hooks-up the inputSystem.
	*/
	protected void hookHandlerSystems()
	{
		eventSystem.addEventHandler( eventController ) ;
		eventSystem.addEventHandler( audioSystem ) ;
		eventSystem.addEventHandler( animationSystem ) ;
		eventSystem.addEventHandler( collisionSystem ) ;
		eventSystem.addEventHandler( system.getRenderInterface() ) ;

		system.addEventHandler( eventController ) ;
		system.addInputHandler( inputSystem ) ;
	}

	/**
		Prevent event-based system from recieving events.
		Important when state in not being used.
		Also unhooks the inputSystem.
	*/
	protected void unhookHandlerSystems()
	{
		eventSystem.removeEventHandler( eventController ) ;
		eventSystem.removeEventHandler( audioSystem ) ;
		eventSystem.removeEventHandler( animationSystem ) ;
		eventSystem.removeEventHandler( collisionSystem ) ;
		eventSystem.removeEventHandler( system.getRenderInterface() ) ;

		system.removeEventHandler( eventController ) ;
		system.removeInputHandler( inputSystem ) ;
	}

	/**
		Initialise the currentUpdate with the mode needed.
		Game State uses useGameMode by default.
	*/
	protected void initModes()
	{
		useGameMode() ;
	}

	protected void useGameMode()
	{
		currentUpdate = new UpdateInterface()
		{
			private final static long COMPENSATE_SLEEP = 40L ;		// We don't want to accidentally sleep too long
			private long runningTime = 0L ;

			@Override
			public void update( final double _dt )
			{
				// Update Default : 15Hz
				updateAccumulator += _dt ;
				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					final long startTime = ElapsedTimer.nanoTime() ;

					system.update( DEFAULT_TIMESTEP ) ;			// Update low-level systems
					inputSystem.update() ;
					eventSystem.update() ;

					eventController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;

					final long endTime = ElapsedTimer.nanoTime() ;
					runningTime += endTime - startTime ;
				}

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				if( renderAccumulator > DEFAULT_FRAMERATE )
				{
					final long startTime = ElapsedTimer.nanoTime() ;

					//System.out.println( ( int )( 1.0f / renderAccumulator ) ) ;
					animationSystem.update( DEFAULT_FRAMERATE ) ;
					system.draw( DEFAULT_FRAMERATE ) ;
					renderAccumulator = 0.0f ;

					final long endTime = ElapsedTimer.nanoTime() ;
					runningTime += endTime - startTime ;
				}

				if( runningTime > 0L )
				{
					final long sleep = ( ( ( long )( DEFAULT_TIMESTEP * 1000000000.0 ) - runningTime ) / 1000000L ) - COMPENSATE_SLEEP ;
					runningTime = 0L ;

					if( sleep > 0L )
					{
						system.sleep( sleep ) ;
					}
				}
			}
		} ;
	}

	protected void useApplicationMode()
	{
		currentUpdate = new UpdateInterface()
		{
			@Override
			public void update( final double _dt )
			{
				final boolean hasInput = inputSystem.hasInputs() ;
				final boolean hasEvents = eventSystem.hasEvents() ;
				if( hasInput == false && hasEvents == false )
				{
					system.sleep( DEFAULT_SLEEP ) ;
				}

				// Update Default : 15Hz
				updateAccumulator += _dt ;
				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					system.update( DEFAULT_TIMESTEP ) ;			// Update low-level systems
					inputSystem.update() ;
					eventSystem.update() ;

					eventController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				if( renderAccumulator > DEFAULT_FRAMERATE )
				{
					//System.out.println( ( int )( 1.0f / renderAccumulator ) ) ;
					animationSystem.update( DEFAULT_FRAMERATE ) ;
					system.draw( DEFAULT_FRAMERATE ) ;
					renderAccumulator = 0.0f ;
				}
			}
		} ;
	}

	protected void initEventProcessors()
	{
		eventController.addEventProcessor( new EventProcessor<InputHandler>( "ADD_GAME_STATE_INPUT", "ADD_GAME_STATE_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputSystem.addInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<InputHandler>( "REMOVE_GAME_STATE_INPUT", "REMOVE_GAME_STATE_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputSystem.removeInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<EventController>( "ADD_GAME_STATE_EVENT", "ADD_GAME_STATE_EVENT" )
		{
			public void processEvent( final Event<EventController> _event )
			{
				final EventController controller = _event.getVariable() ;
				controller.setAddEventInterface( eventSystem ) ;
				eventSystem.addEventHandler( controller ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<EventController>( "REMOVE_GAME_STATE_EVENT", "REMOVE_GAME_STATE_EVENT" )
		{
			public void processEvent( final Event<EventController> _event )
			{
				final EventController controller = _event.getVariable() ;
				controller.setAddEventInterface( null ) ;
				eventSystem.removeEventHandler( controller ) ;
			}
		} ) ;
		
		eventController.addEventProcessor( new EventProcessor<QueryComponent>( "ADD_GAME_STATE_QUERY", "ADD_GAME_STATE_QUERY" )
		{
			public void processEvent( final Event<QueryComponent> _event )
			{
				final QueryComponent query = _event.getVariable() ;
				query.setSearch( entitySystem.getSearch() ) ;
			}
		} ) ;
	}

	/**
		Guarantees that all systems the state uses will be blank.
	*/
	protected void clear()
	{
		eventSystem.clearEvents() ;
		eventSystem.clearHandlers() ;

		inputSystem.clearInputs() ;
		inputSystem.clearHandlers() ;

		audioSystem.clear() ;
		entitySystem.clear() ;
		animationSystem.clear() ;
		system.getRenderInterface().clear() ;
	}

	/**
		Allows the developer to create their own update modes.
	*/
	protected interface UpdateInterface
	{
		public void update( final double _dt ) ;
	}
}