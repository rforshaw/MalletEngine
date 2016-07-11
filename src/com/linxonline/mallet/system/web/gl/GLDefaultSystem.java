package com.linxonline.mallet.system.web.gl ;

import com.linxonline.mallet.audio.web.AudioSourceGenerator ;
import com.linxonline.mallet.renderer.web.gl.GLRenderer ;
import com.linxonline.mallet.input.web.InputSystem ;
import com.linxonline.mallet.util.locks.* ;
import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.event.* ;

public class GLDefaultSystem extends BasicSystem
{
	// Add Web Document here...
	protected EventController eventController = new EventController() ;

	public GLDefaultSystem()
	{
		Locks.getLocks().addLock( "APPLICATION_LOCK", new JLock() ) ;

		shutdownDelegate = new DefaultShutdown() ;
		renderer = new GLRenderer() ;
		audioGenerator = new AudioSourceGenerator() ;
		eventSystem = new EventSystem( "ROOT_EVENT_SYSTEM" ) ;
		inputSystem = new InputSystem() ;
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

		eventSystem.addEvent( new Event( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
	}

	protected void initEventProcessors()
	{
		eventController.addEventProcessor( new EventProcessor<Boolean>( "USE_SYSTEM_MOUSE", "DISPLAY_SYSTEM_MOUSE" )
		{
			@Override
			public void processEvent( final Event<Boolean> _event )
			{
				final boolean displayMouse = _event.getVariable() ;
				// Hide or show mouse if possible...
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
	public void sleep( final long _millis ) {}

	@Override
	public boolean update( final float _dt )
	{
		super.update( _dt ) ;
		eventController.update() ;		// Process the Events this system is interested in
		return true ;					// Informs the Game System whether to continue updating or not.
	}
}