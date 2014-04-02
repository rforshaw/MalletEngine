package com.linxonline.mallet.main ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.ConfigParser ;
import com.linxonline.mallet.io.reader.ConfigReader ;

import com.linxonline.mallet.util.settings.Settings ;

public abstract class DesktopStarter extends StarterInterface
{
	protected final SystemInterface backendSystem  ;
	protected final GameSystem gameSystem ;

	protected final static String BASE_CONFIG = "base/config.cfg" ;

	public DesktopStarter( final SystemInterface _backendSystem, final FileSystem _fileSystem )
	{
		loadFileSystem( _fileSystem ) ;
		backendSystem = _backendSystem ;
		gameSystem = new GameSystem( _backendSystem ) ;
	}

	@Override
	public void init()
	{
		loadConfig() ;
		backendSystem.initSystem() ;
		setRenderSettings( backendSystem ) ;

		if( loadGame( gameSystem, getGameLoader() ) == false )
		{
			System.out.println( "Failed to load game.." ) ;
			return ;
		}

		gameSystem.runSystem() ;			// Begin running the game-loop
		backendSystem.shutdownSystem() ;	// Ensure all base systems are destroyed before exiting
	}

	@Override
	protected abstract GameLoader getGameLoader() ;

	@Override
	protected boolean loadGame( final GameSystem _system, final GameLoader _loader )
	{
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
		GlobalFileSystem.setFileSystem( _fileSystem ) ;
		GlobalFileSystem.scanBaseDirectory() ;
	}

	@Override
	protected void loadConfig()
	{
		final ConfigParser parser = new ConfigParser() ;
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( BASE_CONFIG ), new Settings() ) ) ;
	}

	@Override
	protected void setRenderSettings( final SystemInterface _system )
	{
		final int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", 640 ) ;
		final int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", 480 ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		_system.setDisplayDimensions( new Vector2( displayWidth, displayHeight ) ) ;
		_system.setRenderDimensions( new Vector2( renderWidth, renderHeight ) ) ;
		_system.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;
	}
}