package com.linxonline.malleteditor.system ;

import java.awt.event.WindowListener ;
import java.awt.event.WindowEvent ;
import javax.swing.JFrame ;
import java.awt.Dimension ;

import com.linxonline.malleteditor.renderer.GLEditorRenderer ;
import com.linxonline.mallet.system.desktop.gl.GLDefaultSystem ;
import com.linxonline.mallet.input.desktop.InputSystem ;
import com.linxonline.mallet.renderer.desktop.GL.* ;

public class GLEditorSystem extends GLDefaultSystem
{
	public GLEditorSystem()
	{
		super() ;
		renderer = new GLEditorRenderer() ;
	}

	/*public void initSystem()
	{
		renderer.start() ;
		audioGenerator.startGenerator() ;

		final GLRenderer render = ( GLRenderer )renderer ;
		final InputSystem input = ( InputSystem )inputSystem ;

		input.inputAdapter = render.renderInfo ;				// Hook up Input Adapter

		final JFrame frame = new JFrame( title ) ;					// Initialise Window
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

		render.hookToWindow( frame ) ;
		frame.setVisible( false ) ;
		frame.remove( render.getCanvas() ) ;

		final MainPanel mainPanel = new MainPanel( render.getCanvas() ) ;
		eventSystem.addEventHandler( mainPanel.getEventController() ) ;
		mainPanel.getEventController().setAddEventInterface( eventSystem ) ;

		frame.add( mainPanel ) ;
		frame.pack() ;
		frame.validate() ;
		frame.setVisible( true ) ;
		render.draw( 0.0f ) ;

		render.getCanvas().addMouseListener( input ) ;
		render.getCanvas().addMouseMotionListener( input ) ;
		render.getCanvas().addMouseWheelListener( input ) ;
		render.getCanvas().addKeyListener( input ) ;
	}*/
}