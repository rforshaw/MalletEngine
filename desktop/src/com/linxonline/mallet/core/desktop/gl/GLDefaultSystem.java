package com.linxonline.mallet.core.desktop.gl ;

import com.jogamp.newt.opengl.GLWindow ;
import com.jogamp.newt.event.WindowListener ;
import com.jogamp.newt.event.WindowUpdateEvent ;
import com.jogamp.newt.event.WindowEvent ;
import com.jogamp.newt.event.MouseListener ;
import com.jogamp.newt.event.MouseEvent ;

import com.linxonline.mallet.io.filesystem.desktop.DesktopFileSystem ;
import com.linxonline.mallet.audio.desktop.alsa.ALSAGenerator ;

import com.linxonline.mallet.renderer.RenderAssist ;
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
													   InputSystem>
{
	private final EventQueue<Boolean> DISPLAY_SYSTEM_MOUSE = Event.get( "DISPLAY_SYSTEM_MOUSE" ) ;
	private final EventQueue<Boolean> CAPTURE_SYSTEM_MOUSE = Event.get( "CAPTURE_SYSTEM_MOUSE" ) ;
	private final EventQueue<Boolean> SYSTEM_FULLSCREEN = Event.get( "SYSTEM_FULLSCREEN" ) ;

	public GLDefaultSystem( final Thread _main )
	{
		super( new DefaultShutdown(),
			   new GLRenderer( _main ),
			   new ALSAGenerator(),
			   new InputSystem(),
			   new DesktopFileSystem() ) ;
	}

	public GLWindow getWindow()
	{
		return getRenderer().getCanvas() ;
	}
	
	@Override
	public void init()
	{
		initEventProcessors() ;

		final GLRenderer render = getRenderer() ;
		render.start() ;

		getAudioGenerator().start() ;

		final InputSystem input = getInput() ;
		render.getCanvas().setTitle( GlobalConfig.getString( "APPLICATION_NAME", "Mallet Engine" ) ) ;
		render.getCanvas().addMouseListener( input ) ;
		render.getCanvas().addKeyListener( input ) ;

		DISPLAY_SYSTEM_MOUSE.add( GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ;
		CAPTURE_SYSTEM_MOUSE.add( GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ;
		SYSTEM_FULLSCREEN.add( GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ;

		GlobalConfig.addNotify( "CAPTUREMOUSE", ( String _name ) -> {
			final boolean capture = GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ;
			CAPTURE_SYSTEM_MOUSE.add( capture ) ;
		} ) ;

		GlobalConfig.addNotify( "FULLSCREEN", ( String _name ) -> {
			final boolean fullscreen = GlobalConfig.getBoolean( "FULLSCREEN", false ) ;
			SYSTEM_FULLSCREEN.add( fullscreen ) ;
		} ) ;

		final WinState window = new WinState() ;

		getWindow().addWindowListener( window ) ;
		getWindow().addMouseListener( window ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		System.exit( 0 ) ;
	}

	protected void initEventProcessors()
	{
		block.add( "DISPLAY_SYSTEM_MOUSE", ( final Boolean _displayMouse ) ->
		{
			final boolean displayMouse = _displayMouse ;
			getWindow().setPointerVisible( displayMouse ) ;
		} ) ;

		block.add( "CAPTURE_SYSTEM_MOUSE", ( Boolean _confineMouse ) ->
		{
			final boolean confineMouse = _confineMouse ;
			getWindow().confinePointer( confineMouse ) ;
		} ) ;

		block.add( "SYSTEM_FULLSCREEN", ( Boolean _fullscreen ) ->
		{
			final boolean fullscreen = _fullscreen ;
			RenderAssist.setFullscreen( fullscreen ) ;
		} ) ;
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
			Event.addEvent( Event.<Object>create( "START_SYSTEM_SHUTDOWN" ) ) ;
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
			Event.addEvent( Event.<Boolean>create( "CAPTURE_SYSTEM_MOUSE", false ) ) ;

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
					Event.addEvent( Event.<Boolean>create( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
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
