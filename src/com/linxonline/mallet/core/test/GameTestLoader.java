package com.linxonline.mallet.core.test ;

import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.ArrayList ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.core.* ;
import com.linxonline.mallet.animation.* ;
import com.linxonline.mallet.ecs.* ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.ogg.OGG ;
import com.linxonline.mallet.io.formats.ogg.Vorbis ;
import com.linxonline.mallet.io.formats.gltf.GLTF ;
import com.linxonline.mallet.io.serialisation.Serialise ;

import com.linxonline.mallet.physics.Debug ;
import com.linxonline.mallet.physics.Hull ;
import com.linxonline.mallet.physics.CollisionAssist ;

import com.linxonline.mallet.util.caches.MemoryPool ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.Parallel ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.io.net.UDPServer ;
import com.linxonline.mallet.io.net.UDPClient ;
import com.linxonline.mallet.io.net.IOutStream ;
import com.linxonline.mallet.io.net.InStream ;
import com.linxonline.mallet.io.net.Address ;

import com.linxonline.mallet.script.IScriptEngine ;
import com.linxonline.mallet.script.Script ;
import com.linxonline.mallet.script.JavaInterface ;

import com.linxonline.mallet.script.javascript.JSScriptEngine ;

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
		generators.put( "simple_array", new MaterialPool.SimpleArrayGenerator() ) ;
		generators.put( "simple_instanced", new MaterialPool.SimpleInstancedGenerator() ) ;
	}

	private static final MaterialPool<Program> materialPool = new MaterialPool<Program>( generators ) ;

	public GameTestLoader() {}

	@Override
	public void loadGame( final ISystem _main, final IGameSystem _system )
	{
		_system.addGameState( new GameState( "DEFAULT", _main )
		{
			private IScriptEngine jsEngine ;
			private ECSEvent ecsEvents ;

			private final ECSInput ecsInput = new ECSInput( inputWorldSystem, inputUISystem ) ;
			private final ECSCollision ecsCollision = new ECSCollision() ;

			private final ECSUpdate<ExComponent, ExData> ecsExample = new ECSUpdate<ExComponent, ExData>( ( final ECSEntity _parent, final ExData _data ) ->
			{
				return new ExComponent( _parent, _data ) ;
			} ) ;

			private final ECSUpdate<ECSUpdate.Component, Object> ecsGeneral = new ECSUpdate<ECSUpdate.Component, Object>() ;

			@Override
			protected void createUpdaters( final List<IUpdate> _main, final List<IUpdate> _draw )
			{
				jsEngine = new JSScriptEngine() ;
				ecsEvents = new ECSEvent( eventSystem, system.getEventSystem() ) ;

				_main.add( ecsInput ) ;
				_main.add( ecsEvents ) ;
				_main.add( ecsExample ) ;
				_main.add( ecsGeneral ) ;
				_main.add( ecsCollision ) ;
				_main.add( ( final double _dt ) -> { jsEngine.update( _dt ) ; } ) ;

				final ISystem.ShutdownDelegate shutdown = system.getShutdownDelegate() ;
				shutdown.addShutdownCallback( () ->
				{
					Logger.println( "Shutting down script engine.", Logger.Verbosity.MINOR ) ;
					jsEngine.close() ;
				} ) ;
			}

			@Override
			public void initGame()			// Called when state is started
			{
				final UIVariant variant = new UIVariant( "Test", Action.KEEP ) ;

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

				createProgramTest() ;
				createMathTests() ;

				createUI() ;
				renderTextureArrayExample() ;
				renderTextureExample() ;
				renderAnimationExample() ;
				renderTextExample() ;
				//playAudioExample() ;

				createECSEntities( 10, 10 ) ;

				createMouseAnimExample() ;
				createSpinningCubeExample() ;

				getInternalController().passEvent( Event.<Boolean>create( "SHOW_GAME_STATE_FPS", true ) ) ;
				createScript() ;
			}

			private void createProgramTest()
			{
				final ArrayUniform a = new ArrayUniform( 2 ) ;
				a.set( 0, new Vector2( 1, 1 ) ) ;
				a.set( 1, new Vector2( 2, 2 ) ) ;

				final StructUniform s = new StructUniform() ;
				s.map( "vector2", new Vector2( 3, 3 ) ) ;
				s.map( "vector3", new Vector3( 4, 4, 4 ) ) ;
				s.map( "array", a ) ;

				final Program program = new Program( "TEST" ) ;
				program.mapUniform( "array", a ) ;
				program.mapUniform( "struct", s ) ;
				program.mapUniform( "mat4", new Matrix4() ) ;

				final UniformList draw = program.getDrawUniforms() ;
				draw.add( "draw1" ) ;
				draw.add( "draw2" ) ;

				program.forEachUniform( ( final String _absoluteName, final IUniform _uniform ) ->
				{
					System.out.println( _absoluteName + " : " + _uniform.toString() ) ;
					return true ;
				} ) ;
			}

			private void createMathTests()
			{
				final Plane plane = new Plane( new Vector3( 100, 0, 10 ),
											   new Vector3( 100, 100, 10 ),
											   new Vector3( 0, 0, 10 ) ) ;
				System.out.println( "Closest Point: " + plane.projectOnTo( new Vector3( 150, 150, 100 ) ).toString() ) ;

				final Circle circle = new Circle( 0, 0, 5 ) ;
				System.out.println( "Ray 5, 5, 0, -1: " ) ;
				if( circle.ray( 5, 5, 0, -1 ) )
				{
					System.out.println( "\tLine intersects." ) ;
				}

				System.out.println( "Ray 0, 0, 0, 1: " ) ;
				if( circle.ray( 0, 0, 0, 1 ) )
				{
					System.out.println( "\tLine intersects." ) ;
				}

				System.out.println( "Ray 10, 10, 1, 1: " ) ;
				if( !circle.ray( 10, 10, 1, 1 ) )
				{
					System.out.println( "\t Line does not intersect." ) ;
				}

				System.out.println( "Ray -10, -10, 1, 1:" ) ;
				if( circle.ray( -10, -10, 1, 1 ) )
				{
					System.out.println( "\tLine intersects." ) ;
				}

				System.out.println( "Line Segment -10, -10, 10, 10:" ) ;
				if( circle.intersectLineSegment( -10, -10, 10, 10 ) )
				{
					System.out.println( "\tLine Segment intersects." ) ;
				}

				System.out.println( "Line Segment 6, 6, 10, 10:" ) ;
				if( !circle.intersectLineSegment( 6, 6, 10, 10 ) )
				{
					System.out.println( "\tLine Segment does not intersect." ) ;
				}

				System.out.println( "Line Segment 6, 6, 0, 6:" ) ;
				if( !circle.intersectLineSegment( 6, 6, 0, 6 ) )
				{
					System.out.println( "\tLine Segment does not intersect." ) ;
				}

				System.out.println( "Line Segment 0, 0, 5, 0:" ) ;
				if( circle.intersectLineSegment( 0, 0, 5, 0 ) )
				{
					System.out.println( "\tLine Segment intersects." ) ;
				}

				System.out.println( "Line Segment 0, 0, -5, 0:" ) ;
				if( circle.intersectLineSegment( 0, 0, -5, 0 ) )
				{
					System.out.println( "\tLine Segment intersects." ) ;
				}

				System.out.println( "Line Segment 0, 0, 0, 5:" ) ;
				if( circle.intersectLineSegment( 0, 0, 0, 5 ) )
				{
					System.out.println( "\tLine Segment intersects." ) ;
				}

				System.out.println( "Line Segment 0, 0, 0, -5:" ) ;
				if( circle.intersectLineSegment( 0, 0, 0, -5 ) )
				{
					System.out.println( "\tLine Segment intersects." ) ;
				}
				
				System.out.println( "Line Segment 0, -6, 5, -7:" ) ;
				if( !circle.intersectLineSegment( 0, -6, 5, -7 ) )
				{
					System.out.println( "\tLine Segment does not intersect." ) ;
				}

				final Circle circle2 = new Circle( 505, -1206, 100 ) ;
				System.out.println( "Line Segment 400, -928, 544, -944: " ) ;
				if( !circle2.intersectLineSegment( 400, -928, 544, -944 ) )
				{
					System.out.println( "\tLine Segment does not intersect." ) ;
				}
			}

			public void createECSEntities( final int _row, final int _column )
			{
				final ECSEntity.ICreate<Object> create = ( final ECSEntity _parent, final Object _data ) ->
				{
					final Hull[] hulls = new Hull[_row * _column] ;
					for( int i = 0; i < _row; ++i )
					{
						for( int j = 0; j < _column; ++j )
						{
							final Hull hull = CollisionAssist.createBox2D( AABB.create( 0, 0, 64, 64 ), null ) ;
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

					final ExComponent executor = ecsExample.create( _parent, new ExData( messenger, collision ) ) ;

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
					ecsCollision.remove( ( ECSCollision.Component )_components[3] ) ;
				} ;

				final ECSEntity entity = new ECSEntity( create, destroy ) ;
			}

			public void createScript()
			{
				final Script script = Script.create( "base/scripts/example-count.js", IExtension.class ) ;
				final Count count = script.register( "counter", new Count() ) ;

				script.setListener( new Script.IListener()
				{
					@Override
					public void added( final Object _functions )
					{
						count.setScriptFunctions( ( IExtension )_functions ) ;
					}

					@Override
					public void removed()
					{
						System.out.println( "Script removed." ) ;
					}
				} ) ;

				jsEngine.add( script ) ;
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
				Add a texture array and render directly to the renderer
			**/
			public void renderTextureArrayExample()
			{
				final TextureArray ex1 = TextureArray.create( new String[]
				{
					"base/textures/moomba.png",
					"base/textures/moomba1.png"
				} ) ;

				final TextureArray ex2 = TextureArray.create( new String[]
				{
					"base/textures/moomba.png",
					"base/textures/moomba1.png"
				} ) ;

				final TextureArray ex3 = TextureArray.create( new String[]
				{
					"base/textures/moomba1.png",
					"base/textures/moomba.png"
				} ) ;

				System.out.println( "Ex1 same as Ex2: " + ex1.equals( ex2 ) ) ;
				System.out.println( "Ex1 not same as Ex3: " + ex1.equals( ex3 ) ) ;

				final World world = WorldAssist.getDefault() ;
				final DrawUpdaterPool pool = RenderPools.getDrawUpdaterPool() ;

				{
					final Shape plane = Shape.constructPlane( new Vector3( 128, 128, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

					final Draw draw = new Draw() ;
					draw.setPosition( 415.0f, 485.0f, 0.0f ) ;
					draw.setOffset( -( 128 / 2 ), -( 128 / 2 ), 0.0f ) ;
					draw.setShape( plane ) ;

					final Program program = materialPool.create( "base/materials/example_array.mat" ) ;
					final DrawUpdater updater = pool.getOrCreate( world, program, plane, true, 10 ) ;

					final GeometryBuffer geometry = updater.getBuffer( 0 ) ;
					geometry.addDraws( draw ) ;
				}
			}

			public void renderTextureExample()
			{
				final World world = WorldAssist.getDefault() ;
				final DrawUpdaterPool pool = RenderPools.getDrawUpdaterPool() ;

				final Program geomProgram = ProgramAssist.add( new Program( "SIMPLE_GEOMETRY", Shape.Style.LINE_STRIP, new Attribute[]
				{
					new Attribute( "inVertex", IShape.Attribute.VEC3 ),
					new Attribute( "inColour", IShape.Attribute.FLOAT )
				} ) ) ;

				{
					final IntVector2 dim = world.getRenderDimensions( new IntVector2() ) ;
					final int left = ( int )( -dim.x * 0.5f ) ;
					final int right = left + dim.x ;

					final int top = ( int )( -dim.y * 0.5f ) ;
					final int bottom = top + dim.y ;

					final Camera cam = CameraAssist.add( new Camera( "OFFSIDE" ) ) ;
					cam.setOrthographic( Camera.Mode.WORLD, top, bottom, left, right, -1000.0f, 1000.0f ) ;
					cam.setScreenResolution( dim.x / 4, dim.y / 4 ) ;

					world.addCameras( cam ) ;
				}

				{
					final Colour colour = new Colour( 255, 255, 255 ) ;
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

					final IShape.Attribute[] attributes = new IShape.Attribute[]
					{
						IShape.Attribute.VEC3,
						IShape.Attribute.FLOAT,
						IShape.Attribute.VEC2
					} ;

					final Vector3 position = new Vector3() ;
					final Colour white = Colour.white() ;
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

				final var booklet = new AnimationBooklet<SimpleFrame>( new SimpleFrame.Listener( world, program, draw, 10 ) ) ;

				AnimationAssist.add( booklet ) ;
				booklet.addAnimation( "DEFAULT", AnimatorGenerator.load( "base/anim/example.anim", new SimpleFrame.Generator() )  ) ;
				booklet.play( "DEFAULT" ) ;
			
				final ECSEntity.ICreate<Object> create = ( final ECSEntity _parent, final Object _data ) ->
				{
					return new ECSEntity.Component[] { } ;
				} ;

				final ECSEntity.IDestroy destroy = ( final ECSEntity.Component[] _components ) ->
				{
					ProgramAssist.remove( program ) ; 
					AnimationAssist.remove( booklet ) ;
				} ;

				final ECSEntity entity = new ECSEntity( create, destroy ) ;
			}

			/**
				Add text and render directly to the renderer
			**/
			public void renderTextExample()
			{
				final World world = WorldAssist.getDefault() ;

				final TextDraw draw = new TextDraw( "Hello world!" ) ;
				draw.setPosition( 0.0f, -80.0f, 0.0f ) ;
				draw.setColour( new Colour( 144, 195, 212 ) ) ;
				draw.setBoundary( 50.0f, 80.0f ) ;

				final Program program = ProgramAssist.add( new Program( "SIMPLE_FONT" ) ) ;
				program.mapUniform( "inTex0", new Font( "Arial", 20 ) ) ;

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
				/*AudioAssist.getAudioDelegate( ( final AudioDelegate _delegate ) ->
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
				} ) ;*/

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

			/**
				Create an Entity that follows the mouse
			**/
			public void createMouseAnimExample()
			{
				final Camera camera = CameraAssist.getDefault() ;
				camera.setPosition( 0, 0, -250 ) ;
				camera.setPerspective( Camera.Mode.WORLD, 130.0f, 0.1f, 10000.0f ) ;
				camera.lookAt( 0.0f, 0.0f, 0.0f ) ;
				CameraAssist.update( camera ) ;

				final World world = WorldAssist.getDefault() ;

				final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
				final Shape plane = Shape.constructPlane( new Vector3( 32, 32, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

				final Draw draw = new Draw( 0, 0, 0, -16, -16, 0 ) ;
				draw.setShape( plane ) ;

				final var booklet = new AnimationBooklet<SimpleFrame>( new SimpleFrame.Listener( world, program, draw, 10 ) ) ;

				AnimationAssist.add( booklet ) ;
				booklet.addAnimation( "DEFAULT", AnimatorGenerator.load( "base/anim/example.anim", new SimpleFrame.Generator() ) ) ;
				booklet.play( "DEFAULT" ) ;

				final ECSEntity.ICreate<Object> create = ( final ECSEntity _parent, final Object _data ) ->
				{
					final Vector3 mouse = new Vector3() ;

					return new ECSEntity.Component[]
					{
						ecsInput.createWorld( _parent, ( final InputEvent _event ) ->
						{
							switch( _event.getInputType() )
							{
								case MOUSE_MOVED     : 
								case TOUCH_MOVE      :
								{
									mouse.setXYZ( _event.mouseX, _event.mouseY, -1.0f ) ;
									camera.inputToNDC( mouse, mouse ) ;

									mouse.multiply( 200.0f ) ;
									draw.setPosition( mouse.x, -mouse.y, 0.0f ) ;

									camera.lookAt( mouse.x, mouse.y, 0.0f ) ;
									CameraAssist.update( camera ) ;
									break;
								}
								default              : break ;
							}

							return InputEvent.Action.PROPAGATE ;
						} )
					} ;
				} ;

				final ECSEntity.IDestroy destroy = ( final ECSEntity.Component[] _components ) ->
				{
					ecsInput.remove( ( ECSInput.Component )_components[0] ) ;

					AnimationAssist.remove( booklet ) ;
					ProgramAssist.remove( program ) ;
				} ;

				final ECSEntity entity = new ECSEntity( create, destroy ) ;
			}

			public void createSpinningCubeExample()
			{
				final World world = WorldAssist.getDefault() ;
				final GLTF gltf = GLTF.load( "base/models/cube.glb" ) ;
				final Shape shape = gltf.createMeshByIndex( 0, MalletList.<Tuple<String, IShape.Attribute>>toArray(
					Tuple.<String, IShape.Attribute>build( "POSITION", IShape.Attribute.VEC3 ),
					Tuple.<String, IShape.Attribute>build( "", IShape.Attribute.FLOAT ),
					Tuple.<String, IShape.Attribute>build( "TEXCOORD_0", IShape.Attribute.VEC2 )
				) )[0] ;
				
				//System.out.println( "indices: " + shape.getIndicesSize() + " Vertices: " + shape.getVerticesSize() ) ;
				//final Shape shape = Shape.constructCube( 1.0f, new Vector2(), new Vector2( 1, 1 ) ) ;

				final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
				program.mapUniform( "inTex0", new Texture( "base/textures/moomba.png" ) ) ;

				final DrawUpdaterPool pool = RenderPools.getDrawUpdaterPool() ;
				final DrawUpdater updater = pool.getOrCreate( world, program, shape, false, 10 ) ;

				final GeometryBuffer geometry = updater.getBuffer( 0 ) ;

				final ECSEntity.ICreate<Object> create = ( final ECSEntity _parent, final Object _data ) ->
				{
					final Draw draw = new Draw() ;
					draw.setPosition( 0.0f, -200.0f, 0.0f ) ;
					draw.setScale( 100, 100, 100 ) ;
					draw.setShape( shape ) ;

					geometry.addDraws( draw ) ;

					return new ECSEntity.Component[]
					{
						ecsGeneral.create( _parent, ( final ECSEntity _p, final Object _d ) ->
						{
							return new ECSUpdate.Component( _p )
							{
								private final Vector3 rotate = new Vector3() ;

								@Override
								public void update( final float _dt )
								{
									rotate.x += 2.5f * _dt ;
									rotate.y += 2.8f * _dt ;
									rotate.z += 2.1f * _dt ;

									draw.setRotation( rotate.x, rotate.y, rotate.z ) ;
									updater.makeDirty() ;
								}

								@Override
								public void shutdown()
								{
									geometry.removeDraws( draw ) ;
								}
							} ;
						} )
					} ;
				} ;

				final ECSEntity.IDestroy destroy = ( final ECSEntity.Component[] _components ) ->
				{
					ecsGeneral.remove( ( ECSUpdate.Component )_components[0] ) ;
				} ;

				final ECSEntity entity = new ECSEntity( create, destroy ) ;
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
		public void reset() ;

		// You can return Java objects but they'll
		// only be able to access functions defined
		// with @JavaInterface
		public IHello create() ;
		public int[] primitiveArray() ;
		public IHello[] objectArray() ;
		public List<IHello> objectList() ;
	}

	@JavaInterface
	public interface IHello
	{
		public String hello() ;
	}

	public interface IExtension
	{
		// All functions that call into the script
		// should return void.
		public void notAValidFunction() ;
		public void countReseted() ;
	}

	public static class Count implements ICount
	{
		private int count = 0 ;
		private IExtension jsFunctions ;

		public Count() {}

		public void setScriptFunctions( final IExtension _functions )
		{
			jsFunctions = _functions ;
		}

		@Override
		public IHello create()
		{
			return () ->
			{
				return " World!" ;
			} ;
		}

		@Override
		public int[] primitiveArray()
		{
			return new int[] { 5, 4, 3, 2, 1 } ;
		}

		@Override
		public IHello[] objectArray()
		{
			return new IHello[]
			{
				() -> { return "Hello " ; },
				() -> { return "World!" ; }
			} ;
		}

		@Override
		public List<IHello> objectList()
		{
			List<IHello> list = new ArrayList<IHello>() ;
			list.add( () -> { return "Hello " ; } ) ;
			list.add( () -> { return "World!" ; } ) ;
			return list ;
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
		public void reset()
		{
			count = 0 ;
			jsFunctions.countReseted() ;
			jsFunctions.notAValidFunction() ;
		}
	}

	public static final class ExData
	{
		public final ECSEvent.Component messenger ;
		public final ECSCollision.Component collision ;

		public ExData( final ECSEvent.Component _messenger, final ECSCollision.Component _collision )
		{
			messenger = _messenger ;
			collision = _collision ;
		}
	}

	public static final class ExComponent extends ECSUpdate.Component
	{
		private final static MemoryPool<Vector2> vec2s = new MemoryPool<Vector2>( () -> new Vector2() ) ;

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

			final World world = WorldAssist.getDefault() ;

			final Program program = ProgramAssist.add( new Program( "SIMPLE_INSTANCE_TEXTURE" ) ) ;
			program.mapUniform( "inTex0", new Texture( "base/textures/moomba.png" ) ) ;
			final Shape plane = Shape.constructPlane( new Vector3( 64, 64, 0 ), new Vector2( 0, 0 ), new Vector2( 1, 1 ) ) ;

			final DrawInstancedUpdaterPool pool = RenderPools.getDrawInstancedUpdaterPool() ;
			updater = pool.getOrCreate( world, program, plane, false, 10 ) ;

			final Vector2 position = new Vector2() ;
			final Vector2 offset = new Vector2() ;
			final GeometryBuffer geometry = updater.getBuffer( 0 ) ;

			final Hull[] hulls = collision.getHulls() ;
			draws = new Draw[hulls.length] ;
			for( int i = 0; i < draws.length; ++i )
			{
				final Hull hull = hulls[i] ;
				hull.getPosition( position ) ;
				hull.getOffset( offset ) ;

				final Draw draw = new Draw() ;
				draw.setPosition( position.x, position.y, 0.0f ) ;
				draw.setOffset( offset.x, offset.y, 0.0f ) ;

				draws[i] = draw ;
			}

			geometry.addDraws( draws ) ;
		}

		@Override
		public void update( final float _dt )
		{
			Parallel.forBatch( collision.getHulls(), 1000, this::updateDraws ) ;

			/*acc += _dt ;
			if( acc >= 15.0f )
			{
				messenger.passEvent( Event.<String>create( "KILL_ENTITY", "We are now sending a message to kill the entity." ) ) ;
			}*/
		}

		private void updateDraws( final int _start, final int _end, final Hull[] _hulls )
		{
			final Vector2 position = vec2s.takeSync() ;

			int contact = 0 ;

			for( int i = _start; i < _end; ++i )
			{
				final Hull hull = _hulls[i] ;
				if( hull.contactData.size() <= 0 )
				{
					// If there has been no collisions then
					// they'll be no movement.
					continue ;
				}

				++contact ;
				final Draw draw = draws[i] ;

				hull.getPosition( position ) ;
				draw.setPosition( position.x, position.y, 0.0f ) ;
			}

			if( contact > 0 )
			{
				synchronized( updater )
				{
					updater.makeDirty() ;
				}
			}

			vec2s.reclaimSync( position ) ;
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
