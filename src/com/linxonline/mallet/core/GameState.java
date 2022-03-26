package com.linxonline.mallet.core ;

import java.util.List ;

import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.TextDraw ;
import com.linxonline.mallet.renderer.TextUpdater ;
import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.statemachine.State ;

import com.linxonline.mallet.input.IInputSystem ;
import com.linxonline.mallet.input.InputHandler ;
import com.linxonline.mallet.input.InputState ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventSystem ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.InterceptController ;
import com.linxonline.mallet.event.IEventSystem ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.physics.CollisionSystem ;

import com.linxonline.mallet.animation.AnimationSystem ;
import com.linxonline.mallet.animation.AnimationAssist ;

import com.linxonline.mallet.entity.IEntitySystem ;
import com.linxonline.mallet.entity.EntitySystem ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.Component ;

import com.linxonline.mallet.util.time.ElapsedTimer ;
import com.linxonline.mallet.util.Threaded ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.thread.* ;

public class GameState extends State
{
	protected float DEFAULT_TIMESTEP = 1.0f / 15.0f ;					// 15Hz
	protected float DEFAULT_FRAMERATE = 1.0f / 60.0f ;					// 60Hz
	protected float DEFAULT_ESCAPE_TIME = 0.25f ;						// Escape threshold, if delta spirals out of control with no way to catchup
	protected long DEFAULT_SLEEP = 10L ;								// Duration to sleep before continuing update cycle

	private final IUpdate updater ;													// Current Running Mode
	private final List<IUpdate> mainUpdaters = MalletList.<IUpdate>newList() ;		// Updaters that are triggered on DEFAULT_TIMESTEP
	private final List<IUpdate> drawUpdaters = MalletList.<IUpdate>newList() ;		// Updaters that are triggered on DEFAULT_FRAMERATE

	protected final InputState inputWorldSystem = new InputState() ;			// Internal World Input System
	protected final InputState inputUISystem = new InputState() ;				// Internal UI Input System
	protected final EventSystem eventSystem = new EventSystem() ;				// Internal Event System

	private final InterceptController interceptController = new InterceptController() ;
	private final EventController internalController = new EventController() ;		// Used to process Events, from internal eventSystem
	private final EventController externalController = new EventController() ;		// Used to process Events, from external eventSystem

	protected ISystem system = null ;																// Provides access to Root systems
	protected final IEntitySystem entitySystem ;
	protected final CollisionSystem collisionSystem ;

	protected final AudioSystem audioSystem = new AudioSystem() ;
	protected final AnimationSystem animationSystem = new AnimationSystem() ;

	protected boolean paused = false ;									// Determine whether state was paused.
	protected boolean draw = true ;										// Used to force a Draw
	protected double updateAccumulator = 0.0f ;							// Current dt update
	protected double renderAccumulator = 0.0f ;							// Current dt render

	private final ShowFPS showFPS = new ShowFPS() ;

	public GameState( final String _name )
	{
		this( _name, Threaded.MULTI, UpdateMode.GAME ) ;
	}

	public GameState( final String _name, final UpdateMode _mode )
	{
		this( _name, Threaded.MULTI, _mode ) ;
	}

	public GameState( final String _name, final Threaded _type )
	{
		this( _name, _type, UpdateMode.GAME ) ;
	}
	
	public GameState( final String _name, final Threaded _type, final UpdateMode _mode )
	{
		super( _name ) ;
		switch( _type )
		{
			default     :
			case SINGLE :
			{
				entitySystem = new EntitySystem( eventSystem ) ;
				collisionSystem = new CollisionSystem( eventSystem ) ; 
				break ;
			}
			case MULTI  :
			{
				final WorkerGroup workers = new WorkerGroup( "SHARED_WORKERS", 4 ) ;
				entitySystem = new EntitySystem( eventSystem, workers ) ;
				collisionSystem = new CollisionSystem( eventSystem, workers ) ;
				break ;
			}
		}

		updater = createCoreUpdate( _mode ) ;
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

	/**
		The user should not have to override this method.
		Override initGame and resumeGame instead.
	*/
	@Override
	public void startState( final Settings _package )
	{
		AnimationAssist.setAssist( animationSystem.createAnimationAssist() ) ;
		hookHandlerSystems() ;

		if( paused == true )
		{
			paused = false ;
			resumeGame() ;
			audioSystem.resumeSystem() ;
		}
		else
		{
			createUpdaters( mainUpdaters, drawUpdaters ) ;
			initGame() ;
		}
		
		// Event processors need to be called last 
		// in case developer adds more during initGame or resumeGame.
		initEventProcessors( internalController, externalController, interceptController ) ;
		hookGameStateEventController() ;
	}

	/**
		Cleanup state resources and perhaps save 
		any required persistant data.
	*/
	@Override
	public Settings shutdownState()
	{
		clear() ;								// Remove all content
		unhookHandlerSystems() ;				// Prevent system from recieving external events
		unhookGameStateEventController() ;

		showFPS.setShow( false ) ;
		return null ;
	}

	/**
		The state will no longer be updated until 
		startState is called again.
		Cleanup unused resources and perhaps save 
		persistant data.
		The state shall not recieve any events or inputs 
		while paused.
	*/
	@Override
	public Settings pauseState()
	{
		unhookHandlerSystems() ;				// Prevent system from recieving external events
		unhookGameStateEventController() ;
		audioSystem.pauseSystem() ;

		showFPS.setShow( false ) ;
		paused = true ;
		return null ;
	}

	@Override
	public void update( final double _dt )
	{
		if( _dt < DEFAULT_ESCAPE_TIME && system != null )
		{
			updater.update( _dt ) ;
		}
	}

	/**
		Add entities at the most opportune moment.
		Should be used during game-logic update.
	*/
	public final void addEntities( final List<Entity> _entities )
	{
		final int size = _entities.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Entity entity = _entities.get( i ) ;
			addEntity( entity ) ;
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
		Remove entities at the most opportune moment.
	*/
	public final void removeEntities( final List<Entity> _entities )
	{
		final int size = _entities.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Entity entity = _entities.get( i ) ;
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
		Force the Game State to call system.draw(), on next update.
		Not necessarily used by all IUpdate types.
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
	public final void setSystem( final ISystem _system )
	{
		system = _system ;
		audioSystem.setGenerator( _system.getAudioGenerator() ) ;

		final ISystem.ShutdownDelegate shutdown = _system.getShutdownDelegate() ;
		shutdown.addShutdownCallback( () ->
		{
			audioSystem.clear() ;
		} ) ;
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
		eventSystem.setIntercept( interceptController ) ;

		eventSystem.addHandler( internalController ) ;
		internalController.setAddEventInterface( eventSystem ) ;

		system.getEventSystem().addHandler( externalController ) ;
		externalController.setAddEventInterface( system.getEventSystem() ) ;
	}

	protected void unhookGameStateEventController()
	{
		eventSystem.setIntercept( null ) ;

		eventSystem.removeHandler( internalController ) ;
		system.getEventSystem().removeHandler( externalController ) ;
	}

	/**
		Enable event-based systems to recieve events.
		Also hooks-up the inputSystem.
	*/
	protected void hookHandlerSystems()
	{
		final EventController audioController = audioSystem.getEventController() ;
		final EventController collisionController = collisionSystem.getEventController() ;

		eventSystem.addHandler( audioController ) ;
		eventSystem.addHandler( collisionController ) ;
		eventSystem.addHandler( system.getRenderer().getEventController() ) ;

		audioController.setAddEventInterface( eventSystem ) ;

		final IInputSystem input = system.getInput() ;
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
		final EventController audioController = audioSystem.getEventController() ;

		eventSystem.removeHandler( audioController ) ;
		eventSystem.removeHandler( collisionSystem.getEventController() ) ;
		eventSystem.removeHandler( system.getRenderer().getEventController() ) ;

		audioController.setAddEventInterface( null ) ;

		final IInputSystem input = system.getInput() ;
		input.removeInputHandler( inputUISystem ) ;
		input.removeInputHandler( inputWorldSystem ) ;
	}

	/**
		Create the intended update mode for the gamestate.
	*/
	protected IUpdate createCoreUpdate( final UpdateMode _mode )
	{
		mainUpdaters.add( ( final double _dt ) ->
		{
			system.update( DEFAULT_TIMESTEP ) ;			// Update low-level systems
			inputUISystem.update() ;
			inputWorldSystem.update() ;

			eventSystem.sendEvents() ;
			internalController.update() ;
			externalController.update() ;

			collisionSystem.update( DEFAULT_TIMESTEP ) ;
			entitySystem.update( DEFAULT_TIMESTEP ) ;
			audioSystem.update( DEFAULT_TIMESTEP ) ;
		} ) ;

		drawUpdaters.add( ( final double _dt ) ->
		{
			system.getInput().update() ;
			inputUISystem.update() ;
			inputWorldSystem.update() ;

			animationSystem.update( DEFAULT_FRAMERATE ) ;
			system.draw( DEFAULT_FRAMERATE ) ;
		} ) ;

		switch( _mode )
		{
			default          :
			case GAME        : return useGameMode() ;
			case APPLICATION : return useApplicationMode() ;
		}
	}

	/**
		Allow the user to attach further core systems to the
		game-states update cycle.
	*/
	protected void createUpdaters( final List<IUpdate> _main, final List<IUpdate> _draw ) {}

	protected IUpdate useGameMode()
	{
		return new IUpdate()
		{
			private double deltaUpdateTime = 0.0 ;
			private double deltaRenderTime = 0.0 ;

			@Override
			public void update( final double _dt )
			{
				long startTime = ElapsedTimer.nanoTime() ;
				// Update Default : 15Hz
				updateAccumulator += _dt ;

				while( updateAccumulator >= DEFAULT_TIMESTEP )
				{
					for( IUpdate update : mainUpdaters )
					{
						update.update( DEFAULT_TIMESTEP ) ;
					}

					showFPS.update( deltaRenderTime, deltaUpdateTime ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				long endTime = ElapsedTimer.nanoTime() ;

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				deltaUpdateTime += ( endTime - startTime ) * 0.000000001 ;

				//System.out.println( "Acc: " + renderAccumulator + " FPS: " + DEFAULT_FRAMERATE ) ;
				if( renderAccumulator >= DEFAULT_FRAMERATE )
				{
					startTime = ElapsedTimer.nanoTime() ;

					for( IUpdate update : drawUpdaters )
					{
						update.update( DEFAULT_FRAMERATE ) ;
					}

					endTime = ElapsedTimer.nanoTime() ;

					deltaRenderTime = ( endTime - startTime ) * 0.000000001 ;
					renderAccumulator = 0.0 ;

					// After rendering see if we have any spare time to sleep.
					// Let's not waste CPU resources if we can avoid it.
					final float accumulatedTime = ( float )( deltaUpdateTime + deltaRenderTime ) ;
					// Ensure that the accumulated time for update and drawing is less than
					// half our draw rate.
					if( accumulatedTime < ( DEFAULT_FRAMERATE * 0.5f) )
					{
						// Sleep for a quarter of the leftover time, good
						// chance we don't drop frames.
						final long sleep = ( long )( ( DEFAULT_FRAMERATE - accumulatedTime ) * 0.25f * 1000.0f ) ;
						system.sleep( sleep ) ;
					}

					deltaUpdateTime = 0.0 ;
				}
			}
		} ;
	}

	protected IUpdate useApplicationMode()
	{
		return new IUpdate()
		{
			private float waitDelay = 2.0f ;
			private float wait = 0.0f ;
			
			@Override
			public void update( final double _dt )
			{
				final long startTime = ElapsedTimer.nanoTime() ;

				// Update Default : 15Hz
				updateAccumulator += _dt ;

				// Update the system to ensure that the state 
				// has the latest events and inputs.
				system.update( DEFAULT_TIMESTEP ) ;						// Update low-level systems
				final boolean hasInputs = inputWorldSystem.hasInputs() || inputUISystem.hasInputs() ;
				final boolean hasEvents = eventSystem.hasEvents() ;
				wait += ( hasInputs == true || hasEvents == true ) ? -wait : _dt ;

				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					for( IUpdate update : mainUpdaters )
					{
						update.update( _dt ) ;
					}
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				if( hasInputs == false && hasEvents == false && wait > waitDelay )
				{
					// Rendering consumes the greatest amount of resources 
					// and processing time, if the user has not interacted 
					// with the application, and there are no events 
					// needing passed, then don't refresh the screen.
					system.sleep( ( long )( DEFAULT_FRAMERATE * 1000.0f ) ) ;
				}

				// Render Default : 60Hz
				renderAccumulator += _dt ;

				showFPS.update( _dt, _dt ) ;
				for( IUpdate update : drawUpdaters )
				{
					update.update( _dt ) ;
				}

				renderAccumulator -= DEFAULT_FRAMERATE ;

				final long endTime = ElapsedTimer.nanoTime() ;
				final long runTime = endTime - startTime ;		// In nanoseconds

				final float deltaTime = runTime * 0.000000001f ;									// Convert to seconds
				final long sleepRender = ( long )( ( DEFAULT_FRAMERATE - deltaTime ) * 1000.0f ) ;	// Convert to milliseconds

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

	protected void initEventProcessors( final EventController _internal, final EventController _external, final InterceptController _intercept )
	{
		_internal.addProcessor( "ADD_GAME_STATE_UI_INPUT", ( final InputHandler _handler ) ->
		{
			inputUISystem.addInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_GAME_STATE_UI_INPUT", ( final InputHandler _handler ) ->
		{
			inputUISystem.removeInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "ADD_GAME_STATE_WORLD_INPUT", ( final InputHandler _handler ) ->
		{
			inputWorldSystem.addInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_GAME_STATE_WORLD_INPUT", ( final InputHandler _handler ) ->
		{
			inputWorldSystem.removeInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "ADD_GAME_STATE_EVENT", ( final EventController _controller ) ->
		{
			_controller.setAddEventInterface( eventSystem ) ;
			eventSystem.addHandler( _controller ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_GAME_STATE_EVENT", ( final EventController _controller ) ->
		{
			eventSystem.removeHandler( _controller ) ;
		} ) ;

		_internal.addProcessor( "ADD_BACKEND_EVENT", ( final EventController _controller ) ->
		{
			final IEventSystem eventBackend = system.getEventSystem() ;

			_controller.setAddEventInterface( eventBackend ) ;
			eventBackend.addHandler( _controller ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_BACKEND_EVENT", ( final EventController _controller ) ->
		{
			final IEventSystem eventBackend = system.getEventSystem() ;
			eventBackend.removeHandler( _controller ) ;
		} ) ;

		_internal.addProcessor( "SHOW_GAME_STATE_FPS", new EventController.IProcessor<Boolean>()
		{
			public void process( final Boolean _show )
			{
				final boolean show = _show ;
				if( show == showFPS.toShow() )
				{
					// If they are setting it to the same value it 
					// currently is don't do anything.
					return ;
				}

				showFPS.setShow( show ) ;
			}
		} ) ;
	}

	public EventController getInternalController()
	{
		return internalController ;
	}

	public EventController getExternalController()
	{
		return externalController ;
	}

	/**
		Guarantees that all systems the state uses will be blank.
	*/
	protected void clear()
	{
		internalController.reset() ;
		externalController.reset() ;
		eventSystem.reset() ;

		inputUISystem.clearInputs() ;
		inputUISystem.clearHandlers() ;

		inputWorldSystem.clearInputs() ;
		inputWorldSystem.clearHandlers() ;

		audioSystem.clear() ;
		entitySystem.clear() ;
		animationSystem.clear() ;
	}

	public enum UpdateMode
	{
		APPLICATION,
		GAME
	} ;

	/**
		Allows the developer to create their own update modes.
	*/
	public interface IUpdate
	{
		public void update( final double _dt ) ;
	}

	private static class ShowFPS
	{
		private boolean show = false ;
		private final TextDraw[] draws = new TextDraw[2] ;
		private final TextUpdater updater ;

		public ShowFPS()
		{
			final World world = WorldAssist.getDefault() ;

			final Program program = ProgramAssist.add( new Program( "SIMPLE_FONT" ) ) ;
			program.mapUniform( "inTex0", new MalletFont( "Arial" ) ) ;

			draws[0] = new TextDraw( "0" ) ;
			draws[0].setHidden( !show ) ;

			draws[1] = new TextDraw( "0" ) ;
			draws[1].setPosition( 0.0f, 20.0f, 0.0f ) ;
			draws[1].setHidden( !show ) ;

			updater = TextUpdater.getOrCreate( world, program, true, Integer.MAX_VALUE ) ;
			updater.addDynamics( draws ) ;
		}

		public void setShow( final boolean _show )
		{
			show = _show ;
			draws[0].setHidden( !show ) ;
			draws[1].setHidden( !show ) ;
			updater.forceUpdate() ;
		}

		public boolean toShow()
		{
			return show ;
		}

		public void update( final double _dtRender, final double _dtUpdate )
		{
			if( show == true )
			{
				updateDrawFPS( draws[0], _dtRender ) ;
				updateDrawMS( draws[1], _dtUpdate ) ;
			}
		}

		private void updateDrawFPS( final TextDraw _draw, final double _dt )
		{
			final StringBuilder txt = _draw.getText() ;
			txt.setLength( 0 ) ;
			txt.insert( 0, ( int )Math.ceil( 1.0f / _dt ) ) ;
			txt.append( "fps" ) ;

			_draw.setRange( 0, txt.length() ) ;
			updater.forceUpdate() ;
		}

		private void updateDrawMS( final TextDraw _draw, final double _dt )
		{
			final StringBuilder txt = _draw.getText()  ;
			txt.setLength( 0 ) ;
			txt.insert( 0, ( int )( _dt * 1000.0 ) ) ;
			txt.append( "ms" ) ;

			_draw.setRange( 0, txt.length() ) ;
			updater.forceUpdate() ;
		}

		public TextDraw[] getDraws()
		{
			return draws ;
		}
	}
}
