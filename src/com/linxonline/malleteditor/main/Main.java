package com.linxonline.malleteditor.main ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.malleteditor.system.EditorState ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.malleteditor.system.GLEditorSystem ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.io.filesystem.DesktopFileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.ConfigParser ;
import com.linxonline.mallet.io.reader.ConfigReader ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.settings.Settings ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	private final static String BASE_CONFIG = "base/config.cfg" ;

	public static void main( String _args[] )
	{
		loadFileSystem() ;
		final GLEditorSystem system = new GLEditorSystem() ;		// OpenGL & OpenAL backend

		system.initSystem() ;
		loadConfig( system ) ;

		final GameSystem game = new GameSystem( system ) ;
		game.addGameState( new EditorState( "DEFAULT" ) ) ;

		game.setDefaultGameState( "DEFAULT" ) ;
		game.runSystem() ;							// Begin running the game-loop

		system.shutdownSystem() ;
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

		final int displayWidth = GlobalConfig.getInteger( "DISPLAYWIDTH", 640 ) ;
		final int displayHeight = GlobalConfig.getInteger( "DISPLAYHEIGHT", 480 ) ;

		final int renderWidth = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int renderHeight = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		_system.setDisplayDimensions( new Vector2( displayWidth, displayHeight ) ) ;
		_system.setRenderDimensions( new Vector2( renderWidth, renderHeight ) ) ;
		_system.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;
	}
}