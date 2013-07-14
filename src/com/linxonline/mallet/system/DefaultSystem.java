package com.linxonline.mallet.system ;

import javax.swing.JFrame ;
import java.awt.image.BufferedImage ;
import java.awt.Point ;
import java.awt.Dimension ;
import java.awt.Insets ;

import com.linxonline.mallet.renderer.G2D.G2DRenderer ;
import com.linxonline.mallet.audio.alsa.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

/*===========================================*/
// DefaultSystem
// Used to hook up Engine to OS using Java API
// Central Location for Events, Input, 
// and Rendering
/*===========================================*/

public class DefaultSystem implements SystemInterface
{
	protected JFrame frame = null ;
	protected String titleName = new String( "Mallet Engine" ) ;
	protected ALSASourceGenerator sourceGenerator = new ALSASourceGenerator() ;
	protected G2DRenderer renderer = new G2DRenderer() ;
	public EventSystem eventSystem = new EventSystem() ;
	public InputSystem inputSystem = new InputSystem() ;

	public DefaultSystem() {}

	public void initSystem()
	{
		inputSystem.inputAdapter = renderer.renderInfo ;

		frame = new JFrame( titleName ) ;
		frame.createBufferStrategy( 1 ) ;
		//frame.setCursor( frame.getToolkit().createCustomCursor( new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ), 
		//														new Point( 0, 0 ), "null" ) ) ;
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
		frame.setIgnoreRepaint( true ) ;

		renderer.hookToFrame( frame ) ;

		// Hook Input System with GUI Canvas
		renderer.getCanvas().addMouseListener( inputSystem ) ;
		renderer.getCanvas().addMouseMotionListener( inputSystem ) ;
		renderer.getCanvas().addMouseWheelListener( inputSystem ) ;
		renderer.getCanvas().addKeyListener( inputSystem ) ;

		frame.validate() ;
		frame.setVisible( true ) ;

		setDisplayDimensions( renderer.renderInfo.getDisplayDimensions() ) ;
		frame.setResizable( false ) ;
	}

	public void startSystem() {}

	public void stopSystem() {}

	public void shutdownSystem() {}

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
		final JFrame temp = new JFrame() ;
		temp.pack() ;

		final Insets insets = temp.getInsets() ;
		final Dimension dim = new Dimension( insets.left + insets.right + ( int )_display.x,
										  insets.top + insets.bottom + ( int )_display.y ) ;

		//System.out.println( "Set Display: " + _display ) ;
		frame.setVisible( false ) ;
		renderer.setDisplayDimensions( ( int )_display.x, ( int )_display.y ) ;
		frame.setMinimumSize( dim ) ;
		frame.setSize( dim ) ;
		frame.validate() ;
		frame.setVisible( true ) ;
		//System.out.println( "FrameW: " + frame.getWidth() + " FrameH: " + frame.getHeight() ) ;
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
	public SourceGenerator getSourceGenerator()
	{
		return sourceGenerator ;
	}

	public void clear()
	{
		renderer.clear() ;
	}

	public void clearInputs()
	{
		inputSystem.clearInputs() ;
	}

	public void clearEvents()
	{
		eventSystem.clearEvents() ;
	}
	
	public boolean update()
	{
		inputSystem.update() ;
		eventSystem.update() ;
		return true ;
	}

	public void draw()
	{
		renderer.draw() ;
	}
}
