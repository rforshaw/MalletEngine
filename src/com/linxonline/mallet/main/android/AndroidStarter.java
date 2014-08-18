package com.linxonline.mallet.main.android ;

import android.util.DisplayMetrics ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.main.StarterInterface ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameLoader ;
import com.linxonline.mallet.game.test.GameTestLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.system.android.AndroidSystem ;

public class AndroidStarter extends StarterInterface
{
	protected final SystemInterface backendSystem  ;
	protected final GameSystem gameSystem ;

	protected final static String BASE_CONFIG = "base/config.cfg" ;

	public AndroidStarter( final AndroidActivity _activity )
	{
		backendSystem = new AndroidSystem( _activity ) ;
		gameSystem = new GameSystem( backendSystem ) ;
	}

	@Override
	public void init()
	{
		loadFileSystem( new AndroidFileSystem( ( ( AndroidSystem )backendSystem ).activity ) ) ;						// Ensure FileSystem is setup correctly.
		loadConfig() ;							// Load the config @ base/config.cfg using the default ConfigParser.
		backendSystem.initSystem() ;			// Fully init the backend: Input, OpenGL, & OpenAL.
		setRenderSettings( backendSystem ) ;

		// Load the Game-States into the Game-System
		if( loadGame( gameSystem, getGameLoader() ) == false )
		{
			Logger.println( "Failed to load game..", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
		gameSystem.runSystem() ;			// Begin running the game-loop
		backendSystem.shutdownSystem() ;	// Ensure all base systems are destroyed before exiting
	}

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
		GlobalFileSystem.scanBaseDirectory() ;				// Map base-folder for faster access
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
		final DisplayMetrics display = new DisplayMetrics() ;
		( ( AndroidSystem )backendSystem ).activity.getWindowManager().getDefaultDisplay().getMetrics( display ) ; 
		final int width = display.widthPixels ;
		final int height = display.heightPixels ;

		final int renderWidth = width ;
		final int renderHeight = height ;

		_system.setDisplayDimensions( new Vector2( width, height ) ) ;
		_system.setRenderDimensions( new Vector2( renderWidth, renderHeight ) ) ;
		_system.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;

		final Settings config = new Settings() ;
		config.addInteger( "RENDERWIDTH", renderWidth ) ;
		config.addInteger( "RENDERHEIGHT", renderHeight ) ;

		/*int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", 640 ) ;
		int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", 480 ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		_system.setDisplayDimensions( new Vector2( displayWidth, displayHeight ) ) ;
		_system.setRenderDimensions( new Vector2( renderWidth, renderHeight ) ) ;
		_system.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;*/
	}

	public SystemInterface getAndroidSystem()
	{
		return backendSystem ;
	}

	@Override
	protected GameLoader getGameLoader()
	{
		return new GameTestLoader() ;
	}
}