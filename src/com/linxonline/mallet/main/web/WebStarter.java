package com.linxonline.mallet.main.web ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.main.StarterInterface ;
import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.RenderInterface ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;

import com.linxonline.mallet.util.time.ElapsedTimer ;
import com.linxonline.mallet.util.time.web.WebTimer ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;

/**
	The Web implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	web based platforms. Override getGameLoader, for it 
	to load your game.
	Example: WebTestStarter.
*/
public abstract class WebStarter extends StarterInterface
{
	protected final SystemInterface backendSystem  ;
	protected final GameSystem gameSystem ;

	protected final static String BASE_CONFIG = "base/config.cfg" ;

	public WebStarter( final SystemInterface _backendSystem, final FileSystem _fileSystem )
	{
		ElapsedTimer.setTimer( new WebTimer() ) ;

		loadFileSystem( _fileSystem ) ;						// Ensure FileSystem is setup correctly.
		backendSystem = _backendSystem ;
		gameSystem = new GameSystem( _backendSystem )
		{
			private final TimerHandler singleStep = new TimerHandler()
			{
				long startTime = ElapsedTimer.nanoTime() ;
				long dt = 0L ;

				@Override
				public void onTimer()
				{
					if( running == true )
					{
						stateMachine.update( ( double )( dt * 0.001 ) ) ;			// Update Game State

						long endTime = ElapsedTimer.nanoTime() ;
						dt = endTime - startTime ;

						long sleep = 16L - dt ;
						sleep = ( sleep > 0 ) ? sleep : 0L ;

						// Calculate when the next timestep should be issued.
						// We'll operate on a 60Hz timestep.
						Window.setTimeout( singleStep, sleep ) ;
						startTime = ElapsedTimer.nanoTime() ;
						return ;
					}

					stateMachine.pause() ;
				}
			} ;
		
			@Override
			public void runSystem()
			{
				running = true ;
				stateMachine.resume() ;
				Window.setTimeout( singleStep, 0 ) ;
			}
		} ;
	}

	@Override
	public void init()
	{
		loadConfig() ;							// Load the config @ base/config.cfg using the default ConfigParser.
		backendSystem.initSystem() ;			// Fully init the backend: Input, OpenGL, & OpenAL.

		// Load the Game-States into the Game-System
		if( loadGame( gameSystem, getGameLoader() ) == false )
		{
			Logger.println( "Failed to load game..", Logger.Verbosity.MAJOR ) ;
			return ;
		}
	}

	public void run()
	{
		backendSystem.startSystem() ;
		setRenderSettings( backendSystem ) ;

		Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
		gameSystem.runSystem() ;			// Begin running the game-loop
		Logger.println( "Stopping...", Logger.Verbosity.MINOR ) ;
	}

	public void stop()
	{
		gameSystem.stopSystem() ;
		backendSystem.stopSystem() ;
	}

	@Override
	protected abstract GameLoader getGameLoader() ;

	@Override
	protected boolean loadGame( final GameSystem _system, final GameLoader _loader )
	{
		Logger.println( "Loading game states.", Logger.Verbosity.MINOR ) ;
		if( _system != null && _loader != null )
		{
			_loader.loadGame( _system ) ;
			return true ;
		}

		return false ;
	}

	@Override
	protected void loadFileSystem( final FileSystem _fileSystem )
 	{
		Logger.println( "Finalising filesystem.", Logger.Verbosity.MINOR ) ;
		GlobalFileSystem.setFileSystem( _fileSystem ) ;
		Logger.println( "Mapping Base directory.", Logger.Verbosity.MINOR ) ;

		if( GlobalFileSystem.mapDirectory( "base/" ) == false )				// Map base-folder for faster access
		{
			Logger.println( "Failed to map base directory.", Logger.Verbosity.MINOR ) ;
		}
	}

	@Override
	protected void loadConfig()
	{
		Logger.println( "Loading configuration file.", Logger.Verbosity.MINOR ) ;
		final ConfigParser parser = new ConfigParser() ;		// Extend ConfigParser to implement custom settings
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( BASE_CONFIG ), new Settings() ) ) ;
	}

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	@Override
	protected void setRenderSettings( final SystemInterface _system )
	{
		int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", 640 ) ;
		int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", 480 ) ;

		/*if( GlobalConfig.getBoolean( "FULLSCREEN", false ) == true )
		{
			final DesktopDisplay desktop = new DesktopDisplay() ;
			final ScreenMode screen = desktop.getScreens()[0].getBestScreenMode() ;
			displayWidth = screen.getWidth() ;
			displayHeight = screen.getHeight() ;
		}*/

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		final RenderInterface render = _system.getRenderInterface() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;
		render.setRenderDimensions( renderWidth, renderHeight ) ;
	}
}