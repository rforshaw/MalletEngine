package com.linxonline.mallet.game.test ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;

import com.linxonline.mallet.animation.AnimationAssist ;
import com.linxonline.mallet.animation.Anim ;

import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.renderer.TextFactory ;
import com.linxonline.mallet.renderer.GeometryFactory ;
import com.linxonline.mallet.renderer.CameraFactory ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.audio.AudioFactory ;

import com.linxonline.mallet.util.sort.* ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.io.filesystem.* ;

import com.linxonline.mallet.game.* ;
import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.id.IDInterface ;

import com.linxonline.mallet.util.factory.creators.AnimMouseCreator ;
import com.linxonline.mallet.util.factory.creators.ImageCreator ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.io.formats.ogg.OGG ;
import com.linxonline.mallet.io.formats.ogg.Vorbis ;

import com.linxonline.mallet.physics.hulls.Box2D ;
import com.linxonline.mallet.physics.primitives.AABB ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;

import com.linxonline.mallet.util.tools.Diff ;
import com.linxonline.mallet.util.tools.SimpleDiff ;
import com.linxonline.mallet.io.reader.ByteReader ;

import com.linxonline.mallet.io.save.state.DataSet ;
import com.linxonline.mallet.io.save.state.DataSet.DataOut ;
import com.linxonline.mallet.io.save.state.DataSet.DataIn ;

/**
	Example on how to implement the Game Loader class.
	Initialise your Game States and add them to the 
	Game System passed in. Remember to tell the Game System
	what is the default-state!
*/
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

				for( int i = 0; i < 10; ++i )
				{
					for( int j = 0; j < 10; ++j )
					{
						createEntityExample( i, j ) ;
					}
				}

				createMouseAnimExample() ;
			}

			/**
				Add a texture and render directly to the renderer
			**/
			public void renderTextureExample()
			{
				eventSystem.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						{
							final MalletTexture texture = new MalletTexture( "base/textures/moomba.png" ) ;
							final int width = texture.getWidth() ;
							final int height = texture.getHeight() ;

							final Draw draw = DrawAssist.createDraw( new Vector3( 415.0f, 385.0f, 0.0f ),
																	 new Vector3( -( width / 2 ), -( height / 2 ), 0.0f ),
																	 new Vector3(),
																	 new Vector3( 1, 1, 1 ),
																	 10 ) ;
							DrawAssist.amendShape( draw, Shape.constructPlane( new Vector3( width, height, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ) ;
							DrawAssist.amendTexture( draw, texture ) ;
							DrawAssist.amendUI( draw, true ) ;
							DrawAssist.attachProgram( draw, "SIMPLE_TEXTURE" ) ;

							_delegate.addBasicDraw( draw ) ;
						}

						{
							final MalletColour colour = new MalletColour( 255, 255, 255 ) ;
							final Shape lines = new Shape( 7, 6 ) ;
							lines.addVertex( Shape.construct( 0, 10, 0, colour ) ) ;
							lines.addVertex( Shape.construct( 0, 0, 0, colour ) ) ;
							lines.addVertex( Shape.construct( 100, 0, 0, colour ) ) ;
							lines.addVertex( Shape.construct( 100, 5, 0, colour ) ) ;
							lines.addVertex( Shape.construct( 200, 0, 0, colour ) ) ;
							lines.addVertex( Shape.construct( 200, 10, 0, colour ) ) ;

							lines.addIndex( 0 ) ;
							lines.addIndex( 1 ) ;
							lines.addIndex( 2 ) ;
							lines.addIndex( 3 ) ;
							lines.addIndex( 2 ) ;
							lines.addIndex( 4 ) ;
							lines.addIndex( 5 ) ;

							final Draw draw = DrawAssist.createDraw( new Vector3( 0.0f, 50.0f, 0.0f ),
																	  new Vector3( -100.0f, 0.0f, 0.0f ),
																	  new Vector3(),
																	  new Vector3( 1, 1, 1 ),
																	  10 ) ;
							DrawAssist.amendShape( draw, lines ) ;
							DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;

							_delegate.addBasicDraw( draw ) ;
						}

						{
							final Shape triangle = new Shape( Shape.Style.FILL, 6, 6 ) ;
							triangle.addVertex( Shape.construct( 0, 0, 0,     MalletColour.red() ) ) ;
							triangle.addVertex( Shape.construct( 10, 50, 0,   MalletColour.blue() ) ) ;
							triangle.addVertex( Shape.construct( 50, 90, 0,   MalletColour.green() ) ) ;
							triangle.addVertex( Shape.construct( 100, 40, 0,  MalletColour.red() ) ) ;
							triangle.addVertex( Shape.construct( 110, -20, 0, MalletColour.blue() ) ) ;
							triangle.addVertex( Shape.construct( 50, -30, 0,  MalletColour.green() ) ) ;

							triangle.addIndex( 0 ) ;
							triangle.addIndex( 1 ) ;
							triangle.addIndex( 2 ) ;
							triangle.addIndex( 3 ) ;
							triangle.addIndex( 4 ) ;
							triangle.addIndex( 5 ) ;

							final Draw draw = DrawAssist.createDraw( new Vector3( -200.0f, 0.0f, 0.0f ),
																	 new Vector3(),
																	 new Vector3(),
																	 new Vector3( 1, 1, 1 ),
																	 10 ) ;
							DrawAssist.amendShape( draw, Shape.triangulate( triangle ) ) ;
							DrawAssist.attachProgram( draw, "SIMPLE_GEOMETRY" ) ;

							_delegate.addBasicDraw( draw ) ;
						}
					}
				} ) ) ;
			}

			/**
				Add an animation directly to the animation system
			**/
			public void renderAnimationExample()
			{
				final AnimComponent anim = new AnimComponent() ;
				final Anim moombaAnim = AnimationAssist.createAnimation( "base/anim/moomba.anim",
																		 new Vector3( 0.0f, 0.0f, 0.0f ),
																		 new Vector3( -32, -32, 0 ),
																		 new Vector3(),
																		 new Vector3( 1, 1, 1 ),
																		 10 ) ;

				DrawAssist.amendShape( AnimationAssist.getDraw( moombaAnim ), Shape.constructPlane( new Vector3( 64, 64, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ) ;

				anim.addAnimation( "DEFAULT", moombaAnim ) ;
				anim.setDefaultAnim( "DEFAULT" ) ;

				final Entity entity = new Entity( "Test Animation" ) ;
				entity.addComponent( anim ) ;
				addEntity( entity ) ;
			}

			/**
				Add text and render directly to the renderer
			**/
			public void renderTextExample()
			{
				eventSystem.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						final Draw draw = DrawAssist.createTextDraw( "Hello world!",
																	 new MalletFont( "Arial" ),
																	 new Vector3( 0.0f, -80.0f, 0.0f ),
																	 new Vector3( 0, 0, 0 ),
																	 new Vector3(),
																	 new Vector3( 1, 1, 1 ),
																	 200 ) ;
						_delegate.addTextDraw( draw ) ;
					}
				} ) ) ;
			}

			/**
				Play audio file directly to the audio system
			**/
			public void playAudioExample()
			{
				/*final SoundComponent sound = new SoundComponent() ;
				sound.addSound( "DEFAULT", AudioFactory.createAudio( "base/music/fairing-well.wav", sound ) ) ;

				sound.setDefaultSound( "DEFAULT" ) ;

				final Entity entity = new Entity( "Test Sound" ) ;
				entity.addComponent( sound ) ;
				addEntity( entity ) ;*/

				/*final OGG ogg = OGG.readOGG( "base/music/fairing-well.ogg" ) ;
				//System.out.println( ogg ) ;
				final Vorbis vorbis = new Vorbis() ;
				try
				{
					vorbis.decode( ogg ) ;
					System.out.println( vorbis ) ;
				}
				catch( Exception ex )
				{
					ex.printStackTrace() ;
				}*/
			}

			/**
				Create an Entity using the ImageCreator and add 
				it to the Game State
			**/
			public void createEntityExample( final int _i, final int _j )
			{
				final int x = 100 + ( _i * 50 ) ;
				final int y = 100 + ( _j * 50 ) ;
			
				final Settings image = new Settings() ;
				image.addString( "IMAGE", "base/textures/moomba.png" ) ;
				image.addString( "POS", Integer.toString( x ) + "," + Integer.toString( y ) ) ;
				image.addString( "DIM",  "64, 64" ) ;
				image.addString( "OFFSET", "-32, -32, 0" ) ;

				final ImageCreator creator = new ImageCreator() ;
				final Entity entity = creator.create( image ) ;

				entity.addComponent( CollisionComponent.generateBox2D( new Vector2(),
																	new Vector2( 64, 64 ),
																	new Vector2( x, y ),
																	new Vector2( -32, -32 ) ) ) ;

				addEntity( entity ) ;
			}

			/**
				Create an Entity that follows the mouse
			**/
			public void createMouseAnimExample()
			{
				final Settings mouse = new Settings() ;
				mouse.addString( "ANIM", "base/anim/moomba.anim" ) ;
				mouse.addObject( "DIM", new Vector2( 32, 32 ) ) ;
				mouse.addObject( "OFFSET", new Vector3( -16, -16, 0 ) ) ;

				final AnimMouseCreator creator = new AnimMouseCreator() ;
				final Entity entity = creator.create( mouse ) ;

				final CollisionComponent collision = CollisionComponent.generateBox2D( new Vector2(),
																					   new Vector2( 32, 32 ),
																					   new Vector2( 0, 0 ),
																					   new Vector2( -16, -16 ) ) ;
				collision.hull.setPhysical( false ) ;
				entity.addComponent( collision ) ;

				addEntity( entity ) ;
			}
		} ) ;

		_system.setDefaultGameState( "DEFAULT" ) ;		// Define what Game State should be run first
	}
}