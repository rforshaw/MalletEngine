package com.linxonline.mallet.main ;

import com.linxonline.mallet.game.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.renderer.DrawFactory ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	public static void main( String _args[] )
	{
		loadFileSystem() ;

		final DefaultSystem system = new DefaultSystem() ;
		system.initSystem() ;
		system.setDisplayDimensions( new Vector2( 640, 480 ) ) ;
		system.setRenderDimensions( new Vector2( 640, 480 ) ) ;
		system.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;

		final GameSystem game = new GameSystem() ;
		game.setSystem( system ) ;
		game.addGameState( new GameState( "DEFAULT" )
		{
			// Called when state is started.
			public void initGame()
			{
				// Add a texture to the render system
				eventSystem.addEvent( DrawFactory.createTexture( "base/textures/moomba.png", 		// Texture Location
																new Vector3( 0.0f, 0.0f, 0.0f ),	// Position
																new Vector2( -32, -32 ), 		// Offset
																new Vector2( 64, 64 ),			// Dimension, how large - scaled
																null,							// fill, texture repeat
																null,							// clip
																null,							// clip offset
																10 ) ) ;							// layer
			}
		} ) ;

		game.setDefaultGameState( "DEFAULT" ) ;
		game.runSystem() ;							// Begin running the game-loop
	}
	
	private static void loadFileSystem()
	{
		final ResourceManager resource = ResourceManager.getResourceManager() ;
		final DesktopFileSystem fileSystem = new DesktopFileSystem() ;
		fileSystem.scanBaseDirectory() ;
		
		resource.setFileSystem( fileSystem ) ;
	}
}