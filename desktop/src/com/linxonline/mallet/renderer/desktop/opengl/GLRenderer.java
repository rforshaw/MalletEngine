package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;
import java.util.ArrayList ;
import java.text.MessageFormat ;

import com.jogamp.newt.opengl.GLWindow ;
import com.jogamp.opengl.GLAutoDrawable ;
import com.jogamp.opengl.GLProfile ;
import com.jogamp.opengl.GLCapabilities ;
import com.jogamp.opengl.GLContext ;
import com.jogamp.opengl.GLEventListener ;
import com.jogamp.opengl.GLDebugMessage ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification.Notify ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.opengl.JSONProgram ;

public final class GLRenderer extends BasicRenderer implements GLEventListener
{
	public final static int ORTHOGRAPHIC_MODE = 1 ;
	public final static int PERSPECTIVE_MODE  = 2 ;

	private final static int UPDATE_OPERATION = 0 ;		// Used with an invokeLater() call.

	private final ProgramManager<GLProgram> programs = new ProgramManager<GLProgram>( new ProgramManager.JSONBuilder()
	{
		@Override
		public void build( final JSONProgram _program )
		{
			GLRenderer.this.invokeLater( () ->
			{
				final GLProgram program = GLProgram.build( _program ) ;
				final String id = program.getName() ;
				if( programs.isKeyNull( id ) == false )
				{
					Logger.println( String.format( "Attempting to override existing resource: %s", id ), Logger.Verbosity.MAJOR ) ;
				}

				Logger.println( String.format( "Program: %s has been compiled", id ), Logger.Verbosity.NORMAL ) ;
				programs.put( id, program ) ;
			} ) ;
		}
	} ) ;

	private final static GLProfile glProfile = GLProfile.get( GLProfile.GL3 ) ;

	private final static GLTextureManager textures = new GLTextureManager() ;
	private final static GLFontManager fontManager = new GLFontManager( glProfile, textures ) ;

	private final static Vector2 maxTextureSize = new Vector2() ;						// Maximum Texture resolution supported by the GPU.

	private final AssetLookup<World, GLWorld> worldLookup = new AssetLookup<World, GLWorld>( "WORLD" ) ;
	private final AssetLookup<Camera, GLCamera> cameraLookup = new AssetLookup<Camera, GLCamera>( "CAMERA" ) ;
	private final AssetLookup<ABuffer, GLBuffer> bufferLookup = new AssetLookup<ABuffer, GLBuffer>( "BUFFER" ) ;
	private final AssetLookup<Program, GLProgram> programLookup = new AssetLookup<Program, GLProgram>( "PROGRAM" ) ;
	private final AssetLookup<Storage, GLStorage> storageLookup = new AssetLookup<Storage, GLStorage>( "STORAGE" ) ;

	private static List<Camera> cameras = new ArrayList<Camera>() ;
	private static List<GLWorld> worlds = new ArrayList<GLWorld>() ;

	private final List<ABuffer> buffersToUpdate = new ArrayList<ABuffer>() ;
	private final List<IUpdater> updaters = new ArrayList<IUpdater>() ;

	private final Thread mainThread ;
	private GLWindow canvas ;

	public GLRenderer( final Thread _main )
	{
		super() ;
		mainThread = _main ;

		// As soon as the renderer is constructed,
		// we'll set up our basic shader-programs.
		// They won't be compiled until the first
		// draw() is made.
		GLRenderer.this.invokeLater( () ->
		{
			initDefaultWorld() ;

			Logger.println( "Building default shaders..", Logger.Verbosity.NORMAL ) ;
			programs.load( "SIMPLE_TEXTURE",       "base/shaders/desktop/simple_texture.jgl" ) ;
			programs.load( "SIMPLE_ARRAY_TEXTURE", "base/shaders/desktop/simple_array_texture.jgl" ) ;
			programs.load( "SIMPLE_FONT",          "base/shaders/desktop/simple_font.jgl" ) ;
			programs.load( "SIMPLE_GEOMETRY",      "base/shaders/desktop/simple_geometry.jgl" ) ;
			programs.load( "SIMPLE_STENCIL",       "base/shaders/desktop/simple_stencil.jgl" ) ;

			programs.load( "SIMPLE_INSTANCE_TEXTURE",  "base/shaders/desktop/simple_instance_texture.jgl" ) ;
		} ) ;
	}

	@Override
	public void start()
	{
		Logger.println( "Starting renderer..", Logger.Verbosity.NORMAL ) ;
		super.start() ;

		canvas = createWindow( mainThread ) ;

		RenderAssist.setAssist( createRenderAssist() ) ;

		FontAssist.setAssist( createFontAssist() ) ;
		TextureAssist.setAssist( createTextureAssist() ) ;

		DrawAssist.setAssist( createDrawAssist() ) ;
		ProgramAssist.setAssist( createProgramAssist() ) ;
		StorageAssist.setAssist( createStorageAssist() ) ;

		WorldAssist.setAssist( createWorldAssist() ) ;
		CameraAssist.setAssist( createCameraAssist() ) ;

		canvas.addGLEventListener( this ) ;
		canvas.setVisible( true ) ;
	}

	private GLWindow createWindow( final Thread _main )
	{
		final GLCapabilities capabilities = new GLCapabilities( glProfile ) ;

		final int samples = GlobalConfig.getInteger( "NUM_SAMPLES", 2 ) ;
		capabilities.setNumSamples( samples ) ;
		capabilities.setSampleBuffers( samples > 0 ) ;

		capabilities.setStencilBits( 1 ) ;			// Provide ON/OFF Stencil Buffers
		capabilities.setDoubleBuffered( GlobalConfig.getBoolean( "DOUBLE_BUFFER", true ) ) ;

		final GLWindow win = GLWindow.create( capabilities ) ;
		win.setExclusiveContextThread( _main ) ;

		// We want to be in complete control of any swapBuffer calls
		win.setAutoSwapBufferMode( false ) ;
		win.setResizable( true ) ;
		return win ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting down renderer..", Logger.Verbosity.NORMAL ) ;
		for( final GLWorld world : worlds )
		{
			world.shutdown() ;
		}

		programs.shutdown() ;
		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.shutdown() ;

		canvas.destroy() ;
	}

	public RenderAssist.Assist createRenderAssist()
	{
		return new RenderAssist.Assist()
		{
			@Override
			public void setDisplayDimensions( final int _width, final int _height )
			{
				updateCameraAndWorldDisplay( _width, _height ) ;
				canvas.setSurfaceSize( _width, _height ) ;
			}

			@Override
			public void setFullscreen( final boolean _set )
			{
				if( _set != canvas.isFullscreen() )
				{
					canvas.setFullscreen( _set ) ;
				}
			}
		} ;
	}

	public FontAssist.Assist createFontAssist()
	{
		return new FontAssist.Assist()
		{
			@Override
			public Font.Metrics createMetrics( final Font _font )
			{
				return fontManager.generateMetrics( _font ) ;
			}

			@Override
			public Glyph createGlyph( final Font _font, final int _code )
			{
				return fontManager.generateGlyph( _font, _code ) ;
			}

			@Override
			public String[] loadFont( final String _path )
			{
				return fontManager.loadFont( _path ) ;
			}
		} ;
	}

	public TextureAssist.Assist createTextureAssist()
	{
		return new TextureAssist.Assist()
		{
			@Override
			public Texture.Meta createMeta( final String _path )
			{
				return textures.getMeta( _path ) ;
			}

			@Override
			public Vector2 getMaximumTextureSize()
			{
				return new Vector2( maxTextureSize ) ;
			}

			@Override
			public boolean texturesLoaded()
			{
				return textures.texturesLoaded() ;
			}

			@Override
			public Set<String> getLoadedTextures( final Set<String> _fill )
			{
				return textures.getLoadedKeys( _fill ) ;
			}

			@Override
			public Set<String> getAllTextures( final Set<String> _fill )
			{
				return textures.getAllKeys( _fill ) ;
			}

			@Override
			public void clean( final Set<String> _active )
			{
				GLRenderer.this.invokeLater( () ->
				{
					textures.clean( _active ) ;

					// Once we've cleared the textures we need to
					// refresh the drawbuffers.
					// This ensures any textures that were removed but
					// should not have been removed are pulled back in.
					final int size = bufferLookup.size() ;
					for( int i = 0; i < size; ++i )
					{
						final ABuffer buffer = bufferLookup.getLHS( i ) ;
						if( updateBuffer( buffer ) == false )
						{
							DrawAssist.update( buffer ) ;
						}
					}
				} ) ;
			}
		} ;
	}

	public DrawAssist.Assist createDrawAssist()
	{
		return new DrawAssist.Assist()
		{
			@Override
			public DrawUpdater add( final DrawUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.add( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public DrawUpdater remove( final DrawUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.remove( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public DrawInstancedUpdater add( final DrawInstancedUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.add( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public DrawInstancedUpdater remove( final DrawInstancedUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.remove( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public TextUpdater add( final TextUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.add( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public TextUpdater remove( final TextUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.remove( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public <T extends ABuffer> T add( final T _buffer )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final GLBuffer buff = create( _buffer ) ;
					bufferLookup.map( _buffer.index(), _buffer, buff ) ;
					if( updateBuffer( _buffer ) == false )
					{
						DrawAssist.update( _buffer ) ;
					}
				} ) ;
				return _buffer ;
			}

			@Override
			public <T extends ABuffer> T remove( final T _buffer )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final Tuple<ABuffer, GLBuffer> tuple = bufferLookup.unmap( _buffer.index() ) ;
					if( tuple != null )
					{
						final GLBuffer buff = tuple.getRight() ;
						buff.shutdown() ;
					}
				} ) ;
				return _buffer ;
			}

			@Override
			public <T extends ABuffer> T update( final T _buffer )
			{
				GLRenderer.this.invokeLater( _buffer, UPDATE_OPERATION, () ->
				{
					if( updateBuffer( _buffer ) == false )
					{
						DrawAssist.update( _buffer ) ;
					}
				} ) ;
				return _buffer ;
			}

			private GLBuffer create( final ABuffer _buffer )
			{
				return switch( _buffer )
				{
					case GeometryBuffer b      -> new GLGeometryBuffer( b ) ;
					case TextBuffer b          -> new GLTextBuffer( b ) ;
					case DrawInstancedBuffer b -> new GLDrawInstancedBuffer( ( DrawInstancedBuffer )_buffer ) ;
					case DrawBuffer b          -> new GLDrawBuffer( b ) ;
					case Stencil b             -> new GLStencil( b ) ;
					case Depth b               -> new GLDepth( b ) ;
					case GroupBuffer b         -> new GLGroupBuffer( b ) ;
					default -> null ;//throw new Exception( "Unknown buffer type specified." ) ;
				} ;
			}
		} ;
	}

	public ProgramAssist.Assist createProgramAssist()
	{
		return new ProgramAssist.Assist()
		{
			@Override
			public void load( final String _id, final String _path )
			{
				GLRenderer.this.invokeLater( () ->
				{
					programs.load( _id, MessageFormat.format( _path, "desktop" ) ) ;
				} ) ;
			}

			@Override
			public Program add( final Program _program )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final GLProgram glProgram = programs.get( _program.getID() ) ;
					if( glProgram == null )
					{
						add( _program ) ;
						return ;
					}
					programLookup.map( _program.index(), _program, glProgram ) ;
				} ) ;
				return _program ;
			}

			@Override
			public Program remove( final Program _program ) 
			{
				GLRenderer.this.invokeLater( () ->
				{
					final GLProgram glProgram = programs.get( _program.getID() ) ;
					if( glProgram != null )
					{
						programLookup.unmap( _program.index() ) ;
					}
				} ) ;
				return _program ;
			}
		} ;
	}

	public WorldAssist.Assist createWorldAssist()
	{
		return new WorldAssist.Assist()
		{
			@Override
			public World getDefault()
			{
				return GLRenderer.this.getDefaultWorld() ;
			}

			@Override
			public World add( final World _world )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final GLWorld world = new GLWorld( _world, cameraLookup, bufferLookup ) ;
					worldLookup.map( _world.index(), _world, world ) ;

					final int size = worlds.size() ;
					for( int i = 0; i < size; ++i )
					{
						final GLWorld w = worlds.get( i ) ; 
						if( _world.getOrder() <= w.getOrder() )
						{
							worlds.add( i, world ) ;
							return ;
						}
					}

					worlds.add( world ) ;
				} ) ;

				return _world ;
			}

			@Override
			public World remove( final World _world )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final Tuple<World, GLWorld> tuple = worldLookup.unmap( _world.index() ) ;
					if( tuple != null )
					{
						final GLWorld world = tuple.getRight() ;
						worlds.remove( world ) ;
						world.shutdown() ;
					}
				} ) ;
				return _world ;
			}

			@Override
			public World update( final World _world )
			{
				GLRenderer.this.invokeLater( _world, UPDATE_OPERATION, () ->
				{
					final GLWorld world = get( _world ) ;
					if( world != null )
					{
						world.update( _world, cameraLookup, bufferLookup ) ;
					}
				} ) ;
				return _world ;
			}

			private GLWorld get( final World _world )
			{
				return worldLookup.getRHS( _world.index() ) ;
			}
		} ;
	}

	public CameraAssist.Assist createCameraAssist()
	{
		return new CameraAssist.Assist()
		{
			@Override
			public Camera getDefault()
			{
				return GLRenderer.this.getDefaultCamera() ;
			}

			@Override
			public Camera add( final Camera _camera )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final GLCamera camera = new GLCamera( _camera ) ;
					cameraLookup.map( _camera.index(), _camera, camera ) ;
					cameras.add( _camera ) ;
				} ) ;
				return _camera ;
			}

			@Override
			public Camera remove( final Camera _camera )
			{
				GLRenderer.this.invokeLater( () ->
				{
					cameras.remove( _camera ) ;
					cameraLookup.unmap( _camera.index() ) ;
				} ) ;
				return _camera ;
			}

			private GLCamera get( final Camera _camera )
			{
				return cameraLookup.getRHS( _camera.index() ) ;
			}
		} ;
	}

	public StorageAssist.Assist createStorageAssist()
	{
		return new StorageAssist.Assist()
		{
			@Override
			public StorageUpdater add( final StorageUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.add( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public StorageUpdater remove( final StorageUpdater _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					updaters.remove( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public Storage add( Storage _storage )
			{
				GLRenderer.this.invokeLater( () ->
				{
					final GLStorage storage = new GLStorage( _storage ) ;
					storageLookup.map( _storage.index(), _storage, storage ) ;
				} ) ;

				return _storage ;
			}

			@Override
			public Storage update( final Storage _storage )
			{
				GLRenderer.this.invokeLater( _storage, UPDATE_OPERATION, () ->
				{
					final GLStorage storage = get( _storage ) ;
					if( storage != null )
					{
						storage.update( _storage ) ;
					}
				} ) ;

				return _storage ;
			}

			private GLStorage get( final Storage _storage )
			{
				return storageLookup.getRHS( _storage.index() ) ;
			}
		} ;
	}

	private void updateCameraAndWorldDisplay( final int _width, final int _height )
	{
		final Camera camera = getDefaultCamera() ;
		camera.setDisplayResolution( _width, _height ) ;
		camera.setScreenResolution( _width, _height ) ;

		final World world = getDefaultWorld() ;
		world.setRenderDimensions( 0, 0, _width, _height ) ;
		WorldAssist.update( world ) ;
	}

	@Override
	public void init( final GLAutoDrawable _drawable )
	{
		Logger.println( "Initialise render context..", Logger.Verbosity.NORMAL ) ;
		MGL.setGL( _drawable.getGL().getGL4() ) ;

		//System.out.println( "Vsync: " + GlobalConfig.getInteger( "VSYNC", 0 ) ) ;
		MGL.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled

		MGL.glEnable( MGL.GL_PRIMITIVE_RESTART ) ;
		MGL.glPrimitiveRestartIndex( GLBuffer.PRIMITIVE_RESTART_INDEX ) ;

		MGL.glEnable( MGL.GL_BLEND ) ;
		MGL.glBlendFunc( MGL.GL_SRC_ALPHA, MGL.GL_ONE_MINUS_SRC_ALPHA ) ;

		MGL.glEnable( MGL.GL_CULL_FACE ) ;
		MGL.glCullFace( MGL.GL_BACK ) ;  
		MGL.glFrontFace( MGL.GL_CCW ) ;

		MGL.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		{
			// Query for the Max Texture Size and store the results.
			// I doubt the size will change during the running of the engine.
			final int[] size = new int[1] ;
			MGL.glGetIntegerv( MGL.GL_MAX_TEXTURE_SIZE, size, 0 ) ;
			maxTextureSize.setXY( size[0], size[0] ) ;
		}
	}

	@Override
	public void reshape( final GLAutoDrawable _drawable, final int _x, final int _y, final int _width, final int _height )
	{
		updateCameraAndWorldDisplay( _width, _height ) ;
		if( GlobalConfig.getBoolean( "DISPLAYRENDERPARITY", false ) == true )
		{
			// My default the main framebuffer is the same dimensions 
			// as the window, the camera projection does not have to align 
			// with this requirement unless this flag is set to true.
			final Camera camera = getDefaultCamera() ;
			camera.setOrthographic( Camera.Mode.HUD, 0, _height, 0, _width, -1000.0f, 1000.0f ) ;
		}
	}

	@Override
	public void dispose( final GLAutoDrawable _drawable )
	{
		Logger.println( "Render context lost..", Logger.Verbosity.MAJOR ) ;
	}

	@Override
	public void draw( final float _dt )
	{
		super.draw( _dt ) ;
		canvas.display() ;
	}

	@Override
	public void display( final GLAutoDrawable _drawable )
	{
		final float updateDelta = getUpdateDeltaTime() ;
		final float frameDelta = getFrameDeltaTime() ;

		// Expected number of render frames before the next update is triggered.
		final int difference = ( int )( updateDelta / frameDelta ) ;
		final float coefficient = 1.0f / difference ;

		// We limit the number of textures that can be bound
		// each draw call - ensures things still feel responsive.
		textures.resetBindCount() ;

		final int cameraSize = cameras.size() ;
		for( int i = 0; i < cameraSize; ++i )
		{
			final Camera camera = cameras.get( i ) ;
			camera.update( coefficient ) ;

			final GLCamera glCamera = cameraLookup.getRHS( camera.index() ) ;
			glCamera.update( camera ) ;
		}

		final int buffers = updateBuffers( coefficient ) ;
		//System.out.println( "Buffers: " + buffers ) ;

		updateExecutions() ;

		final int worldSize = worlds.size() ;
		for( int i = 0; i < worldSize; ++i )
		{
			final GLWorld world = worlds.get( i ) ;
			world.draw() ;
		}

		canvas.swapBuffers() ;
	}

	public GLWindow getCanvas()
	{
		return canvas ;
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
		final Camera defaultCamera = getDefaultCamera() ;
		cameras.add( defaultCamera ) ;

		final GLCamera camera = new GLCamera( defaultCamera ) ;
		cameraLookup.map( defaultCamera.index(), defaultCamera, camera ) ;

		final World defaultWorld = getDefaultWorld() ;
		final GLWorld world = GLWorld.createCore( defaultWorld, cameraLookup, bufferLookup ) ;
		worldLookup.map( defaultWorld.index(), defaultWorld, world ) ;

		worlds.add( world ) ;
	}

	protected static GLImage getTexture( final Texture _texture )
	{
		final Texture.Meta meta = _texture.getMeta() ;
		final String path = meta.getPath() ;

		if( meta.getAttachmentIndex() < 0 )
		{
			return textures.get( path ) ;
		}

		final int worldSize = worlds.size() ;
		for( int i = 0; i < worldSize; ++i )
		{
			final GLWorld world = worlds.get( i ) ;
			if( path.equals( world.getID() ) == true )
			{
				return world.getImage( meta.getAttachmentIndex() ) ;
			}
		}

		return null ;
	}

	protected static GLImage getTextureArray( final TextureArray _texture )
	{
		return textures.getByTextureArray( _texture ) ;
	}

	protected static GLFont getFont( final Font _font )
	{
		return fontManager.get( _font ) ;
	}

	private int updateBuffers( final float _coefficient )
	{
		int totalBufferUpdates = 0 ;

		final int dSize = updaters.size() ;
		for( int i = 0; i < dSize; ++i )
		{
			final IUpdater updater = updaters.get( i ) ;
			updater.update( buffersToUpdate, _coefficient ) ;
			if( buffersToUpdate.isEmpty() == false )
			{
				final int bSize = buffersToUpdate.size() ;
				totalBufferUpdates += bSize ;
				for( int j = 0; j < bSize; ++j )
				{
					final ABuffer buffer = buffersToUpdate.get( j ) ;
					if( updateBuffer( buffer ) == false )
					{
						updater.forceUpdate() ;
					}
				}
				buffersToUpdate.clear() ;
			}
		}

		return totalBufferUpdates ;
	}

	private <T extends ABuffer> boolean updateBuffer( final T _buffer )
	{
		return switch( _buffer )
		{
			case GeometryBuffer buffer ->
			{
				final GLGeometryBuffer buf = ( GLGeometryBuffer )bufferLookup.getRHS( buffer.index() ) ;
				yield ( buf != null ) ? buf.update( buffer ) : true ;
			}
			case TextBuffer buffer ->
			{
				final GLTextBuffer buf = ( GLTextBuffer )bufferLookup.getRHS( buffer.index() ) ;
				yield ( buf != null ) ? buf.update( buffer, programLookup, storageLookup ) : true ;
			}
			case DrawInstancedBuffer buffer ->
			{
				final GLDrawInstancedBuffer buf = ( GLDrawInstancedBuffer )bufferLookup.getRHS( buffer.index() ) ;
				yield ( buf != null ) ? buf.update( buffer, programLookup, bufferLookup, storageLookup ) : true ;
			}
			case DrawBuffer buffer ->
			{
				final GLDrawBuffer buf = ( GLDrawBuffer )bufferLookup.getRHS( buffer.index() ) ;
				yield ( buf != null ) ? buf.update( buffer, programLookup, bufferLookup, storageLookup ) : true ;
			}
			case Stencil buffer ->
			{
				final GLStencil stencil = ( GLStencil )bufferLookup.getRHS( buffer.index() ) ;
				yield ( stencil != null ) ? stencil.update( buffer ) : true ;
			}
			case Depth buffer ->
			{
				final GLDepth depth = ( GLDepth )bufferLookup.getRHS( buffer.index() ) ;
				yield ( depth != null ) ? depth.update( buffer ) : true ;
			}
			case Storage buffer ->
			{
				final GLStorage storage = storageLookup.getRHS( buffer.index() ) ;
				yield ( storage != null ) ? storage.update( buffer ) : true ;
			}
			default -> true ;//throw new Exception( "Unknown buffer type specified." ) ;
		} ;
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
