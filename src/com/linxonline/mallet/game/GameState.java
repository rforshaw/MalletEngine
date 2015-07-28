package com.linxonline.mallet.game ;

import java.util.ArrayList ;

import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.game.statemachine.State ;

import com.linxonline.mallet.input.InputSystemInterface ;
import com.linxonline.mallet.input.InputHandler ;
import com.linxonline.mallet.input.InputState ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventSystem ;
import com.linxonline.mallet.event.EventProcessor ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.EventSystemInterface ;

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

import com.linxonline.mallet.io.save.state.DataConverter ;

public class GameState extends State implements HookEntity
{
	protected float DEFAULT_TIMESTEP = 1.0f / 15.0f ;					// 15Hz
	protected float DEFAULT_FRAMERATE = 1.0f / 60.0f ;					// 60Hz
	protected float DEFAULT_ESCAPE_TIME = 0.25f ;						// Escape threshold, if delta spirals out of control with no way to catchup
	protected long DEFAULT_SLEEP = 10L ;								// Duration to sleep before continuing update cycle

	protected UpdateInterface currentUpdate = null ;												// Current Running Mode

	protected final InputState inputWorldSystem = new InputState() ;										// Internal World Input System
	protected final InputState inputUISystem = new InputState() ;											// Internal UI Input System
	protected final EventSystem eventSystem = new EventSystem( "GAME_STATE_EVENT_SYSTEM" ) ;				// Internal Event System
	protected final EventController eventController = new EventController( "GAME_STATE_CONTROLLER" ) ;		// Used to process Events, gateway between internal eventSystem and root event-system

	protected SystemInterface system = null ;																// Provides access to Root systems
	protected final AudioSystem audioSystem = new AudioSystem() ;											// Must specify a SourceGenerator
	protected final EntitySystemInterface entitySystem = new EntitySystem( this ) ;
	protected final AnimationSystem animationSystem = new AnimationSystem( eventSystem ) ;
	protected final CollisionSystem collisionSystem = new CollisionSystem( eventSystem ) ;

	protected final DataConverter dataTracker = new DataConverter() ;	// Track current data that you wish to save/read

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

		hookGameStateEventController() ;
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
		Called by startState once the game has been 
		initialised or resumed.
		Ensures that any EventProcessors added to 
		the controller are handled correctly by the 
		Event Systems.
	*/
	protected void hookGameStateEventController()
	{
		eventSystem.addEventHandler( eventController ) ;
		system.getEventInterface().addEventHandler( eventController ) ;
	}
	
	/**
		Enable event-based systems to recieve events.
		Also hooks-up the inputSystem.
	*/
	protected void hookHandlerSystems()
	{
		eventSystem.addEventHandler( dataTracker.getEventController() ) ;
		eventSystem.addEventHandler( audioSystem ) ;
		eventSystem.addEventHandler( animationSystem ) ;
		eventSystem.addEventHandler( collisionSystem ) ;
		eventSystem.addEventHandler( system.getRenderInterface() ) ;

		final InputSystemInterface input = system.getInputInterface() ;
		input.addInputHandler( inputUISystem ) ;
		input.addInputHandler( inputWorldSystem ) ;
	}

	/**
		Prevent event-based system from recieving events.
		Important when state in not being used.
		Also unhooks the inputSystem.
	*/
	protected void unhookHandlerSystems()
	{
		eventSystem.removeEventHandler( dataTracker.getEventController() ) ;
		eventSystem.removeEventHandler( eventController ) ;
		eventSystem.removeEventHandler( audioSystem ) ;
		eventSystem.removeEventHandler( animationSystem ) ;
		eventSystem.removeEventHandler( collisionSystem ) ;
		eventSystem.removeEventHandler( system.getRenderInterface() ) ;

		final InputSystemInterface input = system.getInputInterface() ;
		input.removeInputHandler( inputUISystem ) ;
		input.removeInputHandler( inputWorldSystem ) ;
	}

	/**
		Initialise the currentUpdate with the mode needed.
		Game State uses useGameMode by default.
	*/
	protected void initModes()
	{
		useGameMode() ;
		//useApplicationMode() ;
	}

	protected void useGameMode()
	{
		currentUpdate = new UpdateInterface()
		{
			private long logicRunningTime = 0L ;
			private long renderRunningTime = 0L ;

			@Override
			public void update( final double _dt )
			{
				// Update Default : 15Hz
				updateAccumulator += _dt ;
				long startTime = ElapsedTimer.nanoTime() ;

				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					system.update( DEFAULT_TIMESTEP ) ;			// Update low-level systems
					inputUISystem.update() ;
					inputWorldSystem.update() ;
					eventSystem.update() ;

					dataTracker.update() ;
					eventController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				// Track the logic running time, not used to calculate 
				// sleep duration, but useful information for developer
				// to see if logic goes over allocated time.
				long endTime = ElapsedTimer.nanoTime() ;
				logicRunningTime = endTime - startTime ;		// In nanoseconds

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				startTime = ElapsedTimer.nanoTime() ;

				if( renderAccumulator > DEFAULT_FRAMERATE )
				{
					//System.out.println( ( int )( 1.0f / _dt ) ) ;
					animationSystem.update( DEFAULT_FRAMERATE ) ;
					system.draw( DEFAULT_FRAMERATE ) ;
					renderAccumulator -= DEFAULT_FRAMERATE ;
				}

				endTime = ElapsedTimer.nanoTime() ;
				renderRunningTime = endTime - startTime ;		// In nanoseconds

				final float deltaLogic = logicRunningTime * 0.000000001f ;							// Convert to seconds
				final float deltaRender = renderRunningTime * 0.000000001f ;						// Convert to seconds
				
				// If the time taken to render the current frame and update the delta logic
				// is less than the total time allocated for rendering a frame, 
				// then we can risk sleeping for a short duration.
				final long sleepRender = ( long )( ( DEFAULT_FRAMERATE - ( deltaRender + deltaLogic ) ) * 1000.0f ) ;	// Convert to milliseconds

				if( sleepRender > 0L )
				{
					system.sleep( sleepRender ) ;
				}
			}
		} ;
	}

	protected void useApplicationMode()
	{
		currentUpdate = new UpdateInterface()
		{
			private long logicRunningTime = 0L ;
			private long renderRunningTime = 0L ;

			@Override
			public void update( final double _dt )
			{
				long startTime = ElapsedTimer.nanoTime() ;

				// Update Default : 15Hz
				updateAccumulator += _dt ;

				// Update the system to ensure that the state 
				// has the latest events and inputs.
				system.update( DEFAULT_TIMESTEP ) ;						// Update low-level systems
				final boolean hasInputs = inputWorldSystem.hasInputs() || inputWorldSystem.hasInputs() ;
				final boolean hasEvents = eventSystem.hasEvents() ;

				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					inputUISystem.update() ;
					inputWorldSystem.update() ;
					eventSystem.update() ;

					dataTracker.update() ;
					eventController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				// Track the logic running time, not used to calculate 
				// sleep duration, but useful information for developer
				// to see if logic goes over allocated time.
				long endTime = ElapsedTimer.nanoTime() ;
				logicRunningTime = endTime - startTime ;		// In nanoseconds

				if( hasInputs == false && hasEvents == false )
				{
					// Rendering consumes the greatest amount of resources 
					// and processing time, if the user has not interacted 
					// with the application, and there are no events 
					// needing passed, then don't refresh the screen.
					system.sleep( ( long )( DEFAULT_FRAMERATE * 1000.0f ) ) ;
					return ;
				}

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				startTime = ElapsedTimer.nanoTime() ;

				//System.out.println( ( int )( 1.0f / _dt ) ) ;
				animationSystem.update( DEFAULT_FRAMERATE ) ;
				system.draw( DEFAULT_FRAMERATE ) ;
				renderAccumulator -= DEFAULT_FRAMERATE ;

				endTime = ElapsedTimer.nanoTime() ;
				renderRunningTime = endTime - startTime ;		// In nanoseconds

				final float deltaLogic = logicRunningTime * 0.000000001f ;								// Convert to seconds
				final float deltaRender = renderRunningTime * 0.000000001f ;							// Convert to seconds
				final long sleepRender = ( long )( ( DEFAULT_FRAMERATE - ( deltaRender + deltaLogic ) ) * 1000.0f ) ;	// Convert to milliseconds

				if( sleepRender > 0L )
				{
					// Even after rendering there is still a chance that 
					// we can sleep before we need to refresh the screen
					// again.
					system.sleep( sleepRender ) ;
				}
			}
		} ;
	}

	protected void initEventProcessors()
	{
		eventController.addEventProcessor( new EventProcessor<InputHandler>( "ADD_GAME_STATE_INPUT", "ADD_GAME_STATE_UI_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputUISystem.addInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<InputHandler>( "REMOVE_GAME_STATE_INPUT", "REMOVE_GAME_STATE_UI_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputUISystem.removeInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<InputHandler>( "ADD_GAME_STATE_INPUT", "ADD_GAME_STATE_WORLD_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputWorldSystem.addInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<InputHandler>( "REMOVE_GAME_STATE_INPUT", "REMOVE_GAME_STATE_WORLD_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputWorldSystem.removeInputHandler( _event.getVariable() ) ;
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

		inputUISystem.clearInputs() ;
		inputUISystem.clearHandlers() ;

		inputWorldSystem.clearInputs() ;
		inputWorldSystem.clearHandlers() ;

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