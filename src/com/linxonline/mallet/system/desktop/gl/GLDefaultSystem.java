package com.linxonline.mallet.system.desktop.gl ;

import javax.swing.JFrame ;
import java.awt.image.BufferedImage ;
import java.awt.Point ;
import java.awt.Cursor ;
import java.awt.Toolkit ;

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
	protected JFrame frame = new JFrame( title ) ;					// Initialise Window
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

	public JFrame getFrame()
	{
		return frame ;
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

		render.hookToWindow( frame ) ;
		render.getCanvas().addMouseListener( input ) ;
		render.getCanvas().addMouseMotionListener( input ) ;
		render.getCanvas().addMouseWheelListener( input ) ;
		render.getCanvas().addKeyListener( input ) ;

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
				frame.getContentPane().setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) ) ;
				if( displayMouse == false )
				{
					// Hide the mouse if it isn't meant to be displayed.
					final BufferedImage cursorImg = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB ) ;
					final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImg, new Point( 0, 0 ), "BLANK_CURSOR" ) ;
					frame.getContentPane().setCursor( blankCursor ) ;
				}
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
