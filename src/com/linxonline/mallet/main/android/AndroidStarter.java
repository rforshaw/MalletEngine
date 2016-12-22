package com.linxonline.mallet.main.android ;

import android.util.DisplayMetrics ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.main.StarterInterface ;
import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.test.GameTestLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.RenderInterface ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.system.android.gl.GLAndroidSystem ;
import com.linxonline.mallet.renderer.android.GL.* ;

public abstract class AndroidStarter extends StarterInterface
{
	protected final SystemInterface backendSystem  ;
	protected final GameSystem gameSystem ;

	protected final static String BASE_CONFIG = "base/config.cfg" ;

	public AndroidStarter( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		backendSystem = new GLAndroidSystem( _activity, _notify ) ;
		gameSystem = new GameSystem( backendSystem ) ;
	}

	@Override
	public void init()
	{
		loadFileSystem( new AndroidFileSystem( ( ( GLAndroidSystem )backendSystem ).activity ) ) ;						// Ensure FileSystem is setup correctly.
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
		setRenderSettings( backendSystem ) ;

		Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
		gameSystem.runSystem() ;			// Begin running the game-loop
		Logger.println( "Stopped...", Logger.Verbosity.MINOR ) ;
	}

	public void stop()
	{
		gameSystem.stopSystem() ;
		backendSystem.stopSystem() ;
	}

	public void shutdown()
	{
		backendSystem.shutdownSystem() ;
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

		Logger.println( "Mapping Base directory.", Logger.Verbosity.MINOR ) ;
		if( GlobalFileSystem.mapDirectory( "base" ) == false )				// Map base-folder for faster access
		{
			Logger.println( "Failed to map base directory.", Logger.Verbosity.MINOR ) ;
		}
	}

	@Override
	protected void loadConfig()
	{
		Logger.println( "Setting up home.", Logger.Verbosity.MINOR ) ;
		GlobalHome.setHome( getApplicationName() ) ;
		GlobalHome.copy( Tuple.build( BASE_CONFIG, BASE_CONFIG ) ) ;

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
		final RenderInterface render = _system.getRenderInterface() ;
	}

	public SystemInterface getAndroidSystem()
	{
		return backendSystem ;
	}
}
