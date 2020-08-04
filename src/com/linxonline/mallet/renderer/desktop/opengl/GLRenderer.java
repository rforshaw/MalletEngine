package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;
import java.util.ArrayList ;

import com.jogamp.newt.opengl.GLWindow ;
import com.jogamp.opengl.GLAutoDrawable ;
import com.jogamp.opengl.GLProfile ;
import com.jogamp.opengl.GLCapabilities ;
import com.jogamp.opengl.GLContext ;
import com.jogamp.opengl.GLEventListener ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification.Notify ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.schema.SStruct ;
import com.linxonline.mallet.util.schema.SArray ;
import com.linxonline.mallet.util.schema.SNode ;

import com.linxonline.mallet.renderer.opengl.Worlds ;
import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.opengl.JSONProgram ;

public class GLRenderer extends BasicRenderer implements GLEventListener
{
	public final static int ORTHOGRAPHIC_MODE = 1 ;
	public final static int PERSPECTIVE_MODE  = 2 ;

	private final static ProgramManager<GLProgram> programs = new ProgramManager<GLProgram>( new ProgramManager.JSONBuilder<GLProgram>()
	{
		public GLProgram build( final JSONProgram _program )
		{
			return GLProgram.build( _program ) ;
		}
	} ) ;

	private final static GLTextureManager textures = new GLTextureManager() ;
	private final static GLFontManager fontManager = new GLFontManager( textures ) ;

	private final static Vector2 maxTextureSize = new Vector2() ;						// Maximum Texture resolution supported by the GPU.

	private GLWindow canvas ;
	private final Worlds<GLDraw, CameraData, GLWorld> worlds = new Worlds<GLDraw, CameraData, GLWorld>() ;

	private CameraData defaultCamera = new CameraData( "MAIN" ) ;
	private int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
		super() ;
		textures.setWorldState( worlds ) ;
		initWindow() ;
	}

	@Override
	public void start()
	{
		Logger.println( "Starting renderer..", Logger.Verbosity.NORMAL ) ;
		super.start() ;

		FontAssist.setAssist( createFontAssist() ) ;
		TextureAssist.setAssist( createTextureAssist() ) ;

		DrawAssist.setAssist( createDrawAssist() ) ;
		ProgramAssist.setAssist( createProgramAssist() ) ;
		StorageAssist.setAssist( createStorageAssist() ) ;

		WorldAssist.setAssist( createWorldAssist() ) ;
		CameraAssist.setAssist( createCameraAssist() ) ;

		canvas.addGLEventListener( this ) ;
		initDefaultWorld() ;

		canvas.setVisible( true ) ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		if( makeCurrent() == true )
		{
			clear() ;							// Clear the contents being rendered

			worlds.shutdown() ;
			programs.shutdown() ;
			textures.shutdown() ;				// We'll loose all texture and font resources
			fontManager.shutdown() ;
			release() ;
		}
	}

	@Override
	protected DrawDelegate constructDrawDelegate()
	{
		return new DrawDelegate()
		{
			private final ArrayList<GLDraw> data = new ArrayList<GLDraw>() ;

			@Override
			@SuppressWarnings( "unchecked" )
			public void addTextDraw( final TextDraw _draw )
			{
				addTextDraw( _draw, null ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addBasicDraw( final Draw _draw )
			{
				addBasicDraw( _draw, null ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addTextDraw( final List<TextDraw> _draws )
			{
				addTextDraw( _draws, null ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addBasicDraw( final List<Draw> _draws )
			{
				addBasicDraw( _draws, null ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addTextDraw( final TextDraw _draw, final World _world )
			{
				final GLWorld world = ( GLWorld )_world ;
				final GLDraw draw = ( GLDraw )_draw ;

				data.add( draw ) ;
				worlds.addDraw( draw, world ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addBasicDraw( final Draw _draw, final World _world )
			{
				final GLWorld world = ( GLWorld )_world ;
				final GLDraw draw = ( GLDraw )_draw ;

				data.add( draw ) ;
				worlds.addDraw( draw, world ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addTextDraw( final List<TextDraw> _draws, final World _world )
			{
				data.ensureCapacity( data.size() + _draws.size() ) ;
				final GLWorld world = ( GLWorld )_world ;
				final List<GLDraw> draws = ( List<GLDraw> )( Object )_draws ;

				data.addAll( draws ) ;
				worlds.addDraw( draws, world ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void addBasicDraw( final List<Draw> _draws, final World _world )
			{
				data.ensureCapacity( data.size() + _draws.size() ) ;
				final GLWorld world = ( GLWorld )_world ;
				final List<GLDraw> draws = ( List<GLDraw> )( Object )_draws ;

				data.addAll( draws ) ;
				worlds.addDraw( draws, world ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public void removeDraw( final Draw _draw )
			{
				final GLDraw draw = ( GLDraw )_draw ;
				if( draw != null )
				{
					data.remove( draw ) ;
					worlds.removeDraw( draw ) ;
				}
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public Camera getCamera( final String _id, final World _world )
			{
				final GLWorld world = ( GLWorld )_world ;
				return worlds.getCamera( _id, world ) ;
			}

			@Override
			@SuppressWarnings( "unchecked" )
			public World getWorld( final String _id )
			{
				return ( GLWorld )worlds.getWorld( _id ) ;
			}

			@Override
			public void shutdown()
			{
				if( data.isEmpty() == false )
				{
					for( final GLDraw draw : data  )
					{
						worlds.removeDraw( draw ) ;
					}
					data.clear() ;
				}
			}
		} ;
	}

	public FontAssist.Assist createFontAssist()
	{
		return new FontAssist.Assist()
		{
			@Override
			public MalletFont.Metrics createMetrics( final MalletFont _font )
			{
				return fontManager.generateMetrics( _font ) ;
			}

			@Override
			public Glyph createGlyph( final MalletFont _font, final int _code )
			{
				return fontManager.generateGlyph( _font, _code ) ;
			}

			@Override
			public boolean loadFont( final String _path )
			{
				assert( true ) ;
				return false ;
			}
		} ;
	}

	public TextureAssist.Assist createTextureAssist()
	{
		return new TextureAssist.Assist()
		{
			@Override
			public MalletTexture.Meta createMeta( final String _path )
			{
				return textures.getMeta( _path ) ;
			}

			@Override
			public MalletTexture.Meta createMeta( final World _world )
			{
				final GLWorld world = cast( _world ) ;
				return world.getMeta() ;
			}

			@Override
			public Vector2 getMaximumTextureSize()
			{
				return new Vector2( maxTextureSize ) ;
			}

			private GLWorld cast( final World _world )
			{
				assert( _world != null ) ;
				assert( !( _world instanceof GLWorld ) ) ;
				return ( GLWorld )_world ;
			}
		} ;
	}

	public DrawAssist.Assist createDrawAssist()
	{
		return new DrawAssist.Assist()
		{
			@Override
			public Draw amendUI( final Draw _draw, final boolean _ui )
			{
				final BasicDraw<GLProgram> basic = cast( _draw ).getBasicData() ;
				basic.setUI( _ui ) ;
				return _draw ;
			}

			@Override
			public Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation )
			{
				final BasicDraw<GLProgram> basic = cast( _draw ).getBasicData() ;
				basic.setInterpolationMode( _interpolation ) ;
				return _draw ;
			}

			@Override
			public Draw amendUpdateType( final Draw _draw, final UpdateType _type )
			{
				final BasicDraw<GLProgram> basic = cast( _draw ).getBasicData() ;
				basic.setUpdateType( _type ) ;
				return _draw ;
			}

			@Override
			public Draw forceUpdate( final Draw _draw )
			{
				final BasicDraw<GLProgram> basic = cast( _draw ).getBasicData() ;
				basic.forceUpdate() ;
				return _draw ;
			}

			@Override
			public boolean isUI( final Draw _draw )
			{
				final BasicDraw<GLProgram> basic = cast( _draw ).getBasicData() ;
				return basic.isUI() ;
			}

			@Override
			public TextDraw createTextDraw( final StringBuilder _text,
											final MalletFont _font,
											final Vector3 _position,
											final Vector3 _offset,
											final Vector3 _rotation,
											final Vector3 _scale,
											final int _order )
			{
				final GLTextDraw draw = new GLTextDraw( UpdateType.ON_DEMAND,
														Interpolation.NONE,
														_position,
														_offset,
														_rotation,
														_scale,
														_order ) ;

				final Program program = ProgramAssist.create( "SIMPLE_FONT" ) ;
				program.mapUniform( "inTex0", _font ) ;

				draw.setProgram( program ) ;
				final TextData text = draw.getTextData() ;
				text.setText( _text ) ;
				return draw ;
			}

			@Override
			public TextDraw createTextDraw( final String _text,
											final MalletFont _font,
											final Vector3 _position,
											final Vector3 _offset,
											final Vector3 _rotation,
											final Vector3 _scale,
											final int _order )
			{
				final StringBuilder builder = new StringBuilder( _text ) ;
				return createTextDraw( builder, _font, _position, _offset, _rotation, _scale, _order ) ;
			}

			@Override
			public StencilDraw createClipDraw( final Vector3 _position,
											   final Vector3 _offset,
											   final Vector3 _rotation,
											   final Vector3 _scale,
											   final int _startOrder,
											   final int _endOrder )
			{
				final GLStencilDraw draw = new GLStencilDraw( UpdateType.ON_DEMAND,
															  Interpolation.NONE,
															  _position,
															  _offset,
															  _rotation,
															  _scale,
															  _startOrder ) ;
				draw.setEndOrder( _endOrder ) ;

				draw.setProgram( ProgramAssist.create( "SIMPLE_STENCIL" ) ) ;
				return draw ;
			}

			@Override
			public Draw createDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _order )
			{
				return new GLBasicDraw( UpdateType.ON_DEMAND,
										Interpolation.NONE,
										_position,
										_offset,
										_rotation,
										_scale,
										_order ) ;
			}

			private GLDraw cast( final Draw _draw )
			{
				assert( _draw != null ) ;
				assert( !( _draw instanceof GLDraw ) ) ;
				return ( GLDraw )_draw ;
			}
		} ;
	}

	public ProgramAssist.Assist createProgramAssist()
	{
		return new ProgramAssist.Assist()
		{
			public void load( final String _id, final String _path ) {}

			public Program create( final String _id )
			{
				final Program program = new ProgramMap<GLProgram>( _id ) ;
				return program ;
			}

			private ProgramMap<GLProgram> cast( final Program _program )
			{
				assert( _program != null ) ;
				assert( !( _program instanceof ProgramMap ) ) ;
				return ( ProgramMap<GLProgram> )_program ;
			}
		} ;
	}

	public WorldAssist.Assist createWorldAssist()
	{
		return new WorldAssist.Assist()
		{
			@Override
			public World getDefaultWorld()
			{
				return worlds.getWorld( ( GLWorld )null ) ;
			}

			@Override
			public World getWorld( final String _id )
			{
				return worlds.getWorld( _id ) ;
			}

			@Override
			public World addWorld( final World _world )
			{
				final GLWorld world = cast( _world ) ;
				worlds.addWorld( world ) ;
				return _world ;
			}

			@Override
			public World removeWorld( final World _world )
			{
				final GLWorld world = cast( _world ) ;
				worlds.removeWorld( world ) ;
				return _world ;
			}

			@Override
			public void destroyWorld( final World _world )
			{
				final GLWorld world = cast( _world ) ;
			}

			@Override
			public World setRenderDimensions( final World _world, final int _x, final int _y, final int _width, final int _height )
			{
				GLRenderer.this.invokeLater( new Runnable()
				{
					public void run()
					{
						final GLWorld world = cast( _world ) ;
						world.setRenderDimensions( _x, _y, _width, _height ) ;
					}
				} ) ;
				return _world ;
			}

			@Override
			public World setDisplayDimensions( final World _world, final int _x, final int _y, final int _width, final int _height )
			{
				GLRenderer.this.invokeLater( new Runnable()
				{
					public void run()
					{
						final GLWorld world = cast( _world ) ;
						world.setDisplayDimensions( _x, _y, _width, _height ) ;
					}
				} ) ;
				return _world ;
			}

			@Override
			public World constructWorld( final String _id, final int _order )
			{
				final GLWorld world = GLWorld.create( _id, _order ) ;
				worlds.addWorld( world ) ;
				return world ;
			}

			private GLWorld cast( final World _world )
			{
				assert( _world != null ) ;
				assert( !( _world instanceof GLWorld ) ) ;
				return ( GLWorld )_world ;
			}
		} ;
	}

	public CameraAssist.Assist createCameraAssist()
	{
		return new CameraAssist.Assist()
		{
			@Override
			public Camera getDefaultCamera()
			{
				return defaultCamera ;
			}

			@Override
			public Camera addCamera( final Camera _camera, final World _world )
			{
				final GLWorld temp = cast( _world ) ;
				final GLWorld world = worlds.getWorld( temp ) ;
				worlds.addCamera( cast( _camera ), world ) ;
				return _camera ;
			}

			@Override
			public Camera removeCamera( final Camera _camera, final World _world )
			{
				worlds.removeCamera( cast( _camera ) ) ;
				return _camera ;
			}

			@Override
			public Camera createCamera( final String _id,
										final Vector3 _position,
										final Vector3 _rotation,
										final Vector3 _scale )
			{
				return new CameraData( _id, _position, _rotation, _scale ) ;
			}

			private GLWorld cast( final World _world )
			{
				assert( _world != null ) ;
				assert( !( _world instanceof GLWorld ) ) ;
				return ( GLWorld )_world ;
			}

			private CameraData cast( final Camera _camera )
			{
				assert( _camera != null ) ;
				assert( !( _camera instanceof CameraData ) ) ;
				return ( CameraData )_camera ;
			}
		} ;
	}

	public StorageAssist.Assist createStorageAssist()
	{
		return new StorageAssist.Assist()
		{
			@Override
			public Storage create( final String _id, final int _size )
			{
				final GLStorage storage = new GLStorage( _size ) ;
				GLRenderer.this.invokeLater( new Runnable()
				{
					public void run()
					{
						MGL.glGenBuffers( 1, storage.id, 0 ) ;
					}
				} ) ;

				return storage ;
			}

			@Override
			public Storage get( final String _id )
			{
				throw new UnsupportedOperationException() ;
			}

			@Override
			public Storage update( final Storage _storage )
			{
				GLRenderer.this.invokeLater( new Runnable()
				{
					public void run()
					{
						final GLStorage storage = ( GLStorage )_storage ;
						final float[] buffer = storage.getBuffer() ;

						final int lengthInBytes = buffer.length * 4 ;

						final java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocateDirect( lengthInBytes ) ;
						byteBuffer.order( java.nio.ByteOrder.nativeOrder() ) ;
						final java.nio.FloatBuffer floatBuffer = byteBuffer.asFloatBuffer() ;

						floatBuffer.put( buffer ) ;
						floatBuffer.position( 0 ) ;

						MGL.glBindBuffer( MGL.GL_SHADER_STORAGE_BUFFER, storage.id[0] ) ;
						MGL.glBufferData( MGL.GL_SHADER_STORAGE_BUFFER, lengthInBytes, floatBuffer​, MGL.GL_DYNAMIC_DRAW ) ;
					}
				} ) ;

				return _storage ;
			}
			
			@Override
			public int calculateSize( SNode _node )
			{
				switch( _node.getType() )
				{
					default      : throw new UnsupportedOperationException() ;
					case STRUCT  : return calculateSStruct( ( SStruct )_node ) ;
					case ARRAY   : return calculateArray( ( SArray )_node ) ;
					case BOOL    : return 4 ;
					case FLOAT   : return 4 ;
					case INTEGER : return 4 ;
				}
			}
			
			@Override
			public int calculateOffset( SNode _node )
			{
				int offset = 0 ;
				SNode node = _node.getParent() ;
				switch( _node.getType() )
				{
					default      : throw new UnsupportedOperationException() ;
					case STRUCT  : offset += calculateSStructOffset( ( SStruct )node, _node ) ; break ;
					case ARRAY   : offset += calculateArray( ( SArray )node ) ; break ;
					case BOOL    : offset +=  4 ; break ;
					case FLOAT   : offset +=  4 ; break ;
					case INTEGER : offset +=  4 ; break ;
				}

				return offset ;
			}

			private int calculateSStructOffset( SStruct _parent, SNode _child )
			{
				int size = 0 ;
				for( final Tuple<String, SNode> child : _parent.getChildren() )
				{
					if( child.getRight() == _child )
					{
						return size ;
					}

					size += calculateSize( child.getRight() ) ;
				}

				return size ;
			}

			private int calculateArray( SArray _array )
			{
				return _array.getLength() * calculateSize( _array.getSupportedType() ) ;
			}

			private int calculateSStruct( SStruct _struct )
			{
				int size = 0 ;
				for( final Tuple<String, SNode> child : _struct.getChildren() )
				{
					size += calculateSize( child.getRight() ) ;
				}
				return size ;
			}
		} ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		updateCameraAndWorldDisplay( _width, _height ) ;
		canvas.setSize( _width, _height ) ;
	}

	private static void updateCameraAndWorldDisplay( final int _width, final int _height )
	{
		final Camera camera = CameraAssist.getDefaultCamera() ;
		camera.setDisplayResolution( _width, _height ) ;

		final World world = WorldAssist.getDefaultWorld() ;
		WorldAssist.setDisplayDimensions( world, 0, 0, _width, _height ) ;
	}
	
	@Override
	public void init( final GLAutoDrawable _drawable )
	{
		System.out.println( "GL3 Contex initialised.." ) ;
		MGL.setGL( _drawable.getGL().getGL3() ) ;

		//System.out.println( "Vsync: " + GlobalConfig.getInteger( "VSYNC", 0 ) ) ;
		MGL.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled

		MGL.glEnable( MGL.GL_PRIMITIVE_RESTART ) ;		//GLRenderer.handleError( "Enable Primitive Restart", _gl ) ;
		MGL.glPrimitiveRestartIndex( GLGeometryUploader.PRIMITIVE_RESTART_INDEX ) ;

		MGL.glEnable( MGL.GL_BLEND ) ;										//GLRenderer.handleError( "Enable Blend", _gl ) ;
		MGL.glBlendFunc( MGL.GL_SRC_ALPHA, MGL.GL_ONE_MINUS_SRC_ALPHA ) ;	//GLRenderer.handleError( "Set Blend Func", _gl ) ;

		MGL.glEnable( MGL.GL_CULL_FACE ) ;
		MGL.glCullFace( MGL.GL_BACK ) ;  
		MGL.glFrontFace( MGL.GL_CCW ) ;

		System.out.println( "Building default shaders.." ) ;
		programs.load( "SIMPLE_TEXTURE",  "base/shaders/desktop/simple_texture.jgl" ) ;
		programs.load( "SIMPLE_FONT",     "base/shaders/desktop/simple_font.jgl" ) ;
		programs.load( "SIMPLE_GEOMETRY", "base/shaders/desktop/simple_geometry.jgl" ) ;
		programs.load( "SIMPLE_STENCIL",  "base/shaders/desktop/simple_stencil.jgl" ) ;
		programs.load( "SIMPLE_STORAGE_TEXTURE",  "base/shaders/desktop/simple_storage_texture.jgl" ) ;

		{
			// Query for the Max Texture Size and store the results.
			// I doubt the size will change during the running of the engine.
			final int[] size = new int[1] ;
			MGL.glGetIntegerv( MGL.GL_MAX_TEXTURE_SIZE, size, 0 ) ;
			maxTextureSize.setXY( size[0], size[0] ) ;
		}
	}

	public void setViewMode( final int _mode )
	{
		viewMode = _mode ;
	}

	@Override
	public void reshape( final GLAutoDrawable _drawable, final int _x, final int _y, final int _width, final int _height )
	{
		updateCameraAndWorldDisplay( _width, _height ) ;
		if( GlobalConfig.getBoolean( "DISPLAYRENDERPARITY", false ) == true )
		{
			// Update the render dimensions if the window size 
			// and render size are meant to be identical.
			// Some users will not want parity, using a larger window 
			// size but rendering to a smaller size and subsequently being upscaled.
			final Camera camera = CameraAssist.getDefaultCamera() ;
			camera.setScreenResolution( _width, _height ) ;
			camera.setOrthographic( 0.0f, _height, 0.0f, _width, -1000.0f, 1000.0f ) ;

			final World world = WorldAssist.getDefaultWorld() ;
			WorldAssist.setRenderDimensions( world, 0, 0, _width, _height ) ;
		}
	}

	@Override
	public void dispose( final GLAutoDrawable _drawable ) {}

	public void draw( final float _dt )
	{
		super.draw( _dt ) ;
		if( canvas.getExclusiveContextThread() == null )
		{
			canvas.setExclusiveContextThread( Thread.currentThread() ) ;
		}
		canvas.display() ;
	}

	@Override
	public void display( final GLAutoDrawable _drawable )
	{
		updateExecutions() ;
		getEventController().update() ;

		final float updateDelta = getUpdateDeltaTime() ;
		final float frameDelta = getFrameDeltaTime() ;
		final int frameNo = getFrameIteration() ;

		// Expected number of render frames before the next update is triggered.
		final int difference = ( int )( updateDelta / frameDelta ) ;

		//System.out.println( "Update Delta: " + updateDelta + " Frame Delta: " + frameDelta + " Frame No: " + frameNo + " Diff: " + difference ) ;
		worlds.upload( difference, frameNo ) ;
		canvas.swapBuffers() ;
	}

	@Override
	public void sort()
	{
		worlds.sort() ;
	}

	@Override
	public void clear()
	{
		worlds.clear() ;
	}

	/**
		Remove resources that are not being used.
		Does not remove resources that are still 
		flagged for use.
	*/
	@Override
	public void clean()
	{
		final Set<String> activeKeys = new HashSet<String>() ;
		worlds.clean( activeKeys ) ;

		programs.clean( activeKeys ) ;
		textures.clean( activeKeys ) ;
		fontManager.clean( activeKeys ) ;
	}

	public GLWindow getCanvas()
	{
		return canvas ;
	}

	/**
		We need to make sure the canvas is constructed 
		as soon as possible.
		We set the canvas size to renderInfo display dimensions, 
		as reshape() is called when GLRenderer is added as 
		a listener to the canvas.
		reshape() updates renderInfo and wipes the dimension 
		settings loaded in by 'base/config.txt' from DesktopStarter.
	*/
	private void initWindow()
	{
		if( canvas == null )
		{
			final GLProfile glProfile = GLProfile.get( GLProfile.GL3 ) ;
			final GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
			capabilities.setStencilBits( 1 ) ;			// Provide ON/OFF Stencil Buffers
			capabilities.setDoubleBuffered( GlobalConfig.getBoolean( "DOUBLE_BUFFER", true ) ) ;

			canvas = GLWindow.create( capabilities ) ;

			//final Vector2 display = defaultCamera.getDisplayScreen().dimension ;
			//canvas.setSize( ( int )display.x, ( int )display.y ) ;

			// We want to be in complete control of any swapBuffer calls
			canvas.setAutoSwapBufferMode( false ) ;
		}
	}

	/**
		Construct an initial world that will be used 
		if the game developer does not specify a world to 
		place Draw objects into.

		Ensure initAssist() is called beforehand.
		This should be called on start() and only once.
	*/
	private void initDefaultWorld()
	{
		final GLWorld world = GLWorld.createCore( "DEFAULT", 0 ) ;

		worlds.addWorld( world ) ;
		worlds.setDefault( world ) ;

		worlds.addCamera( defaultCamera, null ) ;
	}

	/**
		Attempt to acquire a compatible GLProgram from 
		the ProgramManager. Make sure the GLProgram 
		requested maps correctly with the ProgramMap 
		defined in the GLDraw object.
	*/
	protected static boolean loadProgram( final GLDraw _data )
	{
		final BasicDraw<GLProgram> basic = _data.getBasicData() ;
		final ProgramMap<GLProgram> program = basic.getProgram() ;
		if( program == null )
		{
			// If we don't have a program then there is no point progressing further.
			return false ;
		}

		if( program.getProgram() == null )
		{
			final GLProgram glProgram = programs.get( program.getID() ) ;
			if( glProgram == null )
			{
				// If the GLProgram is yet to exist then 
				// _data will need to be run through the
				// rendering cycle again.
				basic.forceUpdate() ;
				return false ;
			}

			if( glProgram.isValidMap( program.getUniformMap() ) == false )
			{
				// If a GLProgram exists but the mappings are 
				// incompatible return false and prevent _data 
				// from being rendered.
				return false ;
			}

			program.setProgram( glProgram ) ;
		}

		return true ;
	}

	protected static GLImage getTexture( final String _path )
	{
		return textures.get( _path ) ;
	}

	protected static GLFont getFont( final MalletFont _font )
	{
		return fontManager.get( _font ) ;
	}

	/**
		Will grab the GLContext and call makeCurrent() 
		if it exists in the first place.
	*/
	private boolean makeCurrent()
	{
		final GLContext context = canvas.getContext() ;
		if( context == null )
		{
			return false ;
		}

		canvas.setExclusiveContextThread( null ) ;
		context.makeCurrent() ;
		return true ;
	}

	/**
		Release the GLContext from the current thread.
		This should only be called if makeCurrent()
		returns true. 
	*/
	private void release()
	{
		final GLContext context = canvas.getContext() ;
		context.release() ;
	}

	public static void handleError( final String _txt )
	{
		int error = 0 ;
		while( ( error = MGL.glGetError() ) != MGL.GL_NO_ERROR )
		{
			switch( error )
			{
				case MGL.GL_NO_ERROR                      : break ;
				case MGL.GL_INVALID_ENUM                  : System.out.println( _txt + ": GL_INVALID_ENUM" ) ; break ;
				case MGL.GL_INVALID_VALUE                 : System.out.println( _txt + ": GL_INVALID_VALUE" ) ; break ;
				case MGL.GL_INVALID_OPERATION             : System.out.println( _txt + ": GL_INVALID_OPERATION" ) ; break ;
				case MGL.GL_INVALID_FRAMEBUFFER_OPERATION : System.out.println( _txt + ": GL_INVALID_FRAMEBUFFER_OPERATION" ) ; break ;
				case MGL.GL_OUT_OF_MEMORY                 : System.out.println( _txt + ": GL_OUT_OF_MEMORY" ) ; break ;
				case MGL.GL_STACK_UNDERFLOW               : System.out.println( _txt + ": GL_STACK_UNDERFLOW" ) ; break ;
				case MGL.GL_STACK_OVERFLOW                : System.out.println( _txt + ": GL_STACK_OVERFLOW" ) ; break ;
				default                                   : System.out.println( _txt + ": Unknown Error." ) ; break ;
			}
		}
	}
}
