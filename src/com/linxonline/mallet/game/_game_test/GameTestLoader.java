package com.linxonline.mallet.game ;

import com.linxonline.mallet.animation.AnimationFactory ;
import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.Line ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.audio.AudioFactory ;

import com.linxonline.mallet.util.sort.* ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.id.IDInterface ;

import com.linxonline.mallet.util.factory.creators.AnimMouseCreator ;
import com.linxonline.mallet.util.factory.creators.ImageCreator ;
import com.linxonline.mallet.util.settings.Settings ;

public final class GameTestLoader extends GameLoader
{
	public GameTestLoader() {}

	@Override
	public void loadGame( final GameSystem _system )
	{
		_system.addGameState( new GameState( "DEFAULT" )
		{
			public void initGame()			// Called when state is started
			{
				renderTextureExample() ;
				renderAnimationExample() ;
				renderTextExample() ;
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
																	new Vector3( -100.0f, 0.0f, 0.0f ),	// Position
																	new Vector2( -32, -32 ), 			// Offset
																	new Vector2( 64, 64 ),				// Dimension, how large - scaled
																	null,								// fill, texture repeat
																	null,								// clip
																	null,								// clip offset
																	10 ) ) ;							// layer

				final Shape lines = new Shape( 7, 6 ) ;
				lines.addPoint( new Vector2( 0, 10 ) ) ;
				lines.addPoint( new Vector2( 0, 0 ) ) ;
				lines.addPoint( new Vector2( 100, 0 ) ) ;
				lines.addPoint( new Vector2( 100, 5 ) ) ;
				lines.addPoint( new Vector2( 200, 0 ) ) ;
				lines.addPoint( new Vector2( 200, 10 ) ) ;

				lines.addIndex( 0 ) ;
				lines.addIndex( 1 ) ;
				lines.addIndex( 2 ) ;
				lines.addIndex( 3 ) ;
				lines.addIndex( 2 ) ;
				lines.addIndex( 4 ) ;
				lines.addIndex( 5 ) ;

				eventSystem.addEvent( DrawFactory.createShape( "DRAWLINES",
																lines,
																new Vector3( -100.0f, 50.0f, 0.0f ),
																null,
																null,
																null,
																10 ) ) ;
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

					public void update( final float _dt ) { /*System.out.println( _dt ) ;*/ }
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
				image.addString( "POS", "100, 0" ) ;
				image.addString( "DIM", "64, 64" ) ;
				image.addString( "OFFSET", "-32, -32" ) ;

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

		_system.setDefaultGameState( "DEFAULT" ) ;		// Define what Game State should be run first
	}
}