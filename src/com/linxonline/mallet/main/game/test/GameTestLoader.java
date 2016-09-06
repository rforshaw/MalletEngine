package com.linxonline.mallet.main.game.test ;

import java.util.ArrayList ;
import java.util.HashMap ;

import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.GameState ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.animation.AnimationAssist ;
import com.linxonline.mallet.animation.Anim ;

import com.linxonline.mallet.audio.AudioAssist ;
import com.linxonline.mallet.audio.AudioDelegateCallback ;
import com.linxonline.mallet.audio.AudioDelegate ;
import com.linxonline.mallet.audio.StreamType ;

import com.linxonline.mallet.util.sort.* ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.io.filesystem.* ;

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

import com.linxonline.mallet.io.formats.sgeom.SGeom ;
import com.linxonline.mallet.util.tools.ConvertBytes ;

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
				createSpinningCubeExample() ;
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
							final int width = GlobalConfig.getInteger( "RENDERWIDTH", 800 ) ;
							final int height = GlobalConfig.getInteger( "RENDERHEIGHT", 600 ) ;

							final Camera cam = CameraAssist.createCamera( "OFFSIDE", new Vector3(), new Vector3(), new Vector3( 1, 1, 1 ) ) ;
							CameraAssist.addCamera( cam, null ) ;

							CameraAssist.amendOrthographic( cam, 0.0f, height, 0.0f, width, -1000.0f, 1000.0f ) ;
							CameraAssist.amendScreenResolution( cam, width / 4, height / 4 ) ;
							//CameraAssist.amendScreenOffset( cam, 200, 200 ) ;
						}
					
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
							DrawAssist.attachProgram( draw, ProgramAssist.createProgram( "SIMPLE_TEXTURE" ) ) ;

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
							DrawAssist.attachProgram( draw, ProgramAssist.createProgram( "SIMPLE_GEOMETRY" ) ) ;

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
							DrawAssist.attachProgram( draw, ProgramAssist.createProgram( "SIMPLE_GEOMETRY" ) ) ;

							_delegate.addBasicDraw( draw ) ;
						}

						/*{
							final Shape shape = SGeom.load( "base/ui/test.sgeom" ) ;
							if( shape != null )
							{
								final Draw draw = DrawAssist.createDraw( new Vector3( -200.0f, 0.0f, 0.0f ),
																		new Vector3(),
																		new Vector3(),
																		new Vector3( 1, 1, 1 ),
																		10 ) ;
								DrawAssist.amendShape( draw, shape ) ;
								DrawAssist.attachProgram( draw, ProgramAssist.createProgram( "SIMPLE_GEOMETRY" ) ) ;

								_delegate.addBasicDraw( draw ) ;
							}
						}*/
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
						DrawAssist.amendColour( draw, new MalletColour( 144, 195, 212 ) ) ;
						_delegate.addTextDraw( draw ) ;
					}
				} ) ) ;
			}

			/**
				Play audio file directly to the audio system
			**/
			public void playAudioExample()
			{
				eventSystem.addEvent( AudioAssist.constructAudioDelegate( new AudioDelegateCallback()
				{
					public void callback( AudioDelegate _delegate )
					{
						_delegate.addAudio( AudioAssist.createAudio( "base/music/fairing-well.wav", StreamType.STATIC ) ) ;
					}
				} ) ) ;

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

			public void createSpinningCubeExample()
			{
				final Entity entity = new Entity( "SPINNING_CUBE" ) ;

				final MalletTexture texture = new MalletTexture( "base/textures/moomba.png" ) ;
				final Draw draw = DrawAssist.createDraw( new Vector3( 0.0f, -200.0f, 0.0f ),
															new Vector3( -50.0f, -50.0f, -50.0f ),
															new Vector3(),
															new Vector3( 1, 1, 1 ),
															10 ) ;
				DrawAssist.amendShape( draw, Shape.constructCube( 100.0f, new Vector2(), new Vector2( 1, 1 ) ) ) ;
				DrawAssist.amendTexture( draw, texture ) ;
				DrawAssist.amendInterpolation( draw, Interpolation.LINEAR ) ;
				DrawAssist.amendUpdateType( draw, UpdateType.ON_DEMAND ) ;
				DrawAssist.attachProgram( draw, ProgramAssist.createProgram( "SIMPLE_TEXTURE" ) ) ;

				final RenderComponent render = new RenderComponent() ;
				render.addBasicDraw( draw ) ;
				
				entity.addComponent( render ) ;
				entity.addComponent( new Component( "SPIN", "CUBE" )
				{
					private final Vector3 rotate = new Vector3() ;

					@Override
					public void update( final float _dt )
					{
						super.update( _dt ) ;
						rotate.x += 2.5f * _dt ;
						rotate.y += 2.8f * _dt ;
						rotate.z += 2.1f * _dt ;

						DrawAssist.amendRotate( draw, rotate.x, rotate.y, rotate.z ) ;
						DrawAssist.forceUpdate( draw ) ;
					}
				} ) ;
				
				addEntity( entity ) ;
			}
		} ) ;

		_system.setDefaultGameState( "DEFAULT" ) ;		// Define what Game State should be run first
	}
}
