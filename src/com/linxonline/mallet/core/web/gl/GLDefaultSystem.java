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
		event.addEvent( Event.<Boolean>create( "DISPLAY_SYSTEM_MOUSE", GlobalConfig.getBoolean( "DISPLAYMOUSE", false ) ) ) ;
		event.addEvent( Event.<Boolean>create( "CAPTURE_SYSTEM_MOUSE", GlobalConfig.getBoolean( "CAPTUREMOUSE", false ) ) ) ;
		event.addEvent( Event.<Boolean>create( "SYSTEM_FULLSCREEN",    GlobalConfig.getBoolean( "FULLSCREEN", false ) ) ) ;
	}

	protected void initEventProcessors()
	{
		controller.addProcessor( "DISPLAY_SYSTEM_MOUSE", ( final Boolean _displayMouse ) ->
		{
			final boolean displayMouse = _displayMouse ;
			//getWindow().setPointerVisible( displayMouse ) ;
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
		Logger.println( "Start System...", Logger.Verbosity.MINOR ) ;
	}
}
