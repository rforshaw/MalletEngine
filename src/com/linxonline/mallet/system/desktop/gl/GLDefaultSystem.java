package com.linxonline.mallet.system.desktop.gl ;

import java.awt.event.WindowListener ;
import java.awt.event.WindowEvent ;
import javax.swing.JFrame ;
import java.awt.image.BufferedImage ;
import java.awt.Point ;
import java.awt.Cursor ;
import java.awt.Toolkit ;

import javax.media.opengl.awt.GLJPanel ;

import com.linxonline.mallet.util.locks.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.audio.desktop.alsa.* ;
import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.renderer.desktop.* ;
import com.linxonline.mallet.input.desktop.InputSystem ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.system.* ;

import com.linxonline.mallet.renderer.desktop.GL.* ;

/*===========================================*/
// DefaultSystem
// Used to hook up Engine to OS using Java API
// Central Location for Events, Input, 
// and Rendering
/*===========================================*/

public class GLDefaultSystem implements SystemInterface
{
	protected String titleName = "GL Mallet Engine" ;
	protected final DefaultShutdown shutdownDelegate = new DefaultShutdown() ;
	protected final JFrame frame = new JFrame( titleName ) ;					// Initialise Window

	protected ALSASourceGenerator sourceGenerator = new ALSASourceGenerator() ;
	protected EventController eventController = new EventController() ;
	protected GLRenderer renderer = new GLRenderer() ;

	public final EventSystem eventSystem = new EventSystem( "ROOT_EVENT_SYSTEM" ) ;
	public final InputSystem inputSystem = new InputSystem() ;

	public GLDefaultSystem()
	{
		Locks.getLocks().addLock( "APPLICATION_LOCK", new JLock() ) ;
	}

	public void initSystem()
	{
		initEventProcessors() ;
		renderer.start() ;
		sourceGenerator.startGenerator() ;
		inputSystem.inputAdapter = renderer.renderInfo ;				// Hook up Input Adapter

		frame.addWindowListener( new WindowListener()
		{
			public void windowActivated( final WindowEvent _event ) {}
			public void windowClosed( final WindowEvent _event ) {}
			public void windowClosing( final WindowEvent _event ) { shutdownSystem() ; }
			public void windowDeactivated( final WindowEvent _event ) {}
			public void windowDeiconified( final WindowEvent _event ) {}
			public void windowIconified( final WindowEvent _event ) {}
			public void windowOpened( final WindowEvent _event ) {}
		} ) ;

		renderer.hookToWindow( frame ) ;
		renderer.getCanvas().addMouseListener( inputSystem ) ;
		renderer.getCanvas().addMouseMotionListener( inputSystem ) ;
		renderer.getCanvas().addMouseWheelListener( inputSystem ) ;
		renderer.getCanvas().addKeyListener( inputSystem ) ;

		addEvent( new Event( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
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

		/*eventController.addEventProcessor( new EventProcessor<?>( "SYSTEM_RENDER", "SYSTEM_RENDER" )
		{
			@Override
			public void processEvent( final Event<?> _event )
			{
				System.out.println( "Handle Render" ) ;
			}
		} ) ;*/

		addEventHandler( eventController ) ;
	}

	public void startSystem() {}
	public void stopSystem() {}

	public void shutdownSystem()
	{
		sourceGenerator.shutdownGenerator() ;
		renderer.shutdown() ;
	}

	/*INPUT HOOK*/
	public void addInputHandler( final InputHandler _handler )
	{
		inputSystem.addInputHandler( _handler ) ;
	}

	public void removeInputHandler( final InputHandler _handler )
	{
		inputSystem.removeInputHandler( _handler ) ;
	}

	/*EVENT HOOK*/
	public void addEvent( final Event _event )
	{
		eventSystem.addEvent( _event ) ;
	}

	public void addEventHandler( final EventHandler _handler )
	{
		eventSystem.addEventHandler( _handler ) ;
	}

	public void removeEventHandler( final EventHandler _handler )
	{
		eventSystem.removeEventHandler( _handler ) ;
	}

	@Override
	public ShutdownDelegate getShutdownDelegate()
	{
		return shutdownDelegate ;
	}

	/*RENDER*/

	public void setTitleName( final String _titleName )
	{
		titleName = _titleName ;
	}

	@Override
	public RenderInterface getRenderInterface()
	{
		return renderer ;
	}

	/*AUDIO SOURCE GENERATOR*/
	@Override
	public AudioGenerator getAudioGenerator()
	{
		return sourceGenerator ;
	}

	@Override
	public void sleep( final long _millis )
	{
		try
		{
			Thread.sleep( _millis ) ;
		}
		catch( InterruptedException ex )
		{
			ex.printStackTrace() ;
		}
	}

	@Override
	public boolean update( final float _dt )
	{
		renderer.updateState( _dt ) ;
		inputSystem.update() ;

		eventSystem.update() ;			// Pass the Events to the interested Backend Systems
		eventController.update() ;		// Process the Events this system is interested in

		return true ;	// Informs the Game System whether to continue updating or not.
	}

	@Override
	public void draw( final float _dt )
	{
		renderer.draw( _dt ) ;
	}
}
