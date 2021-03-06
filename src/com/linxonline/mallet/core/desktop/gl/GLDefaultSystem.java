package com.linxonline.mallet.core.desktop.gl ;

import com.jogamp.newt.opengl.GLWindow ;
import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;

import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;
import com.linxonline.mallet.audio.desktop.alsa.ALSASourceGenerator ;
import com.linxonline.mallet.renderer.desktop.opengl.GLRenderer ;
import com.linxonline.mallet.input.desktop.InputSystem ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.util.buffers.* ;

/**
	Central location for OpenGL Desktop Applications.
	Uses ALSA for audio.
	Handles input events using JFrame.
*/
public class GLDefaultSystem extends BasicSystem<DesktopFileSystem,
												 DefaultShutdown,
												 GLRenderer,
												 ALSASourceGenerator,
												 InputSystem,
												 EventSystem,
												 GameSystem>
{
	protected final EventController eventController = new EventController() ;

	public GLDefaultSystem()
	{
		super( new DefaultShutdown(),
			   new GLRenderer(),
			   new ALSASourceGenerator(),
			   new EventSystem(),
			   new InputSystem(),
			   new DesktopFileSystem(),
			   new GameSystem() ) ;

		getGameSystem().setMainSystem( this ) ;
	}

	public GLWindow getWindow()
	{
		return getRenderer().getCanvas() ;
	}
	
	@Override
	public void initSystem()
	{
		initEventProcessors() ;

		final GLRenderer render = getRenderer() ;
		render.start() ;

		getAudioGenerator().startGenerator() ;

		final InputSystem input = getInput() ;
		render.getCanvas().setTitle( GlobalConfig.getString( "APPLICATION_NAME", "Mallet Engine" ) ) ;
		render.getCanvas().addMouseListener( input ) ;
		render.getCanvas().addKeyListener( input ) ;

		final EventSystem event = getEventSystem() ;
		event.addEvent( new Event<Boolean>( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "SYSTEM_FULLSCREEN",    GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ) ;

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
		eventController.addProcessor( "DISPLAY_SYSTEM_MOUSE", ( final Boolean _displayMouse ) ->
		{
			final boolean displayMouse = _displayMouse ;
			getWindow().setPointerVisible( displayMouse ) ;
		} ) ;

		eventController.addProcessor( "CAPTURE_SYSTEM_MOUSE", ( Boolean _confineMouse ) ->
		{
			final boolean confineMouse = _confineMouse ;
			getWindow().confinePointer( confineMouse ) ;
		} ) ;

		eventController.addProcessor( "SYSTEM_FULLSCREEN", ( Boolean _fullscreen ) ->
		{
			final boolean fullscreen = _fullscreen ;
			getWindow().setFullscreen( fullscreen ) ;
		} ) ;

		getEventSystem().addHandler( eventController ) ;
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
