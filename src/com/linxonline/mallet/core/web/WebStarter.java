package com.linxonline.mallet.main.web ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.core.AbstractStarter ;
import com.linxonline.mallet.core.GameSystem ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.IRender ;

import com.linxonline.mallet.core.web.gl.GLDefaultSystem ;

import com.linxonline.mallet.util.time.ElapsedTimer ;
import com.linxonline.mallet.util.time.web.WebTimer ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.ui.UI ;
import com.linxonline.mallet.ui.UIRatio ;

/**
	The Web implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	web based platforms.
*/
public class WebStarter extends AbstractStarter
{
	public WebStarter( final IGameLoader _loader )
	{
		this( new GLDefaultSystem(), _loader ) ;
	}

	public WebStarter( final ISystem _mainSystem, final IGameLoader _loader )
	{
		super( _mainSystem, _loader ) ;
		ElapsedTimer.setTimer( new WebTimer() ) ;
	}

	public void run()
	{
		final ISystem main = getMainSystem() ;
		final GameSystem game = getGameSystem() ;

		main.startSystem() ;
		Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
		game.runSystem() ;			// Begin running the game-loop
		Logger.println( "Stopping...", Logger.Verbosity.MINOR ) ;
	}

	public void stop()
	{
		System.out.println( "Game System slowing.." ) ;
		getGameSystem().stopSystem() ;

		System.out.println( "Backend stopped.." ) ;
		getMainSystem().stopSystem() ;
	}

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	public void setRenderSettings( final ISystem _system )
	{
		final int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", 640 ) ;
		final int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", 480 ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		final IRender render = _system.getRenderer() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;
		render.setRenderDimensions( renderWidth, renderHeight ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", 90 ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", 90 ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}
}
