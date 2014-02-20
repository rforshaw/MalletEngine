package com.linxonline.mallet.system ;

import java.awt.event.WindowListener ;
import java.awt.event.WindowEvent ;
import javax.swing.JFrame ;
import java.awt.image.BufferedImage ;
import java.awt.Point ;
import java.awt.Cursor ;
import java.awt.Toolkit ;

import com.linxonline.mallet.util.locks.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.audio.alsa.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.GL.* ;


/*===========================================*/
// DefaultSystem
// Used to hook up Engine to OS using Java API
// Central Location for Events, Input, 
// and Rendering
/*===========================================*/

public class GLDefaultSystem implements SystemInterface
{
	protected String titleName = new String( "GL Mallet Engine" ) ;

	protected ALSASourceGenerator sourceGenerator = new ALSASourceGenerator() ;
	protected GLRenderer renderer = new GLRenderer() ;

	public final EventSystem eventSystem = new EventSystem( "ROOT_EVENT_SYSTEM" ) ;
	public final InputSystem inputSystem = new InputSystem() ;

	public GLDefaultSystem()
	{
		Locks.getLocks().addLock( "APPLICATION_LOCK", new JLock() ) ;
	}

	public void initSystem()
	{
		renderer.start() ;
		sourceGenerator.startGenerator() ;
		inputSystem.inputAdapter = renderer.renderInfo ;				// Hook up Input Adapter

		final JFrame frame = new JFrame( titleName ) ;					// Initialise Window

		//final BufferedImage cursorImg = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_ARGB ) ;
		//final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImg, new Point( 0, 0 ), "blank cursor" ) ;
		//frame.getContentPane().setCursor(blankCursor);

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

	/*RENDER*/

	public void setTitleName( final String _titleName )
	{
		titleName = _titleName ;
	}
	
	public void setDisplayDimensions( final Vector2 _display )
	{
		renderer.setDisplayDimensions( ( int )_display.x, ( int )_display.y ) ;
	}

	public void setRenderDimensions( final Vector2 _render )
	{
		renderer.setRenderDimensions( ( int )_render.x, ( int )_render.y ) ;
	}

	public void setCameraPosition( final Vector3 _camera )
	{
		renderer.setCameraPosition( _camera ) ;
	}

	public RenderInterface getRenderInterface()
	{
		return renderer ;
	}

	/*AUDIO SOURCE GENERATOR*/
	public AudioGenerator getAudioGenerator()
	{
		return sourceGenerator ;
	}

	public boolean update()
	{
		renderer.updateState() ;
		inputSystem.update() ;
		eventSystem.update() ;
		return true ; // Informs the Game System whether to continue updating or not.
	}

	public void draw( final float _dt )
	{
		renderer.draw( _dt ) ;
	}
}
