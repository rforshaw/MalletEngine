package com.linxonline.mallet.core.desktop ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.core.IStarter ;
import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.core.GameSystem ;

import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.IRender ;
import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.core.desktop.gl.GLDefaultSystem ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.inspect.DisplayEnvironment ;
import com.linxonline.mallet.util.inspect.Screen ;
import com.linxonline.mallet.util.inspect.ScreenMode ;

import com.linxonline.mallet.ui.UI ;
import com.linxonline.mallet.ui.UIRatio ;

/**
	The Desktop implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	desktop platforms.
*/
public class DesktopStarter implements IStarter
{
	private final GameSystem gameSystem = new GameSystem() ;
	private final ISystem mainSystem ;
	private final IGameLoader loader ;

	protected Thread thread ;

	public DesktopStarter( final IGameLoader _loader )
	{
		this( new GLDefaultSystem(), _loader ) ;
	}

	public DesktopStarter( final ISystem _main, final IGameLoader _loader )
	{
		mainSystem = _main ;
		loader = _loader ;

		gameSystem.setMainSystem( _main ) ;
	}

	public void init()
	{
		IStarter.init( this ) ;
		DesktopStarter.setRenderSettings( this ) ;

		final ISystem main = getMainSystem() ;
		final ShutdownDelegate delegate = main.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			@Override
			public void shutdown()
			{
				System.out.println( "Shutting down..." ) ;
				stop() ;
				System.exit( 0 ) ;
			}
		} ) ;
	}

	public void run()
	{
		final ISystem main = getMainSystem() ;

		main.startSystem() ;
		thread = new Thread( "GAME_THREAD" )
		{
			public void run()
			{
				Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
				gameSystem.runSystem() ;		// Begin running the game-loop
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
		gameSystem.stopSystem() ;
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
		final ISystem main = getMainSystem() ;
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

		final int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", game.getWindowWidth() ) ;
		final int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", game.getWindowHeight() ) ;

		final DisplayEnvironment desktop = new DisplayEnvironment() ;

		final IRender render = main.getRenderer() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", game.getRenderWidth() ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", game.getRenderHeight() ) ;

		final Camera camera = CameraAssist.getDefault() ;

		final int left = -renderWidth / 2 ;
		final int right = left + renderWidth ;

		final int top = -renderHeight / 2 ;
		final int bottom = top + renderHeight ;

		camera.setOrthographic( Camera.Mode.HUD, 0, renderHeight, 0, renderWidth, -1000.0f, 1000.0f ) ;
		camera.setOrthographic( Camera.Mode.WORLD, top, bottom, left, right, -1000.0f, 1000.0f ) ;
		CameraAssist.update( camera ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", desktop.getDPI() ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", desktop.getDPI() ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}

	@Override
	public IGameSystem getGameSystem()
	{
		return gameSystem ;
	}

	@Override
	public IGameLoader getGameLoader()
	{
		return loader ;
	}

	@Override
	public ISystem getMainSystem()
	{
		return mainSystem ;
	}
}
