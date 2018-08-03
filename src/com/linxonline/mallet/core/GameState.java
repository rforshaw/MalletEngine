package com.linxonline.mallet.core ;

import java.util.List ;

import com.linxonline.mallet.audio.* ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.World ;

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
import com.linxonline.mallet.event.EventProcessor ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.IEventSystem ;

import com.linxonline.mallet.physics.CollisionSystem ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;
import com.linxonline.mallet.animation.* ;

import com.linxonline.mallet.util.time.ElapsedTimer ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.settings.* ;

import com.linxonline.mallet.io.save.state.DataConverter ;

public class GameState extends State
{
	protected float DEFAULT_TIMESTEP = 1.0f / 15.0f ;					// 15Hz
	protected float DEFAULT_FRAMERATE = 1.0f / 60.0f ;					// 60Hz
	protected float DEFAULT_ESCAPE_TIME = 0.25f ;						// Escape threshold, if delta spirals out of control with no way to catchup
	protected long DEFAULT_SLEEP = 10L ;								// Duration to sleep before continuing update cycle

	protected IUpdate currentUpdate = null ;												// Current Running Mode

	protected final InputState inputWorldSystem = new InputState() ;										// Internal World Input System
	protected final InputState inputUISystem = new InputState() ;											// Internal UI Input System
	protected final EventSystem eventSystem = new EventSystem( "GAME_STATE_EVENT_SYSTEM" ) ;				// Internal Event System

	private final EventController internalController = new EventController( "GAME_STATE_CONTROLLER_INTERNAL" ) ;		// Used to process Events, from internal eventSystem
	private final EventController externalController = new EventController( "GAME_STATE_CONTROLLER_EXTERNAL" ) ;		// Used to process Events, from external eventSystem

	protected ISystem system = null ;																// Provides access to Root systems
	protected final IEntitySystem entitySystem = new EntitySystem( eventSystem/*, EntitySystem.Threaded.MULTI*/ ) ;

	protected final AudioSystem audioSystem = new AudioSystem() ;
	protected final AnimationSystem animationSystem = new AnimationSystem() ;
	protected final CollisionSystem collisionSystem = new CollisionSystem( eventSystem/*, null*/ ) ;

	protected boolean paused = false ;									// Determine whether state was paused.
	protected boolean draw = true ;										// Used to force a Draw
	protected double updateAccumulator = 0.0f ;							// Current dt update
	protected double renderAccumulator = 0.0f ;							// Current dt render

	private final ShowFPS showFPS = new ShowFPS() ;

	public GameState( final String _name )
	{
		super( _name ) ;
		initModes() ;
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
		hookHandlerSystems() ;

		if( paused == true )
		{
			paused = false ;
			resumeGame() ;
			audioSystem.resumeSystem() ;
		}
		else
		{
			initGame() ;
		}
		
		// Event processors need to be called last 
		// in case developer adds more during initGame or resumeGame.
		initEventProcessors( internalController, externalController ) ;
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
		eventSystem.addEventHandler( internalController ) ;
		internalController.setAddEventInterface( eventSystem ) ;

		system.getEventSystem().addEventHandler( externalController ) ;
		externalController.setAddEventInterface( system.getEventSystem() ) ;
	}

	protected void unhookGameStateEventController()
	{
		eventSystem.removeEventHandler( internalController ) ;
		eventSystem.removeHandlersNow() ;

		system.getEventSystem().removeEventHandler( externalController ) ;
		system.getEventSystem().removeHandlersNow() ;
	}

	/**
		Enable event-based systems to recieve events.
		Also hooks-up the inputSystem.
	*/
	protected void hookHandlerSystems()
	{
		final EventController animationController = animationSystem.getEventController() ;
		final EventController audioController = audioSystem.getEventController() ;

		eventSystem.addEventHandler( audioController ) ;
		eventSystem.addEventHandler( collisionSystem ) ;
		eventSystem.addEventHandler( system.getRenderer().getEventController() ) ;
		eventSystem.addEventHandler( animationController ) ;

		animationController.setAddEventInterface( eventSystem ) ;
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
		eventSystem.removeEventHandler( audioSystem.getEventController() ) ;
		eventSystem.removeEventHandler( animationSystem.getEventController() ) ;
		eventSystem.removeEventHandler( collisionSystem ) ;
		eventSystem.removeEventHandler( system.getRenderer().getEventController() ) ;
		eventSystem.removeHandlersNow() ;

		final IInputSystem input = system.getInput() ;
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
		currentUpdate = new IUpdate()
		{
			private double deltaRenderTime = 0.0 ;

			@Override
			public void update( final double _dt )
			{
				long startTime = ElapsedTimer.nanoTime() ;
				// Update Default : 15Hz
				updateAccumulator += _dt ;

				while( updateAccumulator > DEFAULT_TIMESTEP )
				{
					system.update( DEFAULT_TIMESTEP ) ;			// Update low-level systems
					inputUISystem.update() ;
					inputWorldSystem.update() ;
					eventSystem.update() ;

					internalController.update() ;
					externalController.update() ;

					showFPS.update( deltaRenderTime ) ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
					updateAccumulator -= DEFAULT_TIMESTEP ;
				}

				long endTime = ElapsedTimer.nanoTime() ;

				// Render Default : 60Hz
				renderAccumulator += _dt ;
				final double total = ( endTime - startTime ) * 0.000000001 ;

				final long sleepRender = ( long )( ( DEFAULT_FRAMERATE - ( total + renderAccumulator ) ) * 1000.0 ) ;	// Convert to milliseconds
				//System.out.println( "Total: " + total + " FPS: " + DEFAULT_FRAMERATE ) ;

				//System.out.println( "Acc: " + renderAccumulator + " FPS: " + DEFAULT_FRAMERATE ) ;
				if( renderAccumulator >= DEFAULT_FRAMERATE )
				{
					startTime = ElapsedTimer.nanoTime() ;

					system.getInput().update() ;
					inputUISystem.update() ;
					inputWorldSystem.update() ;

					animationSystem.update( DEFAULT_FRAMERATE ) ;
					system.draw( DEFAULT_FRAMERATE ) ;

					endTime = ElapsedTimer.nanoTime() ;
					//deltaTime += endTime - startTime ;

					deltaRenderTime = ( endTime - startTime ) * 0.000000001 ;
					renderAccumulator = 0.0 ;
				}

				if( sleepRender > 0L )
				{
					//System.out.println( "Sleep: " + sleepRender ) ;
					system.sleep( sleepRender ) ;
				}
			}
		} ;
	}

	protected void useApplicationMode()
	{
		currentUpdate = new IUpdate()
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
					inputUISystem.update() ;
					inputWorldSystem.update() ;
					eventSystem.update() ;

					internalController.update() ;
					externalController.update() ;

					collisionSystem.update( DEFAULT_TIMESTEP ) ;
					entitySystem.update( DEFAULT_TIMESTEP ) ;
					audioSystem.update( DEFAULT_TIMESTEP ) ;
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

				showFPS.update( _dt ) ;
				animationSystem.update( DEFAULT_FRAMERATE ) ;
				system.draw( DEFAULT_FRAMERATE ) ;
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

	protected void initEventProcessors( final EventController _internal, final EventController _external )
	{
		_internal.addEventProcessor( new EventProcessor<InputHandler>( "ADD_GAME_STATE_INPUT", "ADD_GAME_STATE_UI_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputUISystem.addInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<InputHandler>( "REMOVE_GAME_STATE_INPUT", "REMOVE_GAME_STATE_UI_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputUISystem.removeInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<InputHandler>( "ADD_GAME_STATE_INPUT", "ADD_GAME_STATE_WORLD_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputWorldSystem.addInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<InputHandler>( "REMOVE_GAME_STATE_INPUT", "REMOVE_GAME_STATE_WORLD_INPUT" )
		{
			public void processEvent( final Event<InputHandler> _event )
			{
				inputWorldSystem.removeInputHandler( _event.getVariable() ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<EventController>( "ADD_GAME_STATE_EVENT", "ADD_GAME_STATE_EVENT" )
		{
			public void processEvent( final Event<EventController> _event )
			{
				final EventController controller = _event.getVariable() ;
				controller.setAddEventInterface( eventSystem ) ;
				eventSystem.addEventHandler( controller ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<EventController>( "REMOVE_GAME_STATE_EVENT", "REMOVE_GAME_STATE_EVENT" )
		{
			public void processEvent( final Event<EventController> _event )
			{
				final EventController controller = _event.getVariable() ;
				eventSystem.removeEventHandler( controller ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<EventController>( "ADD_BACKEND_EVENT", "ADD_BACKEND_EVENT" )
		{
			public void processEvent( final Event<EventController> _event )
			{
				final EventController controller = _event.getVariable() ;
				final IEventSystem eventBackend = system.getEventSystem() ;

				controller.setAddEventInterface( eventBackend ) ;
				eventBackend.addEventHandler( controller ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<EventController>( "REMOVE_BACKEND_EVENT", "REMOVE_BACKEND_EVENT" )
		{
			public void processEvent( final Event<EventController> _event )
			{
				final EventController controller = _event.getVariable() ;
				final IEventSystem eventBackend = system.getEventSystem() ;

				eventBackend.removeEventHandler( controller ) ;
			}
		} ) ;

		_internal.addEventProcessor( new EventProcessor<Boolean>( "SHOW_GAME_STATE_FPS", "SHOW_GAME_STATE_FPS" )
		{
			private DrawDelegate delegate ;

			public void processEvent( final Event<Boolean> _event )
			{
				final boolean show = _event.getVariable() ;
				if( show == showFPS.toShow() )
				{
					// If they are setting it to the same value it 
					// currently is don't do anything.
					return ;
				}

				if( show == false )
				{
					showFPS.setShow( false ) ;
					if( delegate != null )
					{
						delegate.shutdown() ;
						delegate = null ;
					}
					return ;
				}

				showFPS.setShow( true ) ;
				eventSystem.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( final DrawDelegate _delegate )
					{
						delegate = _delegate ;
						delegate.addTextDraw( showFPS.getDraw() ) ;
					}
				} ) ) ;
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

		eventSystem.clearEvents() ;
		eventSystem.clearHandlers() ;

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
	protected interface IUpdate
	{
		public void update( final double _dt ) ;
	}

	private static class ShowFPS
	{
		private boolean show = false ;
		private final StringBuilder txt = new StringBuilder( "0" ) ;
		private final Draw draw = DrawAssist.createTextDraw( txt,
															new MalletFont( "Arial" ),
															new Vector3(),
															new Vector3(),
															new Vector3(),
															new Vector3( 1.0f, 1.0f, 1.0f ),
															200 ) ;

		public ShowFPS()
		{
			DrawAssist.amendUI( draw, true ) ;
		}

		public void setShow( final boolean _show )
		{
			show = _show ;
		}

		public boolean toShow()
		{
			return show ;
		}

		public void update( final double _dt )
		{
			if( show == true )
			{
				txt.setLength( 0 ) ;
				txt.insert( 0, ( int )Math.ceil( 1.0f / _dt ) ) ;
				//System.out.println( "FPS: " + txt ) ;

				DrawAssist.amendTextStart( draw, 0 ) ;
				DrawAssist.amendTextEnd( draw, txt.length() ) ;
				DrawAssist.forceUpdate( draw ) ;
			}
		}

		public Draw getDraw()
		{
			return draw ;
		}
	}
}
