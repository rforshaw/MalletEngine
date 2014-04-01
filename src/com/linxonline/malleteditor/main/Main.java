package com.linxonline.malleteditor.main ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.malleteditor.system.GLEditorSystem ;

import com.linxonline.mallet.io.filesystem.DesktopFileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.ConfigParser ;
import com.linxonline.mallet.io.reader.ConfigReader ;

import com.linxonline.mallet.util.settings.Settings ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	private final static String BASE_CONFIG = "base/config.cfg" ;
	private final static GLEditorSystem backendSystem = new GLEditorSystem() ;		// OpenGL & OpenAL backend
	private final static GameSystem gameSystem = new GameSystem( backendSystem ) ;

	public static void main( String _args[] )
	{
		loadFileSystem() ;

		backendSystem.initSystem() ;
		loadConfig( backendSystem ) ;
		setRenderSettings( backendSystem ) ;

		if( loadGame( gameSystem, new EditorLoader() ) == false )
		{
			System.out.println( "Failed to load game.." ) ;
			return ;
		}

		gameSystem.runSystem() ;							// Begin running the game-loop
		backendSystem.shutdownSystem() ;
	}

	private static boolean loadGame( final GameSystem _system, final GameLoader _loader )
	{
		if( _system != null && _loader != null )
		{
			_loader.loadGame( _system ) ;
			return true ;
		}

		return false ;
	}

	private static void loadFileSystem()
	{
		GlobalFileSystem.setFileSystem( new DesktopFileSystem() ) ;
		GlobalFileSystem.scanBaseDirectory() ;
	}

	private static void loadConfig( final SystemInterface _system )
	{
		final ConfigParser parser = new ConfigParser() ;
		GlobalConfig.setConfig( parser.parseSettings( ConfigReader.getConfig( BASE_CONFIG ), new Settings() ) ) ;
	}
	
	protected static void setRenderSettings( final SystemInterface _system )
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