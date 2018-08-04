package com.linxonline.mallet.core ;

import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.IGameLoader ;

import com.linxonline.mallet.core.GameSystem ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;

import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.GlobalConfig ;

public abstract class AbstractStarter implements IStarter
{
	private final ISystem mainSystem  ;
	private final IGameLoader loader ;

	public AbstractStarter( final ISystem _main, final IGameLoader _loader )
	{
		mainSystem = _main ;
		loader = _loader ;

		init( _main, _loader ) ;
	}

	public void init( final ISystem _main, final IGameLoader _loader )
	{
		if( _loader == null )
		{
			Logger.println( "No game loader specified..", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		loadFileSystem( _main.getFileSystem() ) ;

		loadConfig() ;						// Load the config @ base/config.cfg using the default ConfigParser.
		_main.initSystem() ;				// Fully init the backend: Input, OpenGL, & OpenAL.

		// Load the Game-States into the Game-System
		if( loadGame( getMainSystem(), _loader ) == false )
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
	public void loadConfig()
	{
		final GameSettings settings = getGameSettings() ;
	
		Logger.println( "Setting up home.", Logger.Verbosity.MINOR ) ;
		GlobalHome.setHome( settings.getApplicationName() ) ;
		GlobalHome.copy( Tuple.<String, String>build( settings.getConfigLocation(), settings.getConfigLocation() ) ) ;

		Logger.println( "Loading configuration file.", Logger.Verbosity.MINOR ) ;
		final ConfigParser parser = new ConfigParser() ;		// Extend ConfigParser to implement custom settings
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( GlobalHome.getFile( settings.getConfigLocation() ) ), new Settings() ) ) ;

		GlobalConfig.addString( "APPLICATION_NAME", settings.getApplicationName() ) ;

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
		Load the Game States defined in IGameLoader into the 
		GameSystem.
		return false if the GameSystem or Game Loader is not 
		specified.
	*/
	public boolean loadGame( final ISystem _system, final IGameLoader _loader )
	{
		Logger.println( "Loading game states.", Logger.Verbosity.MINOR ) ;
		if( _system != null && _loader != null )
		{
			final IGameSystem gameSystem = _system.getGameSystem() ;
			_loader.loadGame( gameSystem ) ;
			return true ;
		}

		return false ;
	}

	@Override
	public IGameLoader getGameLoader()
	{
		return loader ;
	}

	/**
		Will return the GameSettings defined by the IGameLoader.
		Can be overridden to implement platform specific requirements.
	*/
	@Override
	public GameSettings getGameSettings()
	{
		return getGameLoader().getGameSettings() ;
	}

	@Override
	public ISystem getMainSystem()
	{
		return mainSystem ;
	}
}
