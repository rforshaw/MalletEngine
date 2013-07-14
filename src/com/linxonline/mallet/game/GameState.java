package com.linxonline.mallet.game ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.audio.alsa.* ;

import com.linxonline.mallet.game.statemachine.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.system.* ;
import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.physics.* ;
import com.linxonline.mallet.animation.* ;

import com.linxonline.mallet.util.factory.creators.* ;
import com.linxonline.mallet.util.factory.EntityFactory ;

public class GameState extends State implements HookEntity
{
	public static final int GAME_MODE = 1 ;								// Update Logic & Render on a recurring basis
	public static final int APPLICATION_MODE = 2 ;						// Update on logic & Render on user-input

	protected float DEFAULT_TIMESTEP = 1.0f / 120.0f ;
	protected float DEFAULT_FRAMERATE = 1.0f / 60.0f ;
	protected float DEFAULT_ESCAPE_TIME = 0.25f ;

	protected final HashMap<Integer, UpdateInterface> updateModes = new HashMap<Integer, UpdateInterface>() ;
	protected UpdateInterface currentUpdate = null ;						// Current Running Mode

	protected final InputState inputSystem = new InputState() ;					// Internal Input System
	protected final EventSystem eventSystem = new EventSystem() ;				// Internal Event System
	protected final EventController eventController = new EventController() ;	// Used to process Events

	protected SystemInterface system = null ;									// Provides access to Root systems
	protected final AudioSystem audioSystem = new AudioSystem() ;				// Must specify a SourceGenerator
	protected final AnimationSystem animationSystem = new AnimationSystem( eventSystem ) ;
	protected final EntitySystem entitySystem = new EntitySystem( this ) ;
	protected final CollisionSystem collisionSystem = new CollisionSystem() ;

	protected boolean draw = true ;										// Used to force a Draw
	protected double updateAccumulator = 0.0f ;							// Current dt update
	protected double renderAccumulator = 0.0f ;							// Current dt render

	public GameState( final String _name )
	{
		super( _name ) ;
		initModes() ;
	}

	/**
		Initialise the Game related content here.
		Load resources, entities, etc.
	**/
	public void initGame() {}

	@Override
	public void startState( final Settings _package )
	{
		hookHandlerSystems() ;
		initGame() ;
	}

	@Override
	public Settings shutdownState()
	{
		system.clear() ;
		system.clearInputs() ;
		inputSystem.clearInputs() ;
		unhookHandlerSystems() ;
		return null ;
	}

	@Override
	public Settings pauseState()
	{
		unhookHandlerSystems() ;
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

	public final void addEntities( final ArrayList<Entity> _entities )
	{
		for( final Entity entity : _entities )
		{
			addEntity( entity ) ;
		}
	}

	public final void addEntitiesNow( final ArrayList<Entity> _entities )
	{
		for( final Entity entity : _entities )
		{
			addEntityNow( entity ) ;
		}
	}

	public final void removeEntities( final ArrayList<Entity> _entities )
	{
		for( final Entity entity : _entities )
		{
			removeEntity( entity ) ;
		}
	}

	public final void addEntity( final Entity _entity )
	{
		entitySystem.addEntity( _entity ) ;
	}

	public final void addEntityNow( final Entity _entity )
	{
		entitySystem.addEntityNow( _entity ) ;
	}

	public final void removeEntity( final Entity _entity )
	{
		entitySystem.removeEntity( _entity ) ;
	}

	/**
		Hook Entity into relevant systems.
	**/
	public void hookEntity( final Entity _entity )
	{
		ArrayList<Component> components = null ;

		components = _entity.getComponentByGroup( "COLLISIONCOMPONENT" ) ;
		for( Component comp : components )
		{
			CollisionComponent coll = ( CollisionComponent )comp ;
			collisionSystem.add( coll.hull ) ;
		}

		components = _entity.getComponentByGroup( "INPUTCOMPONENT" ) ;
		for( Component comp : components )
		{
			InputHandler component = ( InputHandler )comp ;
			inputSystem.addInputHandler( component ) ;
		}

		components = _entity.getComponentByGroup( "EVENTCOMPONENT" ) ;
		for( Component comp : components )
		{
			final EventComponent e = ( EventComponent )comp ;
			final EventController controller = e.getEventController() ;
			controller.setAddEventInterface( eventSystem ) ;
			eventSystem.addEventHandler( controller ) ;

			e.sendInitialEvents() ;
		}

		components = _entity.getComponentByGroup( "QUERYCOMPONENT" ) ;
		for( Component comp : components )
		{
			QueryComponent component = ( QueryComponent )comp ;
			component.setSearch( entitySystem.getSearch() ) ;
		}

		// Update the Event System so other systems can process them asap.
		eventSystem.update() ;
	}

	/**
		Unhook Entity from systems.
	**/
	public void unhookEntity( final Entity _entity )
	{
		ArrayList<Component> components = null ;

		components = _entity.getComponentByGroup( "COLLISIONCOMPONENT" ) ;
		for( Component comp : components )
		{
			CollisionComponent coll = ( CollisionComponent )comp ;
			collisionSystem.remove( coll.hull ) ;
		}

		components = _entity.getComponentByGroup( "INPUTCOMPONENT" ) ;
		for( Component comp : components )
		{
			InputHandler component = ( InputHandler )comp ;
			inputSystem.removeInputHandler( component ) ;
		}

		components = _entity.getComponentByGroup( "EVENTCOMPONENT" ) ;
		for( Component comp : components )
		{
			final EventComponent e = ( EventComponent )comp ;
			e.sendFinishEvents() ;

			final EventController controller = e.getEventController() ;
			controller.setAddEventInterface( null ) ;
			eventSystem.removeEventHandler( controller ) ;
		}

		components = _entity.getComponentByGroup( "QUERYCOMPONENT" ) ;
		for( Component comp : components )
		{
			QueryComponent component = ( QueryComponent )comp ;
			component.setSearch( null ) ;
		}

		// Unregister any Resources this Component may have acquired.
		_entity.clear() ;

		// Update the Event System so other systems can process them asap.
		eventSystem.update() ;
	}

	/**
		Force the Game State to call system.draw(), on next update.
		Not necessarily used by all UpdateInterface types.
	**/
	protected final void forceDraw()
	{
		draw = true ;
	}

	/**
		Informs the Game State which Mode it is running in.
	**/
	public final void setMode( final int _mode )
	{
		final Integer mode = new Integer( _mode ) ;
		if( updateModes.containsKey( mode ) == true )
		{
			currentUpdate = updateModes.get( mode ) ;
		}
	}

	public final void setTimeStep( final int _timestep )
	{
		DEFAULT_TIMESTEP = 1.0f / _timestep ;
	}

	public final void setFrameRate( final int _framerate )
	{
		DEFAULT_FRAMERATE = 1.0f / _framerate ;
	}

	public final void setSystem( final SystemInterface _system )
	{
		system = _system ;
		audioSystem.setSourceGenerator( _system.getSourceGenerator() ) ;
	}

	/**
		Hook-up the rellevant handlers to the appropriate systems.
		GameState.inputSystem to System.inputSystem
		GameState to GameState.eventSystem
		GameState to System.eventSystem
	**/
	protected void hookHandlerSystems()
	{
		eventSystem.addEventHandler( eventController ) ;
		eventSystem.addEventHandler( audioSystem ) ;
		eventSystem.addEventHandler( animationSystem ) ;
		eventSystem.addEventHandler( system.getRenderInterface() ) ;

		system.addEventHandler( eventController ) ;
		system.addInputHandler( inputSystem ) ;
	}

	/**
		unHook the rellevant handlers from the appropriate systems.
		GameState.inputSystem from System.inputSystem
		GameState from GameState.eventSystem
		GameState from System.eventSystem
	**/
	protected void unhookHandlerSystems()
	{
		eventSystem.removeEventHandler( eventController ) ;
		eventSystem.removeEventHandler( system.getRenderInterface() ) ;
		eventSystem.removeHandlersNow() ;

		system.removeEventHandler( eventController ) ;
		system.removeInputHandler( inputSystem ) ;
	}

	/**
		Initialise the default modes: GAME_MODE and APPLICATION_MODE
		currentUpdate = GAME_MODE, by default.
	**/
	protected void initModes()
	{
		UpdateInterface gameUpdate = new UpdateInterface()
		{
			@Override
			public void update( final double _dt )
			{
				inputSystem.update() ;
				eventSystem.update() ;

				// Update Default : 120Hz
				updateAccumulator += _dt ;
				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					//System.out.println( 1.0f / updateAccumulator ) ;
					eventController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					animationSystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				if( renderAccumulator > DEFAULT_FRAMERATE )
				{
					//System.out.println( 1.0f / renderAccumulator ) ;
					system.draw() ;
					renderAccumulator = 0.0f ;//-= DEFAULT_FRAMERATE ;
				}
			}
		} ;
		updateModes.put( GAME_MODE, gameUpdate ) ;

		UpdateInterface applicationUpdate = new UpdateInterface()
		{
			@Override
			public void update( final double _dt )
			{
				final boolean hasInput = inputSystem.hasInputs() ;
				final boolean hasEvents = eventSystem.hasEvents() ;

				// Update as fast as the computer can manage.
				inputSystem.update() ;
				eventSystem.update() ;

				// Update Default : 120Hz
				updateAccumulator += _dt ;
				if( hasEvents == true || hasInput == true || draw == true )
				{
					//System.out.println( 1.0f / updateAccumulator ) ;
					final float dt = ( float )updateAccumulator ;
					eventController.update() ;

					audioSystem.update( dt ) ;
					collisionSystem.update( dt ) ;
					entitySystem.update( dt ) ;
					updateAccumulator = 0.0f ;

					system.draw() ;
					draw = false ;
				}
			}
		} ;

		updateModes.put( APPLICATION_MODE, applicationUpdate ) ;
		setMode( GAME_MODE ) ;
	}

	/**
		Allows the developer to create their own update modes.
	**/
	protected interface UpdateInterface
	{
		public void update( final double _dt ) ;
	}
}