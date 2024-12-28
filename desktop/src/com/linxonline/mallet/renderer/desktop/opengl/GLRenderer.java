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

	private final static GLTextureManager textures = new GLTextureManager() ;
	private final static GLFontManager fontManager = new GLFontManager( textures ) ;

	private final static Vector2 maxTextureSize = new Vector2() ;						// Maximum Texture resolution supported by the GPU.

	private GLWindow canvas ;

	private int viewMode = ORTHOGRAPHIC_MODE ;

	private final AssetLookup<World, GLWorld> worldLookup = new AssetLookup<World, GLWorld>( "WORLD" ) ;
	private final AssetLookup<Camera, GLCamera> cameraLookup = new AssetLookup<Camera, GLCamera>( "CAMERA" ) ;
	private final AssetLookup<ABuffer, GLBuffer> bufferLookup = new AssetLookup<ABuffer, GLBuffer>( "BUFFER" ) ;
	private final AssetLookup<Program, GLProgram> programLookup = new AssetLookup<Program, GLProgram>( "PROGRAM" ) ;
	private final AssetLookup<Storage, GLStorage> storageLookup = new AssetLookup<Storage, GLStorage>( "STORAGE" ) ;

	private static List<Camera> cameras = new ArrayList<Camera>() ;
	private static List<GLWorld> worlds = new ArrayList<GLWorld>() ;

	private final List<ABuffer> buffersToUpdate = new ArrayList<ABuffer>() ;
	private final List<IUpdater<? extends ABuffer>> drawUpdaters = new ArrayList<IUpdater<? extends ABuffer>>() ;
	private final List<IUpdater<Storage>> storageUpdaters = new ArrayList<IUpdater<Storage>>() ;

	public GLRenderer()
	{
		super() ;
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
		canvas.setVisible( true ) ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		if( makeCurrent() == true )
		{
			clear() ;							// Clear the contents being rendered

			for( final GLWorld world : worlds )
			{
				world.shutdown() ;
			}

			programs.shutdown() ;
			textures.shutdown() ;				// We'll loose all texture and font resources
			fontManager.shutdown() ;
			release() ;
		}
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
						final GLBuffer buff = bufferLookup.getRHS( i ) ;
						if( buff != null )
						{
							final ABuffer buffer = bufferLookup.getLHS( i ) ;
							if( updateBuffer( buffer, buff ) == false )
							{
								DrawAssist.update( buffer ) ;
							}
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
			public <T extends IUpdater<? extends ABuffer>> T add( final T _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					drawUpdaters.add( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public <T extends IUpdater<? extends ABuffer>> T remove( final T _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					drawUpdaters.remove( _updater ) ;
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
					if( updateBuffer( _buffer, buff ) == false )
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
					final GLBuffer buff = bufferLookup.getRHS( _buffer.index() ) ;
					if( buff != null )
					{
						if( updateBuffer( _buffer, buff ) == false )
						{
							DrawAssist.update( _buffer ) ;
						}
					}
				} ) ;
				return _buffer ;
			}

			private GLBuffer create( final ABuffer _buffer )
			{
				switch( _buffer.getBufferType() )
				{
					default                    : return null ;//throw new Exception( "Unknown buffer type specified." ) ;
					case GEOMETRY_BUFFER       : return new GLGeometryBuffer( ( GeometryBuffer )_buffer ) ;
					case TEXT_BUFFER           : return new GLTextBuffer( ( TextBuffer )_buffer ) ;
					case DRAW_BUFFER           : return new GLDrawBuffer( ( DrawBuffer )_buffer ) ;
					case DRAW_INSTANCED_BUFFER : return new GLDrawInstancedBuffer( ( DrawInstancedBuffer )_buffer ) ;
					case STENCIL               : return new GLStencil( ( Stencil )_buffer ) ;
					case DEPTH                 : return new GLDepth( ( Depth )_buffer ) ;
					case GROUP                 : return new GLGroupBuffer( ( GroupBuffer )_buffer ) ;
				}
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

			@Override
			public Camera update( final Camera _camera ) 
			{
				GLRenderer.this.invokeLater( _camera, UPDATE_OPERATION, () ->
				{
					final GLCamera camera = get( _camera ) ;
					if( camera != null )
					{
						camera.update( _camera ) ;
					}
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
			public <T extends IUpdater<Storage>> T add( final T _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					storageUpdaters.add( _updater ) ;
				} ) ;
				return _updater ;
			}

			@Override
			public <T extends IUpdater<Storage>> T remove( final T _updater )
			{
				GLRenderer.this.invokeLater( () ->
				{
					storageUpdaters.remove( _updater ) ;
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

	public void setFullscreen( final boolean _set )
	{
		GLRenderer.this.invokeLater( () ->
		{
			if( _set != canvas.isFullscreen() )
			{
				canvas.setFullscreen( _set ) ;
			}
		} ) ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		updateCameraAndWorldDisplay( _width, _height ) ;
		canvas.setSurfaceSize( _width, _height ) ;
	}

	private void updateCameraAndWorldDisplay( final int _width, final int _height )
	{
		final Camera camera = getDefaultCamera() ;

		camera.setDisplayResolution( _width, _height ) ;
		camera.setScreenResolution( _width, _height ) ;
		CameraAssist.update( camera ) ;

		final World world = getDefaultWorld() ;
		world.setRenderDimensions( 0, 0, _width, _height ) ;
		WorldAssist.update( world ) ;
	}

	@Override
	public void init( final GLAutoDrawable _drawable )
	{
		System.out.println( "GL3 Contex initialised.." ) ;
		MGL.setGL( _drawable.getGL().getGL3() ) ;

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

		initDefaultWorld() ;

		System.out.println( "Building default shaders.." ) ;
		programs.load( "SIMPLE_TEXTURE",       "base/shaders/desktop/simple_texture.jgl" ) ;
		programs.load( "SIMPLE_ARRAY_TEXTURE", "base/shaders/desktop/simple_array_texture.jgl" ) ;
		programs.load( "SIMPLE_FONT",          "base/shaders/desktop/simple_font.jgl" ) ;
		programs.load( "SIMPLE_GEOMETRY",      "base/shaders/desktop/simple_geometry.jgl" ) ;
		programs.load( "SIMPLE_STENCIL",       "base/shaders/desktop/simple_stencil.jgl" ) ;

		programs.load( "SIMPLE_INSTANCE_TEXTURE",  "base/shaders/desktop/simple_instance_texture.jgl" ) ;

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
			// My default the main framebuffer is the same dimensions 
			// as the window, the camera projection does not have to align 
			// with this requirement unless this flag is set to true.
			final Camera camera = getDefaultCamera() ;
			camera.setOrthographic( Camera.Mode.HUD, 0, _height, 0, _width, -1000.0f, 1000.0f ) ;
			CameraAssist.update( camera ) ;
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
		final float updateDelta = getUpdateDeltaTime() ;
		final float frameDelta = getFrameDeltaTime() ;
		final int frameNo = getFrameIteration() ;

		// Expected number of render frames before the next update is triggered.
		final int difference = ( int )( updateDelta / frameDelta ) ;

		// We limit the number of textures that can be bound
		// each draw call - ensures things still feel responsive.
		textures.resetBindCount() ;

		final int cameraSize = cameras.size() ;
		for( int i = 0; i < cameraSize; ++i )
		{
			final Camera camera = cameras.get( i ) ;
			if( camera.update( difference, frameNo ) == true )
			{
				final GLCamera glCamera = cameraLookup.getRHS( camera.index() ) ;
				glCamera.update( camera ) ;
			}
		}

		int totalBufferUpdates = 0 ;
		final int storageBuffers = updateStorageBuffers( difference, frameNo ) ;
		//System.out.println( "Storage Buffers: " + storageBuffers ) ;
		final int buffers = updateBuffers( difference, frameNo ) ;
		//System.out.println( "Buffers: " + buffers ) ;

		totalBufferUpdates += storageBuffers ;
		totalBufferUpdates += buffers ;

		updateExecutions() ;

		final int worldSize = worlds.size() ;
		for( int i = 0; i < worldSize; ++i )
		{
			final GLWorld world = worlds.get( i ) ;
			world.draw() ;
		}

		canvas.swapBuffers() ;
	}

	@Override
	public void sort() {}

	@Override
	public void clear()
	{
		//worlds.clear() ;
	}

	/**
		Remove resources that are not being used.
		Does not remove resources that are still 
		flagged for use.
	*/
	@Override
	public void clean()
	{
		/*final Set<String> activeKeys = new HashSet<String>() ;
		worlds.clean( activeKeys ) ;

		programs.clean( activeKeys ) ;
		textures.clean( activeKeys ) ;
		fontManager.clean( activeKeys ) ;*/
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
			System.out.println( "Init Window" ) ;
		
			final GLProfile glProfile = GLProfile.get( GLProfile.GL3 ) ;
			final GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
			capabilities.setStencilBits( 1 ) ;			// Provide ON/OFF Stencil Buffers
			capabilities.setDoubleBuffered( GlobalConfig.getBoolean( "DOUBLE_BUFFER", true ) ) ;

			canvas = GLWindow.create( capabilities ) ;

			// We want to be in complete control of any swapBuffer calls
			canvas.setAutoSwapBufferMode( false ) ;
			canvas.setResizable( true ) ;
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

	private int updateStorageBuffers( final int _difference, final int _frameNo )
	{
		int totalBufferUpdates = 0 ;

		final int sSize = storageUpdaters.size() ;
		for( int i = 0; i < sSize; ++i )
		{
			final IUpdater<Storage> updater = storageUpdaters.get( i ) ;
			updater.update( buffersToUpdate, _difference, _frameNo ) ;
			if( buffersToUpdate.isEmpty() == false )
			{
				final int bSize = buffersToUpdate.size() ;
				totalBufferUpdates += bSize ;
				for( int j = 0; j < bSize; ++j )
				{
					final ABuffer buffer = buffersToUpdate.get( j ) ;
					final GLStorage storage = storageLookup.getRHS( buffer.index() ) ;
					if( storage.update( ( Storage )buffer ) == false )
					{
						updater.forceUpdate() ;
					}
				}
				buffersToUpdate.clear() ;
			}
		}

		return totalBufferUpdates ;
	}

	private int updateBuffers( final int _difference, final int _frameNo )
	{
		int totalBufferUpdates = 0 ;

		final int dSize = drawUpdaters.size() ;
		for( int i = 0; i < dSize; ++i )
		{
			final IUpdater<? extends ABuffer> updater = drawUpdaters.get( i ) ;
			updater.update( buffersToUpdate, _difference, _frameNo ) ;
			if( buffersToUpdate.isEmpty() == false )
			{
				final int bSize = buffersToUpdate.size() ;
				totalBufferUpdates += bSize ;
				for( int j = 0; j < bSize; ++j )
				{
					final ABuffer buffer = buffersToUpdate.get( j ) ;
					if( updateBuffer( buffer, bufferLookup.getRHS( buffer.index() ) ) == false )
					{
						updater.forceUpdate() ;
					}
				}
				buffersToUpdate.clear() ;
			}
		}

		return totalBufferUpdates ;
	}
	
	private <T extends ABuffer> boolean updateBuffer( final T _buffer, final GLBuffer _buff )
	{
		switch( _buffer.getBufferType() )
		{
			default              : return true ;//throw new Exception( "Unknown buffer type specified." ) ;
			case GEOMETRY_BUFFER :
			{
				final GLGeometryBuffer buf = ( GLGeometryBuffer )_buff ;
				return buf.update( ( GeometryBuffer )_buffer ) ;
			}
			case TEXT_BUFFER     :
			{
				final GLTextBuffer buf = ( GLTextBuffer )_buff ;
				return buf.update( ( TextBuffer )_buffer, programLookup, storageLookup ) ;
			}
			case DRAW_BUFFER     :
			{
				final GLDrawBuffer buf = ( GLDrawBuffer )_buff ;
				return buf.update( ( DrawBuffer )_buffer, programLookup, bufferLookup, storageLookup ) ;
			}
			case DRAW_INSTANCED_BUFFER     :
			{
				final GLDrawInstancedBuffer buf = ( GLDrawInstancedBuffer )_buff ;
				return buf.update( ( DrawInstancedBuffer )_buffer, programLookup, bufferLookup, storageLookup ) ;
			}
			case STENCIL                   :
			{
				final GLStencil stencil = ( GLStencil )_buff ;
				return stencil.update( ( Stencil )_buffer, bufferLookup ) ;
			}
			case DEPTH                     :
			{
				final GLDepth depth = ( GLDepth )_buff ;
				return depth.update( ( Depth )_buffer, bufferLookup ) ;
			}
		}
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
