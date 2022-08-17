package com.linxonline.mallet.core.web ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.core.IStarter ;
import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.IRender ;
import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.core.web.gl.GLDefaultSystem ;

import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.ui.UI ;
import com.linxonline.mallet.ui.UIRatio ;

/**
	The Web implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	web based platforms.
*/
public class WebStarter extends IStarter
{
	public WebStarter( final IGameLoader _loader )
	{
		this( new GLDefaultSystem(), _loader ) ;
	}

	public WebStarter( final ISystem _mainSystem, final IGameLoader _loader )
	{
		super( _mainSystem, _loader ) ;
	}

	public void init()
	{
		IStarter.init( this ) ;
		WebStarter.setRenderSettings( this ) ;

		final ISystem main = getMainSystem() ;
		final ShutdownDelegate delegate = main.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			@Override
			public void shutdown()
			{
				System.out.println( "Shutting down..." ) ;
				stop() ;
				//System.exit( 0 ) ;
			}
		} ) ;
	}

	public void run()
	{
		final ISystem main = getMainSystem() ;
		final IGameSystem game = main.getGameSystem() ;

		main.startSystem() ;
		Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
		game.runSystem() ;			// Begin running the game-loop
		//Logger.println( "Stopping...", Logger.Verbosity.MINOR ) ;
	}

	public void stop()
	{
		final ISystem main = getMainSystem() ;
		final IGameSystem game = main.getGameSystem() ;
	
		System.out.println( "Game System slowing.." ) ;
		game.stopSystem() ;

		System.out.println( "Backend stopped.." ) ;
		main.stopSystem() ;
	}

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	public static void setRenderSettings( final IStarter _starter )
	{
		final ISystem main = _starter.getMainSystem() ;
		final GameSettings game = _starter.getGameLoader().getGameSettings() ;
	
		final int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", 640 ) ;
		final int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", 480 ) ;

		final IRender render = main.getRenderer() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		final Camera camera = CameraAssist.getDefault() ;
		camera.setOrthographic( 0.0f, renderHeight, 0.0f, renderWidth, -1000.0f, 1000.0f ) ;
		CameraAssist.update( camera ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", 90 ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", 90 ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}
}
