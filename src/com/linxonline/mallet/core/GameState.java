package com.linxonline.mallet.core ;

import java.util.List ;

import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.renderer.* ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.core.ISystem ;

import com.linxonline.mallet.input.IInputSystem ;
import com.linxonline.mallet.input.IInputHandler ;
import com.linxonline.mallet.input.InputState ;

import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.event.EventSystem ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.InterceptController ;
import com.linxonline.mallet.event.IEventSystem ;
import com.linxonline.mallet.event.IEventController ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.physics.CollisionSystem ;
import com.linxonline.mallet.physics.CollisionAssist ;

import com.linxonline.mallet.animation.AnimationSystem ;
import com.linxonline.mallet.animation.AnimationAssist ;

import com.linxonline.mallet.entity.EntitySystem ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.Component ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.time.ElapsedTimer ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.script.IScriptEngine ;
import com.linxonline.mallet.script.Script ;
import com.linxonline.mallet.script.javascript.JSScriptEngine ;

public class GameState
{
	public static final int NONE = 0 ;
	public static final int TRANSIST_SHUTDOWN = 1 ;
	public static final int TRANSIST_PAUSE = 2 ;

	public final String name ;					// Must be unique
	protected String transition ;				// State to transition to
	protected int transitionType = NONE ;

	protected float DEFAULT_TIMESTEP = 1.0f / 15.0f ;					// 15Hz
	protected float DEFAULT_FRAMERATE = 1.0f / 60.0f ;					// 60Hz
	protected float DEFAULT_ESCAPE_TIME = 0.25f ;						// Escape threshold, if delta spirals out of control with no way to catchup
	protected long DEFAULT_SLEEP = 10L ;								// Duration to sleep before continuing update cycle

	private final List<IUpdate> mainUpdaters = MalletList.<IUpdate>newList() ;		// Updaters that are triggered on DEFAULT_TIMESTEP
	private final List<IUpdate> drawUpdaters = MalletList.<IUpdate>newList() ;		// Updaters that are triggered on DEFAULT_FRAMERATE

	protected final InputState inputWorldSystem = new InputState() ;			// Internal World Input System
	protected final InputState inputUISystem = new InputState() ;				// Internal UI Input System
	protected final EventSystem eventSystem = new EventSystem() ;				// Internal Event System

	private final InterceptController interceptController = new InterceptController() ;
	private final EventController internalController = new EventController() ;		// Used to process Events, from internal eventSystem
	private final EventController externalController = new EventController() ;		// Used to process Events, from external eventSystem

	protected ISystem system = null ;																// Provides access to Root systems
	protected final EntitySystem entitySystem = new EntitySystem( eventSystem ) ;

	protected final CollisionSystem collisionSystem = new CollisionSystem() ;
	protected final AudioSystem audioSystem = new AudioSystem() ;
	protected final AnimationSystem animationSystem = new AnimationSystem() ;

	protected boolean paused = false ;									// Determine whether state was paused.
	protected double deltaUpdateTime = 0.0 ;
	protected double deltaRenderTime = 0.0 ;
	protected double updateAccumulator = 0.0 ;							// Current dt update
	protected double renderAccumulator = 0.0 ;							// Current dt render

	private final ShowFPS showFPS = new ShowFPS() ;

	public GameState( final String _name )
	{
		name = _name ;

		createCoreUpdate() ;
		setFrameRate( GlobalConfig.getInteger( "MAXFPS", 60 ) ) ;
	}

	protected final void setTransition( final String _transition, final int _type )
	{
		transition = _transition ;				// Name of other State
		transitionType = _type ;		// PAUSE or SHUTDOWN
	}

	/**
		Return the name of the state to transition to.
	*/
	public final String getTransition()
	{
		return transition ;
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
	public void startState( final Settings _package )
	{
		AudioAssist.setAssist( audioSystem.createAudioAssist() ) ;
		AnimationAssist.setAssist( animationSystem.createAnimationAssist() ) ;
		CollisionAssist.setAssist( collisionSystem.createCollisionAssist() ) ;

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
	public Settings pauseState()
	{
		unhookHandlerSystems() ;				// Prevent system from receiving external events
		unhookGameStateEventController() ;
		audioSystem.pauseSystem() ;

		showFPS.setShow( false ) ;
		paused = true ;
		return null ;
	}

	public int update( final double _dt )
	{
		final int transition = updateMain( _dt ) ;
		if( transition == GameState.NONE )
		{
			updateDraw( _dt ) ;
		}

		final double deltaTotalTime = deltaUpdateTime + deltaRenderTime ;
		if( deltaTotalTime < DEFAULT_FRAMERATE )
		{
			final float diff = DEFAULT_FRAMERATE - ( float )deltaTotalTime ;
			final long wait = ( long )( diff * 1000.0f ) - 5 ;
			if( wait <= 0 )
			{
				return transition ;
			}

			try
			{
				synchronized( this )
				{
					wait( wait ) ;
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace() ;
			}
		}

		return transition ;
	}

	/**
		Update the main game-logic.
		Default is expected to be 15Hz
	*/
	private int updateMain( final double _dt )
	{
		if( _dt >= DEFAULT_ESCAPE_TIME || system == null )
		{
			return NONE ;
		}

		updateAccumulator += _dt ;
		while( updateAccumulator >= DEFAULT_TIMESTEP )
		{
			final long startTime = ElapsedTimer.nanoTime() ;
			final int size = mainUpdaters.size() ;
			for( int i = 0; i < size; ++i )
			{
				final IUpdate update = mainUpdaters.get( i ) ;
				update.update( DEFAULT_TIMESTEP ) ;
			}

			showFPS.update( deltaRenderTime, deltaUpdateTime ) ;
			updateAccumulator -= DEFAULT_TIMESTEP ;

			final long endTime = ElapsedTimer.nanoTime() ;
			deltaUpdateTime = ( endTime - startTime ) * 0.000000001 ;
		}

		final int type = transitionType ;
		transitionType = NONE ;
		return type ;
	}

	/**
		Render to the screen.
		Default is expected to be 60Hz.
	*/
	private void updateDraw( final double _dt )
	{
		renderAccumulator += _dt ;
		if( renderAccumulator >= DEFAULT_FRAMERATE )
		{
			final long startTime = ElapsedTimer.nanoTime() ;
			final int size = drawUpdaters.size() ;
			for( int i = 0; i < size; ++i )
			{
				final IUpdate update = drawUpdaters.get( i ) ;
				update.update( DEFAULT_FRAMERATE ) ;
			}
			final long endTime = ElapsedTimer.nanoTime() ;

			deltaRenderTime = ( endTime - startTime ) * 0.000000001 ;
			renderAccumulator = 0.0 ;
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
		entitySystem.removeEntity( _entity ) ;
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
			Logger.println( "Clearing audio from game-state.", Logger.Verbosity.MINOR ) ;
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
		final IInputSystem input = system.getInput() ;
		input.removeInputHandler( inputUISystem ) ;
		input.removeInputHandler( inputWorldSystem ) ;
	}

	/**
		Create the intended update mode for the gamestate.
	*/
	protected void createCoreUpdate()
	{
		mainUpdaters.add( ( final double _dt ) ->
		{
			final float dt = ( float )_dt ;

			system.getRenderer().updateState( dt ) ;
			system.getInput().update() ;
			system.getEventSystem().sendEvents() ;
			system.getEventController().update() ;

			inputUISystem.update() ;
			inputWorldSystem.update() ;

			eventSystem.sendEvents() ;
			internalController.update() ;
			externalController.update() ;

			collisionSystem.update( dt ) ;
			entitySystem.update( dt ) ;
			audioSystem.update( dt ) ;
		} ) ;

		drawUpdaters.add( ( final double _dt ) ->
		{
			system.getInput().update() ;

			inputUISystem.update() ;
			inputWorldSystem.update() ;

			final float dt = ( float )_dt ;
			animationSystem.update( dt ) ;

			system.getRenderer().draw( dt ) ;
		} ) ;
	}

	/**
		Allow the user to attach further core systems to the
		game-states update cycle.
	*/
	protected void createUpdaters( final List<IUpdate> _main, final List<IUpdate> _draw ) {}

	protected void initEventProcessors( final EventController _internal, final EventController _external, final InterceptController _intercept )
	{
		_internal.addProcessor( "ADD_GAME_STATE_UI_INPUT", ( final IInputHandler _handler ) ->
		{
			inputUISystem.addInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_GAME_STATE_UI_INPUT", ( final IInputHandler _handler ) ->
		{
			inputUISystem.removeInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "ADD_GAME_STATE_WORLD_INPUT", ( final IInputHandler _handler ) ->
		{
			inputWorldSystem.addInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_GAME_STATE_WORLD_INPUT", ( final IInputHandler _handler ) ->
		{
			inputWorldSystem.removeInputHandler( _handler ) ;
		} ) ;

		_internal.addProcessor( "ADD_GAME_STATE_EVENT", ( final IEventController _controller ) ->
		{
			_controller.setAddEventInterface( eventSystem ) ;
			eventSystem.addHandler( _controller ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_GAME_STATE_EVENT", ( final IEventController _controller ) ->
		{
			eventSystem.removeHandler( _controller ) ;
			_controller.setAddEventInterface( null ) ;
		} ) ;

		_internal.addProcessor( "ADD_BACKEND_EVENT", ( final IEventController _controller ) ->
		{
			final IEventSystem eventBackend = system.getEventSystem() ;

			_controller.setAddEventInterface( eventBackend ) ;
			eventBackend.addHandler( _controller ) ;
		} ) ;

		_internal.addProcessor( "REMOVE_BACKEND_EVENT", ( final IEventController _controller ) ->
		{
			final IEventSystem eventBackend = system.getEventSystem() ;
			eventBackend.removeHandler( _controller ) ;
			_controller.setAddEventInterface( null ) ;
		} ) ;

		_internal.addProcessor( "SHOW_GAME_STATE_FPS", ( final Boolean _show ) ->
		{
			final boolean show = _show ;
			if( show == showFPS.toShow() )
			{
				// If they are setting it to the same value it 
				// currently is don't do anything.
				return ;
			}

			showFPS.setShow( show ) ;
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

	public EntitySystem getEntitySystem()
	{
		return entitySystem ;
	}

	/**
		Guarantees that all systems the state uses will be blank.
	*/
	protected void clear()
	{
		internalController.setAddEventInterface( null ) ;
		internalController.clearEvents() ;

		externalController.setAddEventInterface( null ) ;
		externalController.clearEvents() ;

		eventSystem.reset() ;

		inputUISystem.clearInputs() ;
		inputUISystem.clearHandlers() ;

		inputWorldSystem.clearInputs() ;
		inputWorldSystem.clearHandlers() ;

		audioSystem.clear() ;
		entitySystem.clear() ;
		animationSystem.clear() ;
	}

	/**
		Allows the developer to create their own update modes.
	*/
	public interface IUpdate
	{
		public void update( final double _dt ) ;
	}

	private static final class ShowFPS
	{
		private boolean show = false ;
		private final TextDraw[] draws = new TextDraw[3] ;
		private final TextUpdater updater ;

		private int accumulatedTicks = 0 ;
		private int accumulatedFPS = 0 ;

		private int averageFPS = 0 ;

		public ShowFPS()
		{
			final World world = WorldAssist.getDefault() ;

			final Program program = ProgramAssist.add( new Program( "SIMPLE_FONT" ) ) ;
			program.mapUniform( "inTex0", new Font( "Arial" ) ) ;

			draws[0] = new TextDraw( "0" ) ;
			draws[0].setHidden( !show ) ;
			draws[0].setBoundary( 200.0f, 50.0f ) ;

			draws[1] = new TextDraw( "0" ) ;
			draws[1].setPosition( 0.0f, 20.0f, 0.0f ) ;
			draws[1].setHidden( !show ) ;
			draws[1].setBoundary( 200.0f, 50.0f ) ;

			draws[2] = new TextDraw( "0" ) ;
			draws[2].setPosition( 0.0f, 40.0f, 0.0f ) ;
			draws[2].setHidden( !show ) ;
			draws[2].setBoundary( 200.0f, 50.0f ) ;

			final TextUpdaterPool pool = RenderPools.getTextUpdaterPool() ;
			updater = pool.getOrCreate( world, program, true, Integer.MAX_VALUE ) ;

			final TextBuffer geometry = updater.getBuffer( 0 ) ;
			geometry.addDraws( draws ) ;
		}

		public void setShow( final boolean _show )
		{
			show = _show ;
			for( final TextDraw draw : draws )
			{
				draw.setHidden( !show ) ;
			}
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
				updateMS( draws[1], _dtRender ) ;
				updateMS( draws[2], _dtUpdate ) ;
			}
		}

		private void updateDrawFPS( final TextDraw _draw, final double _dt )
		{
			final int currentFPS = ( int )Math.ceil( 1.0f / _dt ) ;

			accumulatedFPS += currentFPS ;
			accumulatedTicks += 1 ;

			if( accumulatedTicks >= 10 )
			{
				averageFPS = accumulatedFPS / accumulatedTicks ;

				accumulatedFPS = 0 ;
				accumulatedTicks = 0 ;
			}

			final StringBuilder txt = _draw.getText() ;
			txt.setLength( 0 ) ;
			txt.append( averageFPS ) ;
			txt.append( "fps " ) ;
			txt.append( currentFPS ) ;
			txt.append( "fps" ) ;

			_draw.setRange( 0, txt.length() ) ;
			updater.forceUpdate() ;
		}

		private void updateMS( final TextDraw _draw, final double _dt )
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
