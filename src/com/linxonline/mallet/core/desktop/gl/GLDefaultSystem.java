package com.linxonline.mallet.core.desktop.gl ;

import com.jogamp.newt.opengl.GLWindow ;
import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;
import com.jogamp.newt.event.MouseListener ;
import com.jogamp.newt.event.MouseEvent ;

import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;
import com.linxonline.mallet.audio.desktop.alsa.ALSAGenerator ;
import com.linxonline.mallet.renderer.desktop.opengl.GLRenderer ;
import com.linxonline.mallet.input.desktop.InputSystem ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.buffers.* ;

/**
	Central location for OpenGL Desktop Applications.
	Uses ALSA for audio.
	Handles input events using JFrame.
*/
public final class GLDefaultSystem extends BasicSystem<DesktopFileSystem,
													   DefaultShutdown,
													   GLRenderer,
													   ALSAGenerator,
													   InputSystem,
													   EventSystem,
													   GameSystem>
{
	private final EventController controller = new EventController() ;

	public GLDefaultSystem()
	{
		super( new DefaultShutdown(),
			   new GLRenderer(),
			   new ALSAGenerator(),
			   new EventSystem(),
			   new InputSystem(),
			   new DesktopFileSystem(),
			   new GameSystem() ) ;
	}

	public GLWindow getWindow()
	{
		return getRenderer().getCanvas() ;
	}
	
	@Override
	public void initSystem()
	{
		getGameSystem().setMainSystem( this ) ;

		initEventProcessors() ;

		final GLRenderer render = getRenderer() ;
		render.start() ;

		getAudioGenerator().start() ;

		final InputSystem input = getInput() ;
		render.getCanvas().setTitle( GlobalConfig.getString( "APPLICATION_NAME", "Mallet Engine" ) ) ;
		render.getCanvas().addMouseListener( input ) ;
		render.getCanvas().addKeyListener( input ) ;

		final EventSystem event = getEventSystem() ;
		event.addEvent( new Event<Boolean>( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "SYSTEM_FULLSCREEN",    GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ) ;

		final WinState window = new WinState() ;

		GlobalConfig.addNotify( "CAPTUREMOUSE", ( String _name ) -> {
			final boolean capture = GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ;
			event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", capture ) ) ;
		} ) ;

		GlobalConfig.addNotify( "FULLSCREEN", ( String _name ) -> {
			final boolean fullscreen = GlobalConfig.getBoolean( "FULLSCREEN", false ) ;
			event.addEvent( new Event<Boolean>( "SYSTEM_FULLSCREEN", fullscreen ) ) ;
		} ) ;

		getWindow().addWindowListener( window ) ;
		getWindow().addMouseListener( window ) ;
	}

	protected void initEventProcessors()
	{
		controller.addProcessor( "DISPLAY_SYSTEM_MOUSE", ( final Boolean _displayMouse ) ->
		{
			final boolean displayMouse = _displayMouse ;
			getWindow().setPointerVisible( displayMouse ) ;
		} ) ;

		controller.addProcessor( "CAPTURE_SYSTEM_MOUSE", ( Boolean _confineMouse ) ->
		{
			final boolean confineMouse = _confineMouse ;
			getWindow().confinePointer( confineMouse ) ;
		} ) ;

		controller.addProcessor( "SYSTEM_FULLSCREEN", ( Boolean _fullscreen ) ->
		{
			final boolean fullscreen = _fullscreen ;

			final GLRenderer render = getRenderer() ;
			render.setFullscreen( fullscreen ) ;
		} ) ;

		getEventSystem().addHandler( controller ) ;
	}

	@Override
	public void startSystem()
	{
		Logger.println( "Start System...", Logger.Verbosity.MINOR ) ;
	}

	@Override
	public void stopSystem()
	{
		Logger.println( "Stop System...", Logger.Verbosity.MINOR ) ;
	}

	@Override
	public boolean update( final float _dt )
	{
		super.update( _dt ) ;
		controller.update() ;		// Process the Events this system is interested in
		return true ;					// Informs the Game System whether to continue updating or not.
	}

	private final class WinState implements WindowListener, MouseListener
	{
		private final static int FOCUS_LOST = -1 ;
		private final static int FOCUS_UNKNOWN = 0 ;
		private final static int FOCUS_GAINED = 1 ;

		private int focusState = FOCUS_UNKNOWN ;

		@Override
		public void windowDestroyNotify( final WindowEvent _event )
		{
			GLDefaultSystem.this.shutdownSystem() ;
		}

		@Override
		public void windowGainedFocus( final WindowEvent _event )
		{
			Logger.println( "Main window gained focus.", Logger.Verbosity.MINOR ) ;
			focusState = FOCUS_GAINED ;
		}

		@Override
		public void windowLostFocus( final WindowEvent _event )
		{
			Logger.println( "Main window lost focus.", Logger.Verbosity.MINOR ) ;
			final EventSystem event = GLDefaultSystem.this.getEventSystem() ;
			event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", false ) ) ;

			focusState = FOCUS_LOST ;
		}

		public void windowRepaint( final WindowUpdateEvent _event ) {}
		public void windowDestroyed( final WindowEvent _event ) {}
		public void windowMoved( final WindowEvent _event ) {}
		public void windowResized( final WindowEvent _event ) {}

		public void mouseClicked( final MouseEvent e ) {}
		public void mouseDragged( final MouseEvent e ) {}

		@Override
		public void mouseEntered( final MouseEvent e )
		{
			switch( focusState )
			{
				default           : break ;
				case FOCUS_GAINED :
				{
				
					final EventSystem event = GLDefaultSystem.this.getEventSystem() ;
					event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
					break ;
				}
			}
		}

		public void mouseExited( final MouseEvent e ) {}
		public void mouseMoved( final MouseEvent e ) {}
		public void mousePressed( final MouseEvent e ) {}
		public void mouseReleased( final MouseEvent e ) {}
		public void mouseWheelMoved( final MouseEvent e ) {}
	}
}
