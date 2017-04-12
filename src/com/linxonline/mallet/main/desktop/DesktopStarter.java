package com.linxonline.mallet.main.desktop ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.main.AbstractStarter ;
import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.GameSettings ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.renderer.RenderInfo ;

import com.linxonline.mallet.system.desktop.gl.GLDefaultSystem ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.inspect.desktop.DesktopDisplay ;
import com.linxonline.mallet.util.inspect.ScreenMode ;

import com.linxonline.mallet.ui.UI ;
import com.linxonline.mallet.ui.UIRatio ;

/**
	The Desktop implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	desktop platforms. Override getGameLoader, for it 
	to load your game.
	Example: DesktopTestStarter.
*/
public abstract class DesktopStarter extends AbstractStarter
{
	protected Thread thread ;

	public DesktopStarter()
	{
		this( new GLDefaultSystem() ) ;
	}

	public DesktopStarter( final SystemInterface _mainSystem )
	{
		super( _mainSystem ) ;
		final ShutdownDelegate delegate = _mainSystem.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			public void shutdown()
			{
				stop() ;
				System.exit( 0 ) ;
			}
		} ) ;
	}

	public void run()
	{
		final SystemInterface main = getMainSystem() ;
		final GameSystem game = getGameSystem() ;

		main.startSystem() ;
		thread = new Thread( "GAME_THREAD" )
		{
			public void run()
			{
				Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
				game.runSystem() ;			// Begin running the game-loop
				Logger.println( "Stopping...", Logger.Verbosity.MINOR ) ;
			}
		} ;

		thread.start() ;
	}

	public void stop()
	{
		if( thread == null )
		{
			return ;
		}

		System.out.println( "Game System slowing.." ) ;
		getGameSystem().stopSystem() ;
		if( thread.isAlive() == true )
		{
			try
			{
				thread.join( 10 ) ;
				thread = null ;
			}
			catch( InterruptedException ex )
			{
				ex.printStackTrace() ;
			}
		}

		System.out.println( "Backend stopped.." ) ;
		getMainSystem().stopSystem() ;
	}

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	@Override
	public void setRenderSettings( final SystemInterface _system )
	{
		final GameSettings game = getGameSettings() ;

		int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", game.getWindowWidth() ) ;
		int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", game.getWindowHeight() ) ;

		final DesktopDisplay desktop = new DesktopDisplay() ;

		if( GlobalConfig.getBoolean( "FULLSCREEN", false ) == true )
		{
			final ScreenMode screen = desktop.getScreens()[0].getBestScreenMode() ;
			displayWidth = screen.getWidth() ;
			displayHeight = screen.getHeight() ;

			//System.out.println( desktop.getScreens()[0] ) ;
			//System.out.println( "Display: " + displayWidth + " " + displayHeight ) ;
		}

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", game.getRenderWidth() ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", game.getRenderHeight() ) ;

		//System.out.println( "Render Settings Display: " + renderWidth + " " + renderHeight ) ;
		//System.out.println( "Render Settings Display: " + displayWidth + " " + displayHeight ) ;

		final RenderInterface render = _system.getRenderer() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;
		render.setRenderDimensions( renderWidth, renderHeight ) ;

		final RenderInfo info = render.getRenderInfo() ;
		info.setKeepRenderRatio( GlobalConfig.getBoolean( "KEEPRATIO", true ) ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", desktop.getDPI() ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", desktop.getDPI() ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}
}
