package com.linxonline.mallet.main ;

import com.linxonline.mallet.game.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.system.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.tools.ogg.OGG ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	public static void main( String _args[] )
	{
		loadFileSystem() ;

		//final DefaultSystem system = new DefaultSystem() ;			// Graphics2D backend
		final GLDefaultSystem system = new GLDefaultSystem() ;			// OpenGL backend

		system.initSystem() ;
		system.setDisplayDimensions( new Vector2( 320, 240 ) ) ;
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
																new Vector2( -32, -32 ), 			// Offset
																new Vector2( 64, 64 ),				// Dimension, how large - scaled
																null,			// fill, texture repeat
																null,								// clip
																null,								// clip offset
																10 ) ) ;							// layer

				/*eventSystem.addEvent( AudioFactory.createAudio( "base/audio/0.wav", new SourceCallback()
				{
					public void recieveID( final int _id ) { System.out.println( "Recieved ID: " + _id ) ; }
					public void callbackRemoved() { System.out.println( "Callback Removed" ) ; }

					public void start() { System.out.println( "Source began playing" ) ; }
					public void pause() { System.out.println( "Source has been paused" ) ; }
					public void stop() { System.out.println( "Source has been stopped" ) ; }

					public void update( final float _dt ) { System.out.println( _dt ) ; }
					public void finished() { System.out.println( "Source has finished" ) ; }
				} ) ) ;*/
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
		
		OGG ogg = new OGG( "base/audio/0.ogg" ) ;
	}
}