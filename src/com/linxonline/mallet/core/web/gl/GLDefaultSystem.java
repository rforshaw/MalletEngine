package com.linxonline.mallet.core.web.gl ;

import org.teavm.jso.browser.Window ;
import org.teavm.jso.browser.AnimationFrameCallback ;

import com.linxonline.mallet.io.filesystem.web.WebFileSystem ;
import com.linxonline.mallet.audio.web.AudioSourceGenerator ;
import com.linxonline.mallet.renderer.web.gl.GLRenderer ;
import com.linxonline.mallet.input.web.InputSystem ;
import com.linxonline.mallet.core.web.WebGameSystem ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.event.* ;

import com.linxonline.mallet.util.Logger ;

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
			   new EventSystem(),
			   new InputSystem(),
			   new WebFileSystem(),
			   new WebGameSystem() ) ;
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

		final EventSystem event = getEventSystem() ;
		event.addEvent( new Event<Boolean>( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
		event.addEvent( new Event<Boolean>( "SYSTEM_FULLSCREEN",    GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ) ;
	}

	protected void initEventProcessors()
	{
		eventController.addProcessor( "DISPLAY_SYSTEM_MOUSE", ( final Boolean _displayMouse ) ->
		{
			final boolean displayMouse = _displayMouse ;
			//getWindow().setPointerVisible( displayMouse ) ;
		} ) ;

		getEventSystem().addHandler( eventController ) ;
	}

	@Override
	public void sleep( final long _millis ) {}

	@Override
	public void startSystem()
	{
		Logger.println( "Start System...", Logger.Verbosity.MINOR ) ;
	}

	@Override
	public void stopSystem()
	{
		Logger.println( "Start System...", Logger.Verbosity.MINOR ) ;
	}

	@Override
	public boolean update( final float _dt )
	{
		super.update( _dt ) ;
		eventController.update() ;		// Process the Events this system is interested in
		return true ;					// Informs the Game System whether to continue updating or not.
	}
}
