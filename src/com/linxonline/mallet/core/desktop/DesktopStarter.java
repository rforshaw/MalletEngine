package com.linxonline.mallet.core.desktop ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.core.IStarter ;
import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.IRender ;
import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.core.desktop.gl.GLDefaultSystem ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.inspect.desktop.DesktopDisplay ;
import com.linxonline.mallet.util.inspect.ScreenMode ;

import com.linxonline.mallet.ui.UI ;
import com.linxonline.mallet.ui.UIRatio ;

/**
	The Desktop implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	desktop platforms.
*/
public class DesktopStarter extends IStarter
{
	protected Thread thread ;

	public DesktopStarter( final IGameLoader _loader )
	{
		this( new GLDefaultSystem(), _loader ) ;
	}

	public DesktopStarter( final ISystem _main, final IGameLoader _loader )
	{
		super( _main, _loader ) ;
		init() ;
	}

	public void init()
	{
		IStarter.init( this ) ;
		DesktopStarter.setRenderSettings( this ) ;

		final ISystem main = getMainSystem() ;
		final ShutdownDelegate delegate = main.getShutdownDelegate() ;
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
		final ISystem main = getMainSystem() ;
		final IGameSystem game = main.getGameSystem() ;

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

		final ISystem main = getMainSystem() ;
		final IGameSystem game = main.getGameSystem() ;

		System.out.println( "Game System slowing.." ) ;
		game.stopSystem() ;
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

		int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", game.getWindowWidth() ) ;
		int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", game.getWindowHeight() ) ;

		final DesktopDisplay desktop = new DesktopDisplay() ;
		if( GlobalConfig.getBoolean( "FULLSCREEN", false ) == true )
		{
			final ScreenMode screen = desktop.getScreens()[0].getBestScreenMode() ;
			displayWidth = screen.getWidth() ;
			displayHeight = screen.getHeight() ;
		}

		final IRender render = main.getRenderer() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", game.getRenderWidth() ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", game.getRenderHeight() ) ;
		updateRenderDimensions( renderWidth, renderHeight ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", desktop.getDPI() ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", desktop.getDPI() ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}

	private static void updateRenderDimensions( final int _width, final int _height )
	{
		final Camera camera = CameraAssist.getDefaultCamera() ;
		CameraAssist.amendScreenResolution( camera, _width, _height ) ;
		CameraAssist.amendOrthographic( camera, 0.0f, _height, 0.0f, _width, -1000.0f, 1000.0f ) ;
		
		final World world = WorldAssist.getDefaultWorld() ;
		WorldAssist.setRenderDimensions( world, 0, 0, _width, _height ) ;
	}
}
