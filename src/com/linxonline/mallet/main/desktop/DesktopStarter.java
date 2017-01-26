package com.linxonline.mallet.main.desktop ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.main.StarterInterface ;
import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.renderer.RenderInfo ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.inspect.desktop.DesktopDisplay ;
import com.linxonline.mallet.util.inspect.ScreenMode ;

/**
	The Desktop implementation of the Starter Interface.
	Handles the initialisation & loading stage for 
	desktop platforms. Override getGameLoader, for it 
	to load your game.
	Example: DesktopTestStarter.
*/
public abstract class DesktopStarter extends StarterInterface
{
	protected final SystemInterface backendSystem  ;
	protected final GameSystem gameSystem ;
	protected Thread thread ;

	protected final static String BASE_CONFIG = "base/config.cfg" ;

	public DesktopStarter( final SystemInterface _backendSystem, final FileSystem _fileSystem )
	{
		loadFileSystem( _fileSystem ) ;						// Ensure FileSystem is setup correctly.
		backendSystem = _backendSystem ;
		gameSystem = new GameSystem( _backendSystem ) ;

		final ShutdownDelegate delegate = _backendSystem.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			public void shutdown()
			{
				stop() ;
				System.exit( 0 ) ;
			}
		} ) ;
	}

	@Override
	public void init()
	{
		loadConfig() ;							// Load the config @ base/config.cfg using the default ConfigParser.
		setRenderSettings( backendSystem ) ;
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
		thread = new Thread( "GAME_THREAD" )
		{
			public void run()
			{
				Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
				gameSystem.runSystem() ;			// Begin running the game-loop
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
		backendSystem.stopSystem() ;
	}

	@Override
	protected abstract String getApplicationName() ;

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
		Logger.println( "Setting up home.", Logger.Verbosity.MINOR ) ;
		GlobalHome.setHome( getApplicationName() ) ;
		GlobalHome.copy( Tuple.<String, String>build( BASE_CONFIG, BASE_CONFIG ) ) ;

		Logger.println( "Loading configuration file.", Logger.Verbosity.MINOR ) ;
		final ConfigParser parser = new ConfigParser() ;		// Extend ConfigParser to implement custom settings
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( GlobalHome.getFile( BASE_CONFIG ) ), new Settings() ) ) ;

		final ShutdownDelegate delegate = backendSystem.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			@Override
			public void shutdown()
			{
				Logger.println( "Saving configuration file.", Logger.Verbosity.MINOR ) ;
				if( ConfigWriter.write( GlobalHome.getFile( BASE_CONFIG ), GlobalConfig.getConfig() ) == false )
				{
					Logger.println( "Failed to write configuration file.", Logger.Verbosity.MAJOR ) ;
				}
			}
		} ) ;
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

		if( GlobalConfig.getBoolean( "FULLSCREEN", false ) == true )
		{
			final DesktopDisplay desktop = new DesktopDisplay() ;
			final ScreenMode screen = desktop.getScreens()[0].getBestScreenMode() ;
			displayWidth = screen.getWidth() ;
			displayHeight = screen.getHeight() ;

			//System.out.println( desktop.getScreens()[0] ) ;
			//System.out.println( "Display: " + displayWidth + " " + displayHeight ) ;
		}

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		//System.out.println( "Render Settings Display: " + renderWidth + " " + renderHeight ) ;
		//System.out.println( "Render Settings Display: " + displayWidth + " " + displayHeight ) ;

		final RenderInterface render = _system.getRenderInterface() ;
		render.setDisplayDimensions( displayWidth, displayHeight ) ;
		render.setRenderDimensions( renderWidth, renderHeight ) ;

		final RenderInfo info = render.getRenderInfo() ;
		info.setKeepRenderRatio( GlobalConfig.getBoolean( "KEEPRATIO", true ) ) ;
	}
}
