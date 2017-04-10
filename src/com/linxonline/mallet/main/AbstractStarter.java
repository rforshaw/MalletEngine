package com.linxonline.mallet.main ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.GameSettings ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate ;
import com.linxonline.mallet.system.GlobalConfig ;

public abstract class AbstractStarter implements IStarter
{
	private final SystemInterface mainSystem  ;
	private final GameSystem gameSystem ;
	private GameSettings settings ;

	public AbstractStarter( final SystemInterface _main )
	{
		mainSystem = _main ;
		gameSystem = new GameSystem( mainSystem ) ;
	}

	@Override
	public void init()
	{
		final GameLoader loader = getGameLoader() ;
		if( loader == null )
		{
			Logger.println( "No game loader specified..", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		settings = loader.getGameSettings() ;
		loadFileSystem( mainSystem.getFileSystem() ) ;

		loadConfig() ;							// Load the config @ base/config.cfg using the default ConfigParser.
		setRenderSettings( mainSystem ) ;
		mainSystem.initSystem() ;				// Fully init the backend: Input, OpenGL, & OpenAL.

		// Load the Game-States into the Game-System
		if( loadGame( getGameSystem(), loader ) == false )
		{
			Logger.println( "Failed to load game..", Logger.Verbosity.MAJOR ) ;
			return ;
		}
	}

	/**
		Set the GlobalFileSystem to the file-system specified in 
		SystemInterface - map the base directory to allow for 
		quick lookup.
	*/
	@Override
	public void loadFileSystem( final FileSystem _fileSystem )
 	{
		Logger.println( "Finalising filesystem.", Logger.Verbosity.MINOR ) ;
		GlobalFileSystem.setFileSystem( _fileSystem ) ;

		Logger.println( "Mapping Base directory.", Logger.Verbosity.MINOR ) ;
		if( GlobalFileSystem.mapDirectory( "base" ) == false )				// Map base-folder for faster access
		{
			Logger.println( "Failed to map base directory.", Logger.Verbosity.MINOR ) ;
		}
	}

	/**
		Load the config file specified in game settings.
		Copy the config from the applications read-only directory and 
		move it to the user home directory.
		Set GlobalConfig to the user home-directory config file.
		Initialise a ShutdownCallback to save the GlobalConfig back to 
		the users home-directory if the config changes while playing.
	*/
	@Override
	public void loadConfig()
	{
		Logger.println( "Setting up home.", Logger.Verbosity.MINOR ) ;
		GlobalHome.setHome( settings.getApplicationName() ) ;
		GlobalHome.copy( Tuple.<String, String>build( settings.getConfigLocation(), settings.getConfigLocation() ) ) ;

		Logger.println( "Loading configuration file.", Logger.Verbosity.MINOR ) ;
		final ConfigParser parser = new ConfigParser() ;		// Extend ConfigParser to implement custom settings
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( GlobalHome.getFile( settings.getConfigLocation() ) ), new Settings() ) ) ;

		final ShutdownDelegate delegate = mainSystem.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			@Override
			public void shutdown()
			{
				Logger.println( "Saving configuration file.", Logger.Verbosity.MINOR ) ;
				if( ConfigWriter.write( GlobalHome.getFile( settings.getConfigLocation() ), GlobalConfig.getConfig() ) == false )
				{
					Logger.println( "Failed to write configuration file.", Logger.Verbosity.MAJOR ) ;
				}
			}
		} ) ;
	}

	/**
		Load the Game States defined in GameLoader into the 
		GameSystem.
		return false if the GameSystem or Game Loader is not 
		specified.
	*/
	@Override
	public boolean loadGame( final GameSystem _system, final GameLoader _loader )
	{
		Logger.println( "Loading game states.", Logger.Verbosity.MINOR ) ;
		if( _system != null && _loader != null )
		{
			_loader.loadGame( _system ) ;
			return true ;
		}

		return false ;
	}

	/**
		Will return the GameSettings defined by the GameLoader.
		Can be overridden to implement platform specific requirements.
	*/
	@Override
	public GameSettings getGameSettings()
	{
		return settings ;
	}

	public SystemInterface getMainSystem()
	{
		return mainSystem ;
	}

	public GameSystem getGameSystem()
	{
		return gameSystem ;
	}
}
