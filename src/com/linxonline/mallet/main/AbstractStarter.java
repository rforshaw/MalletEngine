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

	public AbstractStarter( final SystemInterface _main )
	{
		mainSystem = _main ;
		gameSystem = new GameSystem( mainSystem ) ;
	}

	@Override
	public void init()
	{
		final SystemInterface main = getMainSystem() ;
		loadFileSystem( mainSystem.getFileSystem() ) ;

		loadConfig() ;							// Load the config @ base/config.cfg using the default ConfigParser.
		setRenderSettings( main ) ;
		main.initSystem() ;			// Fully init the backend: Input, OpenGL, & OpenAL.

		// Load the Game-States into the Game-System
		final GameSystem game = getGameSystem() ;
		if( loadGame( game, getGameLoader() ) == false )
		{
			Logger.println( "Failed to load game..", Logger.Verbosity.MAJOR ) ;
			return ;
		}
	}

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

	@Override
	public void loadConfig()
	{
		final GameSettings game = getGameSettings() ;
		final SystemInterface main = getMainSystem() ;

		Logger.println( "Setting up home.", Logger.Verbosity.MINOR ) ;
		GlobalHome.setHome( game.getApplicationName() ) ;
		GlobalHome.copy( Tuple.<String, String>build( game.getConfigLocation(), game.getConfigLocation() ) ) ;

		Logger.println( "Loading configuration file.", Logger.Verbosity.MINOR ) ;
		final ConfigParser parser = new ConfigParser() ;		// Extend ConfigParser to implement custom settings
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( GlobalHome.getFile( game.getConfigLocation() ) ), new Settings() ) ) ;

		final ShutdownDelegate delegate = main.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			@Override
			public void shutdown()
			{
				Logger.println( "Saving configuration file.", Logger.Verbosity.MINOR ) ;
				if( ConfigWriter.write( GlobalHome.getFile( game.getConfigLocation() ), GlobalConfig.getConfig() ) == false )
				{
					Logger.println( "Failed to write configuration file.", Logger.Verbosity.MAJOR ) ;
				}
			}
		} ) ;
	}

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

	public SystemInterface getMainSystem()
	{
		return mainSystem ;
	}

	public GameSystem getGameSystem()
	{
		return gameSystem ;
	}
}
