package com.linxonline.mallet.core.test ;

import java.util.List ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.animation.* ;
import com.linxonline.mallet.io.filesystem.* ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.io.formats.ogg.OGG ;
import com.linxonline.mallet.io.formats.ogg.Vorbis ;

import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.hulls.Box2D ;
import com.linxonline.mallet.physics.primitives.AABB ;

import com.linxonline.mallet.util.schema.* ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;

/**
	Example on how to implement the Game Loader class.
	Initialise your Game States and add them to the 
	Game System passed in. Remember to tell the Game System
	what is the default-state!
*/
public final class GameTestLoader implements IGameLoader
{
	public GameTestLoader() {}

	@Override
	public void loadGame( final IGameSystem _system )
	{
		_system.addGameState( new GameState( "DEFAULT" )
		{
			public void initGame()			// Called when state is started
			{
				//createMeta() ;

				createUI() ;
				renderTextureExample() ;
				renderAnimationExample() ;
				renderTextExample() ;
				//playAudioExample() ;

				createEntities( 10, 10 ) ;

				createMouseAnimExample() ;
				createSpinningCubeExample() ;

				createEventMessageTest() ;

				getInternalController().passEvent( new Event<Boolean>( "SHOW_GAME_STATE_FPS", true ) ) ;
			}

			public void createMeta()
			{
				final SNode struct1 = SStruct.create( SStruct.var( "test1", SPrim.bool() ),
													  SStruct.var( "test2", SPrim.flt() ),
													  SStruct.var( "test3", SPrim.integer() ) ) ;

				final SNode struct2 = SStruct.create( SStruct.var( "test1", SPrim.bool() ),
													  SStruct.var( "test2", SPrim.flt() ),
													  SStruct.var( "test3", SPrim.integer() ) ) ;

				System.out.println( "Structures 1 and 2: " + struct1.equals( struct2 ) ) ;

				final SNode struct3 = SStruct.create( SStruct.var( "test1", SPrim.flt() ),
													  SStruct.var( "test2", SPrim.flt() ),
													  SStruct.var( "test3", SPrim.integer() ) ) ;

				System.out.println( "Structures 1 and 3: " + struct1.equals( struct3 ) ) ;
			}

			public void createUI()
			{
				final JUI jUI = JUI.create( "base/ui/test.jui" ) ;
				final UIButton button1 = jUI.get( "TestButton1", UIButton.class ) ;
				final UIButton button2 = jUI.get( "TestButton2", UIButton.class ) ;
				final UICheckbox checkbox = jUI.get( "TestCheckbox", UICheckbox.class ) ;

				UIElement.connect( button1, button1.released(), ( final UIButton _box ) ->
				{
					button2.setVisible( !button2.isVisible() ) ;
				} ) ;

				UIElement.connect( button2, button2.released(), ( final UIButton _box ) ->
				{
					button1.setVisible( !button1.isVisible() ) ;
				} ) ;

				UIElement.connect( checkbox, checkbox.checkChanged(), ( final UICheckbox _box ) ->
				{
					button1.setVisible( !button1.isVisible() ) ;
					button2.setVisible( !button2.isVisible() ) ;
				} ) ;

				final Entity entity = new Entity( 1, Entity.AllowEvents.NO ) ;
				final UIComponent component = new UIComponent( entity ) ;
				component.addElement( jUI.getParent() ) ;

				addEntity( entity ) ;
			}

			/**
				Add a texture and render directly to the renderer
			**/
			public void renderTextureExample()
			{
				eventSystem.addEvent( DrawAssist.constructDrawDelegate( ( final DrawDelegate _delegate ) ->
				{
					{
						final World base = WorldAssist.getDefaultWorld() ;
						final IntVector2 dim = WorldAssist.getRenderDimensions( base ) ;

						final Camera cam = CameraAssist.createCamera( "OFFSIDE", new Vector3(), new Vector3(), new Vector3( 1, 1, 1 ) ) ;
						CameraAssist.addCamera( cam, null ) ;

						CameraAssist.amendOrthographic( cam, 0.0f, dim.y, 0.0f, dim.x, -1000.0f, 1000.0f ) ;
						CameraAssist.amendScreenResolution( cam, dim.x / 4, dim.y / 4 ) ;
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

						final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
						ProgramAssist.mapUniform( program, "inTex0", texture ) ;
						DrawAssist.attachProgram( draw, program ) ;

						DrawAssist.amendShape( draw, Shape.constructPlane( new Vector3( width, height, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ) ;
						DrawAssist.amendUI( draw, true ) ;

						_delegate.addBasicDraw( draw ) ;
					}

					{
						final MalletColour colour = new MalletColour( 255, 255, 255 ) ;
						final Shape lines = new Shape( Shape.Style.LINE_STRIP, 7, 6 ) ;
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
						DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;

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

						final Draw draw = DrawAssist.createDraw( new Vector3( 0.0f, 0.0f, 0.0f ),
																	new Vector3(),
																	new Vector3(),
																	new Vector3( 1, 1, 1 ),
																	0 ) ;
						DrawAssist.amendShape( draw, Shape.triangulate( triangle ) ) ;
						DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;

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
							DrawAssist.attachProgram( draw, ProgramAssist.create( "SIMPLE_GEOMETRY" ) ) ;

							_delegate.addBasicDraw( draw ) ;
						}
					}*/
				} ) ) ;
			}

			/**
				Add an animation directly to the animation system
			**/
			public void renderAnimationExample()
			{
				final Entity entity = new Entity( 2, Entity.AllowEvents.NO ) ;

				final AnimComponent anim = new AnimComponent( entity ) ;
				final Anim moombaAnim = AnimationAssist.createAnimation( "base/anim/moomba.anim",
																		 new Vector3( 0.0f, 0.0f, 0.0f ),
																		 new Vector3( -32, -32, 0 ),
																		 new Vector3(),
																		 new Vector3( 1, 1, 1 ),
																		 10 ) ;

				DrawAssist.amendShape( AnimationAssist.getDraw( moombaAnim ), Shape.constructPlane( new Vector3( 64, 64, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ) ;

				anim.addAnimation( "DEFAULT", moombaAnim ) ;
				anim.setDefaultAnim( "DEFAULT" ) ;

				new Component( entity )
				{
					private final static float DURATION = 5.0f ;
					private float elapsed = 0.0f ;

					public void update( final float _dt )
					{
						elapsed += _dt ;
						if( elapsed >= DURATION )
						{
							elapsed = 0.0f ;
							//anim.playAnimation( "DEFAULT" ) ;
						}
					}
				} ;
				
				addEntity( entity ) ;
			}

			/**
				Add text and render directly to the renderer
			**/
			public void renderTextExample()
			{
				eventSystem.addEvent( DrawAssist.constructDrawDelegate( ( final DrawDelegate _delegate ) ->
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
				} ) ) ;
			}

			/**
				Play audio file directly to the audio system
			**/
			public void playAudioExample()
			{
				eventSystem.addEvent( AudioAssist.constructAudioDelegate( ( final AudioDelegate _delegate ) ->
				{
					final Audio audio = AudioAssist.createAudio( "base/music/test.wav", StreamType.STATIC, Category.Channel.MUSIC ) ;
					_delegate.addAudio( AudioAssist.amendCallback( AudioAssist.play( audio ), new SourceCallback()
					{
						public void callbackRemoved() {}

						public void start()
						{
							//System.out.println( "Audio start." ) ;
						}

						public void pause()
						{
							//System.out.println( "Audio pause." ) ;
						}

						public void stop()
						{
							//System.out.println( "Audio stop." ) ;
						}

						public void tick( final float _dt )
						{
							//System.out.println( "Audio Tick " + _dt ) ;
						}

						public void finished()
						{
							//System.out.println( "Audio Finished." ) ;
							// Enable to loop test audio.
							AudioAssist.play( audio ) ;
						}
					} ) ) ;
				} ) ) ;

				/*final OGG ogg = OGG.readOGG( "base/music/test.ogg" ) ;
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

			public void createEntities( final int _row, final int _column )
			{
				final int amount = _row * _column ;
				final Vector3 dim = new Vector3( 64, 64, 0 ) ;
				final Shape plane = Shape.constructPlane( dim, new Vector2( 0, 0 ), new Vector2( 1, 1 ) ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.mapUniform( program, "inTex0", new MalletTexture( "base/textures/moomba.png" ) ) ;

				final Entity entity = new Entity( 1 + amount, Entity.AllowEvents.NO ) ;

				final List<Hull> hulls = MalletList.<Hull>newList( amount ) ;
				final List<Draw> draws = MalletList.<Draw>newList( amount ) ;
				final RenderComponent render = new RenderComponent( entity, Entity.AllowEvents.NO )
				{
					@Override
					public void update( final float _dt )
					{
						super.update( _dt ) ;
						final int size = draws.size() ;
						for( int i = 0; i < size; ++i )
						{
							final Hull hull = hulls.get( i ) ;
							final Draw draw = draws.get( i ) ;
							DrawAssist.forceUpdate( draw ) ;

							final Vector2 pos = hull.getPosition() ;
							DrawAssist.amendPosition( draw, pos.x, pos.y, 0.0f ) ;
						}
					}
				} ;

				for( int i = 0; i < _row; ++i )
				{
					for( int j = 0; j < _column; ++j )
					{
						final int x = 50 + ( i * 50 ) ;
						final int y = 50 + ( j * 50 ) ;

						final CollisionComponent coll = CollisionComponent.generateBox2D( entity,
																						Entity.AllowEvents.NO,
																						new Vector2(),
																						new Vector2( 64, 64 ),
																						new Vector2( x, y ),
																						new Vector2( -32, -32 ) ) ;
						final Hull hull = coll.hull ;
						final Vector2 position = new Vector2( x, y ) ;

						final Vector3 offset = new Vector3( -32, -32, 0 ) ;
						final Draw draw = DrawAssist.createDraw( new Vector3( position ),
																offset,
																new Vector3(),
																new Vector3( 1, 1, 1 ),
																10 ) ;

						DrawAssist.amendShape( draw, plane ) ;
						DrawAssist.attachProgram( draw, program ) ;
						DrawAssist.amendInterpolation( draw, Interpolation.LINEAR ) ;

						hulls.add( hull ) ;
						draws.add( draw ) ;
					}
				}

				render.addBasicDraw( draws, null ) ;

				addEntity( entity ) ;
			}

			/**
				Create an Entity that follows the mouse
			**/
			public void createMouseAnimExample()
			{
				final World base = WorldAssist.getDefaultWorld() ;
				final IntVector2 dim = WorldAssist.getRenderDimensions( base ) ;

				final Entity entity = new Entity( 4, Entity.AllowEvents.NO ) ;
				final AnimComponent anim   = new AnimComponent( entity ) ;
				final EventComponent event = new EventComponent( entity ) ;

				final Anim animation = AnimationAssist.createAnimation( "base/anim/moomba.anim",
																		new Vector3( dim.x / 2, dim.y / 2, 0 ),
																		new Vector3( -16, -16, 0 ),
																		new Vector3(),
																		new Vector3( 1, 1, 1 ),
																		100 ) ;

				final Shape plane = Shape.constructPlane( new Vector3( 32, 32, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;
				DrawAssist.amendShape( AnimationAssist.getDraw( animation ), plane ) ;
				DrawAssist.amendInterpolation( AnimationAssist.getDraw( animation ), Interpolation.LINEAR ) ;
				DrawAssist.amendUpdateType( AnimationAssist.getDraw( animation ), UpdateType.ON_DEMAND ) ;

				anim.addAnimation( "DEFAULT", animation ) ;
				anim.setDefaultAnim( "DEFAULT" ) ;

				final CollisionComponent collision = CollisionComponent.generateBox2D( entity,
																						new Vector2(),
																						new Vector2( 32, 32 ),
																						new Vector2( 0, 0 ),
																						new Vector2( -16, -16 ) ) ;
				collision.hull.setPhysical( false ) ;

				final MouseComponent mouse = new MouseComponent( entity )
				{
					private final Draw draw = AnimationAssist.getDraw( animation ) ;

					@Override
					public void applyMousePosition( final Vector2 _mouse )
					{
						DrawAssist.amendPosition( draw, _mouse.x, _mouse.y, 0.0f ) ;
					}
				} ;
				
				addEntity( entity ) ;
			}

			public void createSpinningCubeExample()
			{
				final Entity entity = new Entity( 2, Entity.AllowEvents.NO ) ;

				final MalletTexture texture = new MalletTexture( "base/textures/moomba.png" ) ;
				final Draw draw = DrawAssist.createDraw( new Vector3( 0.0f, -200.0f, 0.0f ),
														 new Vector3( -50.0f, -50.0f, -50.0f ),
														 new Vector3(),
														 new Vector3( 1, 1, 1 ),
														 10 ) ;
				DrawAssist.amendShape( draw, Shape.constructCube( 100.0f, new Vector2(), new Vector2( 1, 1 ) ) ) ;
				DrawAssist.amendInterpolation( draw, Interpolation.LINEAR ) ;
				DrawAssist.amendUpdateType( draw, UpdateType.ON_DEMAND ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.mapUniform( program, "inTex0", texture ) ;
				DrawAssist.attachProgram( draw, program ) ;

				final RenderComponent render = new RenderComponent( entity ) ;
				render.addBasicDraw( draw ) ;

				new Component( entity )
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
				} ;

				addEntity( entity ) ;
			}

			public void createEventMessageTest()
			{
				{
					final Entity receive = new Entity( 1, Entity.AllowEvents.NO ) ;
					final EventComponent event = new EventComponent( receive )
					{
						@Override
						public void initStateEventProcessors( final EventController _controller )
						{
							super.initStateEventProcessors( _controller ) ;
							_controller.addProcessor( "TEST_EVENT", ( final String _message ) ->
							{
								System.out.println( "Received: " + _message ) ;
							} ) ;
						}
					} ;

					addEntity( receive ) ;
				}

				{
					final Entity send = new Entity( 1, Entity.AllowEvents.NO ) ;
					final EventComponent event = new EventComponent( send ) ;
					event.passStateEvent( new Event<String>( "TEST_EVENT", "Hello World!" ) ) ;

					addEntity( send ) ;
				}
			}
		} ) ;

		_system.setDefaultGameState( "DEFAULT" ) ;		// Define what Game State should be run first
	}

	@Override
	public GameSettings getGameSettings()
	{
		return new GameSettings( "Mallet Engine - Test" ) ;
	}
}
