package com.linxonline.mallet.core.test ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.animation.* ;
import com.linxonline.mallet.ecs.* ;

import com.linxonline.mallet.renderer.IShape.Attribute ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.ogg.OGG ;
import com.linxonline.mallet.io.formats.ogg.Vorbis ;
import com.linxonline.mallet.io.formats.gltf.GLTF ;
import com.linxonline.mallet.io.serialisation.Serialise ;

import com.linxonline.mallet.physics.Debug ;
import com.linxonline.mallet.physics.hulls.Hull ;
import com.linxonline.mallet.physics.CollisionAssist ;

import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.settings.Settings ;

import com.linxonline.mallet.io.net.UDPServer ;
import com.linxonline.mallet.io.net.UDPClient ;
import com.linxonline.mallet.io.net.IOutStream ;
import com.linxonline.mallet.io.net.InStream ;
import com.linxonline.mallet.io.net.Address ;

import com.linxonline.mallet.script.Script ;
import com.linxonline.mallet.script.JavaInterface ;

/**
	Example on how to implement the Game Loader class.
	Initialise your Game States and add them to the 
	Game System passed in. Remember to tell the Game System
	what is the default-state!
*/
public final class GameTestLoader implements IGameLoader
{
	private static final Map<String, MaterialPool.IGenerator<Program>> generators = MalletMap.<String, MaterialPool.IGenerator<Program>>newMap() ;
	static
	{
		// All materials should have a "type" field.
		generators.put( "simple", new MaterialPool.SimpleGenerator() ) ;
		generators.put( "simple_instanced", new MaterialPool.SimpleInstancedGenerator() ) ;
	}

	private static final MaterialPool<Program> materialPool = new MaterialPool<Program>( generators ) ;

	public GameTestLoader() {}

	@Override
	public void loadGame( final IGameSystem _system )
	{
		_system.addGameState( new GameState( "DEFAULT" )
		{
			private ECSEvent ecsEvents ;
			private ECSCollision ecsCollision ;
			private final ECSUpdate<ExComponent, ExData> ecsExample = new ECSUpdate<ExComponent, ExData>( ( final ECSEntity _parent, final ExData _data ) ->
			{
				return new ExComponent( _parent, _data ) ;
			} ) ;

			@Override
			protected void createUpdaters( final List<IUpdate> _main, final List<IUpdate> _draw )
			{
				ecsCollision = new ECSCollision() ;
				ecsEvents = new ECSEvent( eventSystem, system.getEventSystem() ) ;
				_main.add( ecsEvents ) ;
				_main.add( ecsExample ) ;
				_main.add( ecsCollision ) ;
			}

			@Override
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

				createPlaneTest() ;

				createUI() ;
				renderTextureExample() ;
				renderAnimationExample() ;
				renderTextExample() ;
				//playAudioExample() ;

				//createEntities( 10, 10 ) ;
				createECSEntities( 10, 10 ) ;

				createMouseAnimExample() ;
				createSpinningCubeExample() ;

				createEventMessageTest() ;

				getInternalController().passEvent( new Event<Boolean>( "SHOW_GAME_STATE_FPS", true ) ) ;
				createScript() ;
			}

			private void createPlaneTest()
			{
				final Plane plane = new Plane( new Vector3( 100, 0, 10 ),
											   new Vector3( 100, 100, 10 ),
											   new Vector3( 0, 0, 10 ) ) ;
				System.out.println( "Closest Point: " + plane.projectOnTo( new Vector3( 150, 150, 100 ) ).toString() ) ;
			}

			public void createECSEntities( final int _row, final int _column )
			{
				final World world = WorldAssist.getDefault() ;

				final Program program = ProgramAssist.add( new Program( "SIMPLE_INSTANCE_TEXTURE" ) ) ;
				program.mapUniform( "inTex0", new MalletTexture( "base/textures/moomba.png" ) ) ;
				final Shape plane = Shape.constructPlane( new Vector3( 64, 64, 0 ), new Vector2( 0, 0 ), new Vector2( 1, 1 ) ) ;

				final DrawInstancedUpdaterPool pool = RenderPools.getDrawInstancedUpdaterPool() ;
				final DrawInstancedUpdater updater = pool.getOrCreate( world, program, plane, false, 10 ) ;

				final ECSEntity.ICreate create = ( final ECSEntity _parent, final Object _data ) ->
				{
					final Hull[] hulls = new Hull[_row * _column] ;
					for( int i = 0; i < _row; ++i )
					{
						for( int j = 0; j < _column; ++j )
						{
							final Hull hull = CollisionAssist.createBox2D( new AABB( 0, 0, 64, 64 ), null ) ;
							hull.setPosition( i * 60, j * 60  ) ;
							hull.setOffset( -32, -32 ) ;

							final int index = ( i * _column ) + j ;
							hulls[index] = hull ;
						}
					}

					final ECSCollision.Component collision = ecsCollision.create( _parent, hulls ) ;

					final ECSEvent.Component messenger = ecsEvents.create( _parent, ECSEvent.Type.ENTITY ) ;
					final ECSEvent.Component processor = ecsEvents.create( _parent, ECSEvent.Type.ENTITY, EventController.create( "KILL_ENTITY", ( final String _message ) ->
					{
						_parent.destroy() ;
					} ) ) ;

					final ExComponent executor = ecsExample.create( _parent, new ExData( messenger, collision, updater ) ) ;

					return new ECSEntity.Component[]
					{
						messenger,
						processor,
						executor,
						collision
					} ;
				} ;

				final ECSEntity.IDestroy destroy = ( final ECSEntity.Component[] _components ) ->
				{
					ecsEvents.remove( ( ECSEvent.Component )_components[0] ) ;
					ecsEvents.remove( ( ECSEvent.Component )_components[1] ) ;
					ecsExample.remove( ( ExComponent )_components[2] ) ;
				} ;

				final ECSEntity entity = new ECSEntity( create, destroy ) ;
			}

			public void createScript()
			{
				final Entity entity = new Entity( 1 ) ;
				new CountComponent( entity, "base/scripts/example-count.js" ) ;

				addEntity( entity ) ;
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

				final Entity entity = new Entity( 1 ) ;
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
					final int width = 64 ;
					final int height = 64 ;

					final IShape.Attribute[] attributes = new IShape.Attribute[3] ;
					attributes[0] = IShape.Attribute.VEC3 ;
					attributes[1] = IShape.Attribute.FLOAT ;
					attributes[2] = IShape.Attribute.VEC2 ;

					final Vector3 position = new Vector3() ;
					final MalletColour white = MalletColour.white() ;
					final Vector2 uv = new Vector2() ;
					final Object[] vertex = new Object[] { position, white, uv } ; 

					final HEShape plane = new HEShape( attributes ) ;
					final HEShape.Vertex v0 = plane.addVertex( vertex ) ;

					position.setXYZ( 0.0f, height, 0.0f ) ;
					uv.setXY( 0.0f, 1.0f ) ;
					final HEShape.Vertex v1 = plane.addVertex( vertex ) ;

					final HEShape.Edge e0 = plane.addEdge( v0, v1 ) ;

					final HEShape.Edge e1 = e0.extrude() ;
					e1.translateVector3( 0, 64.0f, 0.0f, 0.0f ) ;
					e1.translateVector2( 2, 1.0f, 0.0f ) ;

					final HEShape.Edge e2 = e1.getPair().extrude() ;
					e2.translateVector3( 0, 64.0f, 0.0f, 0.0f ) ;
					e2.translateVector2( 2, 1.0f, 0.0f ) ;

					final HEShape.Edge e3 = e2.getPair().extrude() ;
					e3.translateVector3( 0, 64.0f, 0.0f, 0.0f ) ;
					e3.translateVector2( 2, 1.0f, 0.0f ) ;

					final HEShape.Edge e4 = e3.split( 0.5f ) ;
					final HEShape.Edge e5 = e4.getPair().extrude() ;
					e5.translateVector3( 0, 64.0f, 0.0f, 0.0f ) ;
					e5.translateVector2( 2, 1.0f, 0.0f ) ;

					final Draw draw = new Draw() ;
					draw.setPosition( 415.0f, 385.0f, 0.0f ) ;
					draw.setOffset( -( width / 2 ), -( height / 2 ), 0.0f ) ;
					draw.setShape( plane ) ;

					final Program program = materialPool.create( "base/materials/example.mat" ) ;
					final DrawUpdater updater = pool.getOrCreate( world, program, plane, true, 10 ) ;

					final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
					geometry.addDraws( draw ) ;

					final HEShape.Edge closestEdge = plane.getClosestEdge( 512.0f, 33.0f, 0.0f ) ;
					System.out.println( "Found Closest Edge: " + closestEdge.getOrigin().getVector3( 0, new Vector3() ) ) ;

					final HEShape.Vertex closestVertex = plane.getClosestVertex( 512.0f, 33.0f, 0.0f ) ;
					System.out.println( "Found Closest Vertex: " + closestVertex.getVector3( 0, new Vector3() ) ) ;
				}
			}

			/**
				Add an animation directly to the animation system
			**/
			public void renderAnimationExample()
			{
				final World world = WorldAssist.getDefault() ;
				final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
				final Shape plane = Shape.constructPlane( new Vector3( 64, 64, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

				final Draw draw = new Draw( 0, 0, 0, -32, -32, 0 ) ;
				draw.setShape( plane ) ;

				final AnimationBooklet booklet = new AnimationBooklet( new SimpleFrame.Listener( world, program, draw, 10 ) ) ;

				AnimationAssist.add( booklet ) ;
				booklet.addAnimation( "DEFAULT", AnimatorGenerator.load( "base/anim/example.anim", new SimpleFrame.Generator() )  ) ;
				booklet.play( "DEFAULT" ) ;

				final Entity entity = new Entity( 1 ) ;
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
						ProgramAssist.remove( program ) ; 
						AnimationAssist.remove( booklet ) ;
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

				final Entity entity = new Entity( 1 + amount ) ;

				final List<Hull> hulls = MalletList.<Hull>newList( amount ) ;
				final Draw[] draws = new Draw[amount] ;
				final Draw[] debugDraws = new Draw[amount] ;

				int inc = 0 ;
				for( int i = 0; i < _row; ++i )
				{
					for( int j = 0; j < _column; ++j )
					{
						final int x = 50 + ( i * 60 ) ;
						final int y = 50 + ( j * 60 ) ;

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

						hulls.add( hull ) ;
						draws[inc] = draw ;
						debugDraws[inc] = Debug.createDraw( hull ) ;
						inc += 1 ;
					}
				}

				new RenderComponent( entity, Entity.AllowEvents.NO )
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
						geometry.removeDraws( draws ) ;
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

				final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
				final Shape plane = Shape.constructPlane( new Vector3( 32, 32, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

				final Draw draw = new Draw( 0, 0, 0, -16, -16, 0 ) ;
				draw.setShape( plane ) ;

				final Entity entity = new Entity( 1 ) ;

				final AnimationBooklet booklet = new AnimationBooklet( new SimpleFrame.Listener( world, program, draw, 10 ) ) ;

				AnimationAssist.add( booklet ) ;
				booklet.addAnimation( "DEFAULT", AnimatorGenerator.load( "base/anim/example.anim", new SimpleFrame.Generator() ) ) ;
				booklet.play( "DEFAULT" ) ;

				new MouseComponent( entity )
				{
					@Override
					public void applyMousePosition( final Vector2 _mouse )
					{
						draw.setPosition( _mouse.x, _mouse.y, 0.0f ) ;
					}

					@Override
					public void readyToDestroy( final Entity.ReadyCallback _callback )
					{
						AnimationAssist.remove( booklet ) ;
						ProgramAssist.remove( program ) ;
						super.readyToDestroy( _callback ) ;
					}
				} ;
				
				addEntity( entity ) ;
			}

			public void createSpinningCubeExample()
			{
				final Entity entity = new Entity( 1 ) ;

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
						final Shape shape = gltf.createMeshByIndex( 0, MalletList.<Tuple<String, Attribute>>toArray(
							Tuple.<String, Attribute>build( "POSITION", Attribute.VEC3 ),
							Tuple.<String, Attribute>build( "", Attribute.FLOAT ),
							Tuple.<String, Attribute>build( "TEXCOORD_0", Attribute.VEC2 )
						) ) ;
						//System.out.println( "indices: " + shape.getIndicesSize() + " Vertices: " + shape.getVerticesSize() ) ;
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
					final Entity receive = new Entity( 0, Entity.AllowEvents.GAMESTATE )
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
					final Entity send = new Entity( 0, Entity.AllowEvents.GAMESTATE  ) ;
					send.passStateEvent( new Event<String>( "TEST_EVENT", "Hello World!" ) ) ;

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

	// Required for the scripting-system to pick it up.
	@JavaInterface
	public interface ICount
	{
		public void count() ;
		public int getCount() ;
	}

	// Required for the scripting-system to pick it up.
	@JavaInterface
	public interface IDestroy
	{
		public void destroy() ;
		public boolean isDead() ;
	}

	public interface IDestroyed
	{
		public void destroyed() ;
	}

	public interface IExtension extends IDestroyed
	{
		// All functions that call into the script
		// should return void.
		public void notAValidFunction() ;
	}

	public static class CountComponent extends ScriptComponent implements ICount, IDestroy
	{
		private int count = 0 ;
		private IExtension functions ;	// Script functions that can be called from the Java side.

		public CountComponent( final Entity _parent, final String _scriptPath )
		{
			super( _scriptPath, _parent ) ;

			final Script script = getScript() ;
			script.setScriptFunctions( IExtension.class ) ;

			script.setListener( new Script.IListener()
			{
				@Override
				public void added( final Object _functions )
				{
					functions = ( IExtension )_functions ;
				}

				public void removed() {}
			} ) ;
		}

		@Override
		public void count()
		{
			++count ;
		}

		@Override
		public int getCount()
		{
			return count ;
		}

		@Override
		public void destroy()
		{
			getParent().destroy() ;
			functions.destroyed() ;
			functions.notAValidFunction() ;
		}

		@Override
		public boolean isDead()
		{
			return getParent().isDead() ;
		}
	}

	public static class ExData
	{
		public final ECSEvent.Component messenger ;
		public final ECSCollision.Component collision ;
		public final DrawInstancedUpdater updater ;

		public ExData( final ECSEvent.Component _messenger, final ECSCollision.Component _collision, final DrawInstancedUpdater _updater )
		{
			messenger = _messenger ;
			collision = _collision ;
			updater = _updater ;
		}
	}

	public static class ExComponent extends ECSUpdate.Component
	{
		private final Vector2 position = new Vector2() ;
		private final DrawInstancedUpdater updater ;
		private final ECSEvent.Component messenger ;
		private final ECSCollision.Component collision ;
		private final Draw[] draws ;

		private float acc = 0 ;

		public ExComponent( final ECSEntity _parent, final ExData _data )
		{
			super( _parent ) ;
			messenger = _data.messenger ;
			collision = _data.collision ;
			updater = _data.updater ;

			final Vector2 offset = new Vector2() ;
			final GeometryBuffer geometry = updater.getBuffer( 0 ) ;

			final Hull[] hulls = collision.getHulls() ;
			draws = new Draw[hulls.length] ;
			for( int i = 0; i < draws.length; ++i )
			{
				final Hull hull = hulls[i] ;
				hull.getPosition( position ) ;
				hull.getOffset( offset ) ;

				draws[i] = new Draw() ;
				draws[i].setPosition( position.x, position.y, 0.0f ) ;
				draws[i].setOffset( offset.x, offset.y, 0.0f ) ;
			}

			geometry.addDraws( draws ) ;
		}

		@Override
		public void update( final float _dt )
		{
			boolean updateDraw = false ;

			final Hull[] hulls = collision.getHulls() ;
			for( int i = 0; i < draws.length; ++i )
			{
				final Hull hull = hulls[i] ;
				updateDraw = ( hull.contactData.size() > 0 ) ? true : updateDraw ;

				final Draw draw = draws[i] ;
				hull.getPosition( position ) ;
				draw.setPosition( position.x, position.y, 0.0f ) ;
			}

			if( updateDraw == true )
			{
				updater.makeDirty() ;
			}

			/*acc += _dt ;
			if( acc >= 15.0f )
			{
				messenger.passEvent( new Event<String>( "KILL_ENTITY", "We are now sending a message to kill the entity." ) ) ;
			}*/
		}

		@Override
		public void shutdown()
		{
			final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
			geometry.removeDraws( draws ) ;
			updater.forceUpdate() ;
		}
	}
}
