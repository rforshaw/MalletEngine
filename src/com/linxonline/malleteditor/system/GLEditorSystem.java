package com.linxonline.malleteditor.system ;

import java.awt.event.WindowListener ;
import java.awt.event.WindowEvent ;
import javax.swing.JFrame ;
import java.awt.Dimension ;

import com.linxonline.malleteditor.renderer.GLEditorRenderer ;
import com.linxonline.mallet.system.GLDefaultSystem ;
import com.linxonline.mallet.maths.* ;

public class GLEditorSystem extends GLDefaultSystem
{
	public GLEditorSystem()
	{
		super() ;
		renderer = new GLEditorRenderer() ;
	}

	public void initSystem()
	{
		renderer.start() ;
		sourceGenerator.startGenerator() ;
		inputSystem.inputAdapter = renderer.renderInfo ;				// Hook up Input Adapter

		final JFrame frame = new JFrame( titleName ) ;					// Initialise Window
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
		frame.setVisible( false ) ;
		frame.remove( renderer.getCanvas() ) ;
		
		final MainPanel mainPanel = new MainPanel( renderer.getCanvas() ) ;
		eventSystem.addEventHandler( mainPanel.getEventController() ) ;
		
		frame.add( mainPanel ) ;
		frame.pack() ;
		frame.validate() ;
		frame.setVisible( true ) ;
		renderer.draw() ;

		renderer.getCanvas().addMouseListener( inputSystem ) ;
		renderer.getCanvas().addMouseMotionListener( inputSystem ) ;
		renderer.getCanvas().addMouseWheelListener( inputSystem ) ;
		renderer.getCanvas().addKeyListener( inputSystem ) ;
	}
}