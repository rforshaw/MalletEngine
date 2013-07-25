package com.linxonline.mallet.main ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameState ;

import com.linxonline.mallet.system.GLDefaultSystem ;
import com.linxonline.mallet.system.DefaultSystem ;

import com.linxonline.mallet.io.filesystem.DesktopFileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.audio.AudioFactory ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.id.IDInterface ;

import com.linxonline.mallet.util.factory.creators.AnimMouseCreator ;
import com.linxonline.mallet.util.factory.creators.ImageCreator ;
import com.linxonline.mallet.util.settings.Settings ;

/*===========================================*/
// Main
// Test Main
/*===========================================*/
public class Main
{
	public static void main( String _args[] )
	{
		loadFileSystem() ;

		//final DefaultSystem system = new DefaultSystem() ;			// Graphics2D & OpenAL backend
		final GLDefaultSystem system = new GLDefaultSystem() ;		// OpenGL & OpenAL backend

		system.initSystem() ;
		system.setDisplayDimensions( new Vector2( 320, 240 ) ) ;
		system.setRenderDimensions( new Vector2( 640, 480 ) ) ;
		system.setCameraPosition( new Vector3( 0.0f, 0.0f, 0.0f ) ) ;

		final GameSystem game = new GameSystem( system ) ;
		game.addGameState( new GameState( "DEFAULT" )
		{
			// Called when state is started.
			public void initGame()
			{
				//renderTextureExample() ;
				//renderAnimationExample() ;
				//playAudioExample() ;
				createEntityExample() ;
				createMouseAnimExample() ;
			}

			/**
				Add a texture and render directly to the renderer
			**/
			public void renderTextureExample()
			{
				eventSystem.addEvent( DrawFactory.createTexture( "base/textures/moomba.png", 			// Texture Location
																	new Vector3( 0.0f, 0.0f, 0.0f ),	// Position
																	new Vector2( -32, -32 ), 			// Offset
																	new Vector2( 64, 64 ),				// Dimension, how large - scaled
																	null,								// fill, texture repeat
																	null,								// clip
																	null,								// clip offset
																	10 ) ) ;							// layer
			}

			/**
				Add an animation directly to the animation system
			**/
			public void renderAnimationExample()
			{
				eventSystem.addEvent( AnimationFactory.createAnimation( "base/anim/moomba.anim", 			// Animation Location
																		 new Vector3( 0.0f, 0.0f, 0.0f ),	// Position
																		 new Vector2( -32, -32 ), 			// Offset
																		 new Vector2( 64, 64 ),				// Dimension, how large - scaled
																		 null,								// fill, texture repeat
																		 null,								// clip
																		 null,								// clip offset
																		 10,								// layer
																		 new SourceCallback()
				{
					public void recieveID( final int _id ) { System.out.println( "Recieved ID: " + _id ) ; }

					public void callbackRemoved() { System.out.println( "Callback Removed" ) ; }

					public void start() { System.out.println( "Source began playing" ) ; }
					public void pause() { System.out.println( "Source has been paused" ) ; }
					public void stop() { System.out.println( "Source has been stopped" ) ; }

					public void update( final float _dt ) { System.out.println( _dt ) ; }
					public void finished() { System.out.println( "Source has finished" ) ; }
				} ) ) ;
			}

			/**
				Play audio file directly to the audio system
			**/
			public void playAudioExample()
			{
				eventSystem.addEvent( AudioFactory.createAudio( "base/music/fairing-well.wav", new SourceCallback()
				{
					public void recieveID( final int _id ) { System.out.println( "Recieved ID: " + _id ) ; }
					public void callbackRemoved() { System.out.println( "Callback Removed" ) ; }

					public void start() { System.out.println( "Source began playing" ) ; }
					public void pause() { System.out.println( "Source has been paused" ) ; }
					public void stop() { System.out.println( "Source has been stopped" ) ; }

					public void update( final float _dt ) { System.out.println( _dt ) ; }
					public void finished() { System.out.println( "Source has finished" ) ; }
				} ) ) ;
			}

			/**
				Create an Entity using the ImageCreator and add 
				it to the Game State
			**/
			public void createEntityExample()
			{
				final Settings image = new Settings() ;
				image.addString( "IMAGE", "base/textures/moomba.png" ) ;
				image.addString( "POS", "0, 0" ) ;
				image.addString( "DIM", "128, 128" ) ;
				image.addString( "OFFSET", "-64, -64" ) ;

				final ImageCreator creator = new ImageCreator() ;
				addEntity( creator.create( image ) ) ;
			}

			/**
				Create an Entity that follows the mouse
			**/
			public void createMouseAnimExample()
			{
				final Settings mouse = new Settings() ;
				mouse.addString( "ANIM", "base/anim/moomba.anim" ) ;
				mouse.addObject( "DIM", new Vector2( 32, 32 ) ) ;
				mouse.addObject( "OFFSET", new Vector2( -16, -16 ) ) ;

				final AnimMouseCreator creator = new AnimMouseCreator() ;
				addEntity( creator.create( mouse ) ) ;
			}
		} ) ;

		game.setDefaultGameState( "DEFAULT" ) ;
		game.runSystem() ;							// Begin running the game-loop

		system.shutdownSystem() ;
	}

	private static void loadFileSystem()
	{
		final DesktopFileSystem fileSystem = new DesktopFileSystem() ;

		GlobalFileSystem.setFileSystem( fileSystem ) ;
		GlobalFileSystem.scanBaseDirectory() ;
	}
}