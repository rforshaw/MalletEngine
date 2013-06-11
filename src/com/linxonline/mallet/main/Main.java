package com.linxonline.mallet.main ;

import com.linxonline.mallet.util.tools.* ;
import com.linxonline.mallet.io.serialisation.* ;
import  com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.game.* ;
import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.maths.* ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	public static void main( String _args[] )
	{
		loadFileSystem() ;

		DefaultSystem system = new DefaultSystem() ;
		//GLDefaultSystem system = new GLDefaultSystem() ;
		system.initSystem() ;
		system.setDisplayDimensions( new Vector2( 800, 600 ) ) ;
		system.setRenderDimensions( new Vector2( 800, 600 ) ) ;
		system.setCameraPosition( new Vector3( 400.0f, 300.0f, 0.0f ) ) ;

		GameSystem game = new GameSystem() ;
		GameState state = new GameState( "DEFAULT" ) ;

		game.setSystem( system ) ;
		game.addGameState( state ) ;
		game.setDefaultGameState( "DEFAULT" ) ;

		game.runSystem() ;
	}
	
	private static void loadFileSystem()
	{
		final ResourceManager resource = ResourceManager.getResourceManager() ;
		final DesktopFileSystem fileSystem = new DesktopFileSystem() ;
		fileSystem.scanBaseDirectory() ;
		
		resource.setFileSystem( fileSystem ) ;
	}
}