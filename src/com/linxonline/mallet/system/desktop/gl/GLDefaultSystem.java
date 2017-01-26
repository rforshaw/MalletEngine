package com.linxonline.mallet.system.desktop.gl ;

import com.jogamp.newt.opengl.GLWindow ;
import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;

import com.linxonline.mallet.audio.desktop.alsa.ALSASourceGenerator ;
import com.linxonline.mallet.renderer.desktop.GL.GLRenderer ;
import com.linxonline.mallet.input.desktop.InputSystem ;
import com.linxonline.mallet.util.locks.* ;
import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.event.* ;

/**
	Central location for OpenGL Desktop Applications.
	Uses ALSA for audio.
	Handles input events using JFrame.
*/
public class GLDefaultSystem extends BasicSystem
{
	protected EventController eventController = new EventController() ;

	public GLDefaultSystem()
	{
		Locks.getLocks().addLock( "APPLICATION_LOCK", new JLock() ) ;

		shutdownDelegate = new DefaultShutdown() ;
		renderer = new GLRenderer() ;
		audioGenerator = new ALSASourceGenerator() ;
		eventSystem = new EventSystem( "ROOT_EVENT_SYSTEM" ) ;
		inputSystem = new InputSystem() ;
	}

	public GLWindow getWindow()
	{
		final GLRenderer render = ( GLRenderer )renderer ;
		return render.getCanvas() ;
	}
	
	@Override
	public void initSystem()
	{
		initEventProcessors() ;

		renderer.start() ;
		audioGenerator.startGenerator() ;

		final GLRenderer render = ( GLRenderer )renderer ;
		final InputSystem input = ( InputSystem )inputSystem ;

		input.inputAdapter = render.getRenderInfo() ;					// Hook up Input Adapter

		render.getCanvas().setTitle( title ) ;
		render.getCanvas().addMouseListener( input ) ;
		render.getCanvas().addKeyListener( input ) ;

		eventSystem.addEvent( new Event<Boolean>( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
		eventSystem.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
		eventSystem.addEvent( new Event<Boolean>( "SYSTEM_FULLSCREEN",    GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ) ;

		getWindow().addWindowListener( new WindowListener()
		{
			public void windowDestroyNotify( final WindowEvent _event )
			{
				GLDefaultSystem.this.shutdownSystem() ;
			}

			public void windowGainedFocus( final WindowEvent _event ) {}
			public void windowLostFocus( final WindowEvent _event ) {}
			public void windowRepaint( final WindowUpdateEvent _event ) {}
			public void windowDestroyed( final WindowEvent _event ) {}
			public void windowMoved( final WindowEvent _event ) {}
			public void windowResized( final WindowEvent _event ) {}
		} ) ;
	}

	protected void initEventProcessors()
	{
		eventController.addEventProcessor( new EventProcessor<Boolean>( "USE_SYSTEM_MOUSE", "DISPLAY_SYSTEM_MOUSE" )
		{
			@Override
			public void processEvent( final Event<Boolean> _event )
			{
				final boolean displayMouse = _event.getVariable() ;
				getWindow().setPointerVisible( displayMouse ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<Boolean>( "USE_SYSTEM_MOUSE", "CAPTURE_SYSTEM_MOUSE" )
		{
			@Override
			public void processEvent( final Event<Boolean> _event )
			{
				final boolean confineMouse = _event.getVariable() ;
				getWindow().confinePointer( confineMouse ) ;
			}
		} ) ;

		eventController.addEventProcessor( new EventProcessor<Boolean>( "USE_SYSTEM_MOUSE", "SYSTEM_FULLSCREEN" )
		{
			@Override
			public void processEvent( final Event<Boolean> _event )
			{
				final boolean fullscreen = _event.getVariable() ;
				getWindow().setFullscreen( fullscreen ) ;
			}
		} ) ;

		eventSystem.addEventHandler( eventController ) ;
	}

	@Override
	public void startSystem()
	{
		System.out.println( "Start System..." ) ;
	}

	@Override
	public void stopSystem()
	{
		System.out.println( "Stop System..." ) ;
	}

	@Override
	public boolean update( final float _dt )
	{
		super.update( _dt ) ;
		eventController.update() ;		// Process the Events this system is interested in
		return true ;					// Informs the Game System whether to continue updating or not.
	}
}
