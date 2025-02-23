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
	The goal of IStarter is to enable a platform specific
	way of initialising the core sub-systems of a game
	and hooking them up to the game state.
	Each platform supported should implement this interface.
	Allows the developer to define what and how things 
	should be loaded on start.
	Example: DesktopStarter.java
*/
public interface IStarter
{
	/**
		Provides the main game-loop.
		This can be implemented on a platform specific basis
		but it's recommended to use GameSystem if you can get
		away with it - some platforms such as Web doesn't like it.
	*/
	public IGameSystem getGameSystem() ;

	/**
		The game loader contains all the specific game-state
		for your game.
		It will eventually be added to the game-system.
	*/
	public IGameLoader getGameLoader() ;

	/**
		Defines the core sub-systems for a specific platform.
		This includes the input, file, and rendering systems.
	*/
	public ISystem getMainSystem() ;

	/**
		Using the passed in IStarter load the default configuration file.
		Initialise the main system and start loading games states from 
		the defined game Loader.
	*/
	public static boolean init( IStarter _starter )
	{
		final ISystem main = _starter.getMainSystem() ;
		final IGameLoader loader = _starter.getGameLoader() ;
		final IGameSystem gameSystem = _starter.getGameSystem() ;

		IStarter.loadConfig( loader.getGameSettings(), main ) ;		// Load the config @ base/config.cfg using the default ConfigParser.
		main.initSystem() ;										// Fully init the backend: Input, OpenGL, & OpenAL.

		// Load the Game-States into the Game-System
		if( IStarter.loadGame( loader, gameSystem ) == false )
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
				final String location = _settings.getConfigLocation() ;
				if( ConfigWriter.write( GlobalHome.getFile( location ), GlobalConfig.getConfig() ) == false )
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
	public static boolean loadGame( final IGameLoader _loader, final IGameSystem _gameSystem )
	{
		Logger.println( "Loading game states.", Logger.Verbosity.MINOR ) ;
		if( _loader != null && _gameSystem != null )
		{
			_loader.loadGame( _gameSystem ) ;
			return true ;
		}

		return false ;
	}
}
