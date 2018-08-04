package com.linxonline.mallet.core.web.gl ;

import com.linxonline.mallet.io.filesystem.web.WebFileSystem ;
import com.linxonline.mallet.audio.web.AudioSourceGenerator ;
import com.linxonline.mallet.renderer.web.gl.GLRenderer ;
import com.linxonline.mallet.input.web.InputSystem ;
import com.linxonline.mallet.core.web.WebGameSystem ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.renderer.web.WebShape ;
import com.linxonline.mallet.renderer.Shape ;

/**
	Central location for WebGL Applications.
*/
public class GLDefaultSystem extends BasicSystem<WebFileSystem,
												 DefaultShutdown,
												 GLRenderer,
												 AudioSourceGenerator,
												 InputSystem,
												 EventSystem,
												 WebGameSystem>
{
	// Add Web Document here...
	protected EventController eventController = new EventController() ;

	public GLDefaultSystem()
	{
		super( new DefaultShutdown(),
			   new GLRenderer(),
			   new AudioSourceGenerator(),
			   new EventSystem( "ROOT_EVENT_SYSTEM" ),
			   new InputSystem(),
			   new WebFileSystem(),
			   new WebGameSystem() ) ;

		getGameSystem().setMainSystem( this ) ;
	}

	@Override
	public void initSystem()
	{
		Shape.setFactory( new WebShape.Factory() ) ;
		initEventProcessors() ;

		final GLRenderer render = getRenderer() ;
		render.start() ;

		getAudioGenerator().startGenerator() ;

		final InputSystem input = getInput() ;

		final EventSystem event = getEventSystem() ;
		event.addEvent( new Event<Boolean>( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "SYSTEM_FULLSCREEN",    GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ) ;
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

		getEventSystem().addEventHandler( eventController ) ;
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
	public void sleep( final long _millis )
	{
		/*try
		{
			Thread.sleep( _millis ) ;
			Thread.yield() ;
		}
		catch( InterruptedException ex )
		{
			Thread.currentThread().interrupt() ;
			//ex.printStackTrace() ;
		}*/
	}

	@Override
	public boolean update( final float _dt )
	{
		super.update( _dt ) ;
		eventController.update() ;		// Process the Events this system is interested in
		return true ;					// Informs the Game System whether to continue updating or not.
	}
}
