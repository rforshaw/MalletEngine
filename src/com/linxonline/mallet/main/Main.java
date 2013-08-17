package com.linxonline.mallet.main ;

import java.util.ArrayList ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.game.GameSystem ;
import com.linxonline.mallet.game.GameState ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.GLDefaultSystem ;
import com.linxonline.mallet.system.DefaultSystem ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.io.filesystem.DesktopFileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.ConfigParser ;
import com.linxonline.mallet.io.reader.ConfigReader ;

import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.audio.AudioFactory ;

import com.linxonline.mallet.util.sort.* ;

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
	private final static String BASE_CONFIG = "base/config.cfg" ;

	public static void main( String _args[] )
	{
		loadFileSystem() ;
		//final DefaultSystem system = new DefaultSystem() ;			// Graphics2D & OpenAL backend
		final GLDefaultSystem system = new GLDefaultSystem() ;		// OpenGL & OpenAL backend

		system.initSystem() ;
		loadConfig( system ) ;

		final GameSystem game = new GameSystem( system ) ;
		game.addGameState( new GameState( "DEFAULT" )
		{
			// Called when state is started.
			public void initGame()
			{
				renderTextureExample() ;
				//renderAnimationExample() ;
				renderTextExample() ;
				//playAudioExample() ;
				//createEntityExample() ;
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
				Add text and render directly to the renderer
			**/
			public void renderTextExample()
			{
				eventSystem.addEvent( DrawFactory.createText(  "Hello World!", 						// Text
																new Vector3( 0.0f, -80.0f, 0.0f ),	// Position
																new Vector2( 0, 0 ), 				// Offset
																new MalletFont( "Arial", 20 ),		// Mallet Font
																null,								// Mallet Colour
																null,								// clip
																null,								// clip offset
																10,									// layer
																2 ) ) ;								// Text alignment, Centre
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