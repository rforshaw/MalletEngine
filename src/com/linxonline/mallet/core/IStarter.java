package com.linxonline.mallet.core ;

import com.linxonline.mallet.core.GameSettings ;
import com.linxonline.mallet.core.IGameSystem ;
import com.linxonline.mallet.core.IGameLoader ;
import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.reader.config.ConfigParser ;

/**
	Each platform supported should implement this interface.
	Allows the developer to define what and how things 
	should be loaded on start. Direct implementations of this 
	class should be abstract.
	Example: DesktopStarter.java
*/
public class IStarter
{
	private final ISystem mainSystem  ;
	private final IGameLoader loader ;

	public IStarter( final ISystem _main, final IGameLoader _loader )
	{
		mainSystem = _main ;
		loader = _loader ;
	}
	
	public IGameLoader getGameLoader()
	{
		return loader ;
	}

	public ISystem getMainSystem()
	{
		return mainSystem ;
	}

	/**
		Using the passed in IStarter load the default configuration file.
		Initialise the main system and start loading games states from 
		the defined game Loader.
	*/
	public static boolean init( IStarter _starter )
	{
		final ISystem main = _starter.getMainSystem() ;
		final IGameLoader loader = _starter.getGameLoader() ;

		IStarter.loadConfig( loader.getGameSettings(), main ) ;		// Load the config @ base/config.cfg using the default ConfigParser.
		main.initSystem() ;										// Fully init the backend: Input, OpenGL, & OpenAL.

		// Load the Game-States into the Game-System
		if( IStarter.loadGame( main, loader ) == false )
		{
			Logger.println( "Failed to load game..", Logger.Verbosity.MAJOR ) ;
			return false ;
		}

		return true ;
	}

	/**
		Load the config file specified in game settings.
		Copy the config from the applications read-only directory and 
		move it to the user home directory.
		Set GlobalConfig to the user home-directory config file.
		Initialise a ShutdownCallback to save the GlobalConfig back to 
		the users home-directory if the config changes while playing.
	*/
	public static void loadConfig( final GameSettings _settings, final ISystem _main )
	{
		Logger.println( "Setting up home.", Logger.Verbosity.MINOR ) ;
		GlobalHome.setHome( _settings.getApplicationName() ) ;
		GlobalHome.copy( Tuple.<String, String>build( _settings.getConfigLocation(), _settings.getConfigLocation() ) ) ;

		Logger.println( "Loading configuration file.", Logger.Verbosity.MINOR ) ;
		final ConfigParser parser = new ConfigParser() ;		// Extend ConfigParser to implement custom settings
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( GlobalHome.getFile( _settings.getConfigLocation() ) ), new Settings() ) ) ;

		GlobalConfig.addString( "APPLICATION_NAME", _settings.getApplicationName() ) ;

		final ShutdownDelegate delegate = _main.getShutdownDelegate() ;
		delegate.addShutdownCallback( new ShutdownDelegate.Callback()
		{
			@Override
			public void shutdown()
			{
				Logger.println( "Saving configuration file.", Logger.Verbosity.MINOR ) ;
				if( ConfigWriter.write( GlobalHome.getFile( _settings.getConfigLocation() ), GlobalConfig.getConfig() ) == false )
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
	public static boolean loadGame( final ISystem _system, final IGameLoader _loader )
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
}
