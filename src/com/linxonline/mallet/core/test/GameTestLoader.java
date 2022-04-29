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

import com.linxonline.mallet.renderer.IShape.Swivel ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.ogg.OGG ;
import com.linxonline.mallet.io.formats.ogg.Vorbis ;
import com.linxonline.mallet.io.formats.gltf.GLTF ;
import com.linxonline.mallet.io.serialisation.Serialise ;

import com.linxonline.mallet.physics.Debug ;
import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.hulls.Box2D ;
import com.linxonline.mallet.physics.primitives.AABB ;

import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.io.net.UDPServer ;
import com.linxonline.mallet.io.net.UDPClient ;
import com.linxonline.mallet.io.net.IOutStream ;
import com.linxonline.mallet.io.net.InStream ;
import com.linxonline.mallet.io.net.Address ;

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
				/*boolean run = true ;
				final InStream stream = new InStream( 100 ) ;
				final UDPServer server = new UDPServer() ;
				server.init( new Address( "localhost", 4455 ), 30 ) ;

				final UDPClient client = new UDPClient() ;
				client.init( new Address( "localhost", 4455 ), 30 ) ;
				client.send( new IOutStream()
				{
					private String test = "Hello World!" ;

					@Override
					public int getLength()
					{
						return test.getBytes().length ;
					}

					@Override
					public void serialise( Serialise.Out _out )
					{
						_out.writeString( test ) ;
					}
				} ) ;

				while( run == true )
				{
					server.receive( stream ) ;
					final int size = stream.getDataLength() ;
					final Serialise.ByteIn serialise = new Serialise.ByteIn( stream.getBuffer(), 0, size ) ;

					final String response = serialise.readString() ;
					System.out.println( size ) ;
					System.out.println( response ) ;

					if( response.equals( "Hello World!" ) )
					{
						run = false ;
					}
				}
				server.close() ;
				client.close() ;*/

				createUI() ;
				renderTextureExample() ;
				renderAnimationExample() ;
				renderTextExample() ;
				playAudioExample() ;

				createEntities( 10, 10 ) ;

				createMouseAnimExample() ;
				createSpinningCubeExample() ;

				createEventMessageTest() ;

				getInternalController().passEvent( new Event<Boolean>( "SHOW_GAME_STATE_FPS", true ) ) ;
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
				final World world = WorldAssist.getDefault() ;
				final DrawUpdaterPool pool = RenderPools.getDrawUpdaterPool() ;
				final Program geomProgram = ProgramAssist.add( new Program( "SIMPLE_GEOMETRY" ) ) ;

				{
					final IntVector2 dim = world.getRenderDimensions( new IntVector2() ) ;

					final Camera cam = CameraAssist.add( new Camera( "OFFSIDE" ) ) ;
					cam.setOrthographic( 0.0f, dim.y, 0.0f, dim.x, -1000.0f, 1000.0f ) ;
					cam.setScreenResolution( dim.x / 4, dim.y / 4 ) ;
					//CameraAssist.amendScreenOffset( cam, 200, 200 ) ;

					world.addCameras( cam ) ;
				}

				{
					final MalletColour colour = new MalletColour( 255, 255, 255 ) ;
					final Shape lines = new Shape( Shape.Style.LINE_STRIP, 7, 6 ) ;
					lines.copyVertex( Shape.construct( 0, 10, 0, colour ) ) ;
					lines.copyVertex( Shape.construct( 0, 0, 0, colour ) ) ;
					lines.copyVertex( Shape.construct( 100, 0, 0, colour ) ) ;
					lines.copyVertex( Shape.construct( 100, 5, 0, colour ) ) ;
					lines.copyVertex( Shape.construct( 200, 0, 0, colour ) ) ;
					lines.copyVertex( Shape.construct( 200, 10, 0, colour ) ) ;

					lines.addIndex( 0 ) ;
					lines.addIndex( 1 ) ;
					lines.addIndex( 2 ) ;
					lines.addIndex( 3 ) ;
					lines.addIndex( 2 ) ;
					lines.addIndex( 4 ) ;
					lines.addIndex( 5 ) ;

					final Draw draw = new Draw() ;
					draw.setPosition( 0.0f, 50.0f, 0.0f ) ;
					draw.setOffset( -100.0f, 0.0f, 0.0f ) ;
					draw.setShape( lines ) ;

					final DrawUpdater updater = pool.getOrCreate( world, geomProgram, lines, false, 10 ) ;

					final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
					geometry.addDraws( draw ) ;
				}

				{
					final MalletTexture texture = new MalletTexture( "base/textures/moomba.png" ) ;
					final int width = texture.getWidth() ;
					final int height = texture.getHeight() ;

					final Shape plane = Shape.constructPlane( new Vector3( width, height, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

					final Draw draw = new Draw() ;
					draw.setPosition( 415.0f, 385.0f, 0.0f ) ;
					draw.setOffset( -( width / 2 ), -( height / 2 ), 0.0f ) ;
					draw.setShape( plane ) ;

					final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
					program.mapUniform( "inTex0", texture ) ;

					final DrawUpdater updater = pool.getOrCreate( world, program, plane, true, 10 ) ;

					final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
					geometry.addDraws( draw ) ;
				}
			}

			/**
				Add an animation directly to the animation system
			**/
			public void renderAnimationExample()
			{
				final World world = WorldAssist.getDefault() ;
				final Program program = new Program( "SIMPLE_TEXTURE" ) ;
				final Shape plane = Shape.constructPlane( new Vector3( 64, 64, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

				final Draw draw = new Draw( 0, 0, 0, -32, -32, 0 ) ;
				draw.setShape( plane ) ;

				final SpriteAnimations animations = new SpriteAnimations( new SimpleSpriteListener( world, program, draw, 10 ) ) ;

				AnimationAssist.add( animations ) ;
				animations.addSprite( "DEFAULT", new MalletSprite( "base/anim/moomba.anim" ) ) ;
				animations.play( "DEFAULT" ) ;

				final Entity entity = new Entity( 1, Entity.AllowEvents.NO ) ;
				new Component( entity )
				{
					private final static float DURATION = 5.0f ;
					private float elapsed = 0.0f ;

					@Override
					public void update( final float _dt )
					{
						elapsed += _dt ;
						if( elapsed >= DURATION )
						{
							elapsed = 0.0f ;
						}
					}

					@Override
					public void readyToDestroy( final Entity.ReadyCallback _callback )
					{
						AnimationAssist.remove( animations ) ;
						super.readyToDestroy( _callback ) ;
					}
				} ;
				
				addEntity( entity ) ;
			}

			/**
				Add text and render directly to the renderer
			**/
			public void renderTextExample()
			{
				final World world = WorldAssist.getDefault() ;

				final TextDraw draw = new TextDraw( "Hello world!" ) ;
				draw.setPosition( 0.0f, -80.0f, 0.0f ) ;
				draw.setColour( new MalletColour( 144, 195, 212 ) ) ;

				final Program program = ProgramAssist.add( new Program( "SIMPLE_FONT" ) ) ;
				program.mapUniform( "inTex0", new MalletFont( "Arial", 20 ) ) ;

				final TextUpdaterPool pool = RenderPools.getTextUpdaterPool() ;
				final TextUpdater updater = pool.getOrCreate( world, program, false, 200 ) ;

				final TextBuffer geometry = updater.getBuffer( 0 ) ;
				geometry.addDraws( draw ) ;
			}

			/**
				Play audio file directly to the audio system
			**/
			public void playAudioExample()
			{
				/*eventSystem.addEvent( AudioAssist.constructAudioDelegate( ( final AudioDelegate _delegate ) ->
				{
					final Emitter emitter = new Emitter( "base/music/test.wav", StreamType.STATIC, Category.Channel.MUSIC ) ;
					emitter.setCallback( new SourceCallback()
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
							_delegate.play( emitter ) ;
						}
					} ) ;
					_delegate.play( _delegate.add( emitter ) ) ;
				} ) ) ;*/

				final OGG ogg = OGG.readOGG( "base/music/test.ogg" ) ;
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
				}
			}

			public void createEntities( final int _row, final int _column )
			{
				final int amount = _row * _column ;
				final Vector3 dim = new Vector3( 64, 64, 0 ) ;
				final Shape plane = Shape.constructPlane( dim, new Vector2( 0, 0 ), new Vector2( 1, 1 ) ) ;

				final Program program = ProgramAssist.add( new Program( "SIMPLE_INSTANCE_TEXTURE" ) ) ;
				program.mapUniform( "inTex0", new MalletTexture( "base/textures/moomba.png" ) ) ;

				final Entity entity = new Entity( 1 + amount, Entity.AllowEvents.NO ) ;

				final List<Hull> hulls = MalletList.<Hull>newList( amount ) ;
				final Draw[] draws = new Draw[amount] ;
				final Draw[] debugDraws = new Draw[amount] ;

				int inc = 0 ;
				for( int i = 0; i < _row; ++i )
				{
					for( int j = 0; j < _column; ++j )
					{
						final int x = 50 + ( i * 50 ) ;
						final int y = 50 + ( j * 50 ) ;

						final Vector2 position = new Vector2( x, y ) ;
						final CollisionComponent coll = CollisionComponent.generateBox2D( entity,
																						  Entity.AllowEvents.NO,
																						  new Vector2(),
																						  new Vector2( 64, 64 ),
																						  position,
																						  new Vector2( -32, -32 ) ) ;
						final Hull hull = coll.hulls[0] ;

						final Draw draw = new Draw() ;
						draw.setPosition( position.x, position.y, 0.0f ) ;
						draw.setOffset( -32.0f, -32.0f, 0.0f ) ;
						//draw.setShape( plane ) ;

						hulls.add( hull ) ;
						draws[inc] = draw ;
						debugDraws[inc] = Debug.createDraw( hull ) ;
						inc += 1 ;
					}
				}

				final RenderComponent render = new RenderComponent( entity, Entity.AllowEvents.NO )
				{
					private DrawInstancedUpdater updater ;
					//private DrawUpdater debugUpdater ;
					private final Vector2 position = new Vector2() ;

					@Override
					public void init()
					{
						final World world = WorldAssist.getDefault() ;

						{
							final DrawInstancedUpdaterPool pool = RenderPools.getDrawInstancedUpdaterPool() ;
							updater = pool.getOrCreate( world, program, plane, false, 10 ) ;

							final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
							geometry.addDraws( draws ) ;
						}

						{
							//final Program program = ProgramAssist.add( new Program( "SIMPLE_GEOMETRY" ) ) ;
							//debugUpdater = getUpdater( world, program, debugDraws[0], false, 10 ) ;
							//debugUpdater.addDraws( debugDraws ) ;
						}
					}

					@Override
					public void shutdown()
					{
						final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
						geometry.addDraws( draws ) ;
						//debugUpdater.removeDraws( debugDraws ) ;
					}

					@Override
					public void update( final float _dt )
					{
						super.update( _dt ) ;
						boolean updateDraw = false ;
						
						for( int i = 0; i < draws.length; ++i )
						{
							final Hull hull = hulls.get( i ) ;
							updateDraw = ( hull.contactData.size() > 0 ) ? true : updateDraw ;
							
							final Draw draw = draws[i] ;
							//final Draw debugDraw = debugDraws[i] ;

							hull.getPosition( position ) ;
							draw.setPosition( position.x, position.y, 0.0f ) ;

							//Debug.updateDraw( debugDraw, hull ) ;
						}

						if( updateDraw == true )
						{
							updater.makeDirty() ;
							//debugUpdater.makeDirty() ;
						}
					}
				} ;

				addEntity( entity ) ;
			}

			/**
				Create an Entity that follows the mouse
			**/
			public void createMouseAnimExample()
			{
				final World world = WorldAssist.getDefault() ;
				final IntVector2 dim = world.getRenderDimensions( new IntVector2() ) ;

				final Program program = new Program( "SIMPLE_TEXTURE" ) ;
				final Shape plane = Shape.constructPlane( new Vector3( 32, 32, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

				final Draw draw = new Draw( 0, 0, 0, -16, -16, 0 ) ;
				draw.setShape( plane ) ;

				final Entity entity = new Entity( 2, Entity.AllowEvents.NO ) ;
				final SpriteAnimations animations = new SpriteAnimations( new SimpleSpriteListener( world, program, draw, 100 ) ) ;

				AnimationAssist.add( animations ) ;
				animations.addSprite( "DEFAULT", new MalletSprite( "base/anim/moomba.anim" ) ) ;
				animations.play( "DEFAULT" ) ;

				final CollisionComponent collision = CollisionComponent.generateBox2D( entity,
																						new Vector2(),
																						new Vector2( 32, 32 ),
																						new Vector2( 0, 0 ),
																						new Vector2( -16, -16 ) ) ;
				//collision.hull.setPhysical( false ) ;

				final MouseComponent mouse = new MouseComponent( entity )
				{
					@Override
					public void applyMousePosition( final Vector2 _mouse )
					{
						draw.setPosition( _mouse.x, _mouse.y, 0.0f ) ;
					}

					@Override
					public void readyToDestroy( final Entity.ReadyCallback _callback )
					{
						AnimationAssist.remove( animations ) ;
						super.readyToDestroy( _callback ) ;
					}
				} ;
				
				addEntity( entity ) ;
			}

			public void createSpinningCubeExample()
			{
				final Entity entity = new Entity( 1, Entity.AllowEvents.NO ) ;

				new RenderComponent( entity )
				{
					private DrawUpdater updater ;

					private Draw draw ;
					private final Vector3 rotate = new Vector3() ;

					@Override
					public void init()
					{
						final World world = WorldAssist.getDefault() ;
						final GLTF gltf = GLTF.load( "base/models/cube.glb" ) ;
						final Shape shape = gltf.createMeshByIndex( 0, MalletList.<Tuple<String, Swivel>>toArray(
							Tuple.<String, Swivel>build( "POSITION", Swivel.VEC3 ),
							Tuple.<String, Swivel>build( "", Swivel.FLOAT ),
							Tuple.<String, Swivel>build( "TEXCOORD_0", Swivel.VEC2 )
						) ) ;
						System.out.println( "indices: " + shape.getIndicesSize() + " Vertices: " + shape.getVerticesSize() ) ;
						//final Shape shape = Shape.constructCube( 1.0f, new Vector2(), new Vector2( 1, 1 ) ) ;

						draw = new Draw() ;
						draw.setPosition( 0.0f, -200.0f, 0.0f ) ;
						//draw.setOffset( 0.0f, 0.0f, -50.0f ) ;
						draw.setScale( 100, 100, 100 ) ;
						draw.setShape( shape ) ;

						final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
						program.mapUniform( "inTex0", new MalletTexture( "base/textures/moomba.png" ) ) ;

						final DrawUpdaterPool pool = RenderPools.getDrawUpdaterPool() ;
						updater = pool.getOrCreate( world, program, draw.getShape(), false, 10 ) ;

						final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
						geometry.addDraws( draw ) ;
					}

					@Override
					public void shutdown()
					{
						final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
						geometry.removeDraws( draw ) ;
					}

					@Override
					public void update( final float _dt )
					{
						super.update( _dt ) ;
						rotate.x += 2.5f * _dt ;
						rotate.y += 2.8f * _dt ;
						rotate.z += 2.1f * _dt ;

						draw.setRotation( rotate.x, rotate.y, rotate.z ) ;
						updater.makeDirty() ;
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
						public EventController createStateEventController( final Tuple<String, EventController.IProcessor<?>> ... _processors )
						{
							return super.createStateEventController( MalletList.concat( _processors,
								EventController.create( "TEST_EVENT", ( final String _message ) ->
								{
									System.out.println( "Received: " + _message ) ;
								} ) )
							) ;
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
