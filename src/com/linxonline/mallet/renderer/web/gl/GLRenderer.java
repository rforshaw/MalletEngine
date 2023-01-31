package com.linxonline.mallet.renderer.web.gl ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;
import java.util.ArrayList ;
import java.text.MessageFormat ;

import org.teavm.jso.* ;
import org.teavm.jso.json.JSON ;
import org.teavm.jso.dom.html.* ;
import org.teavm.jso.dom.events.* ;
import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLUniformLocation ;
import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.webgl.WebGLTexture ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.BufferedList ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification.Notify ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.opengl.JSONProgram ;

public final class GLRenderer extends BasicRenderer
{
	public final static int ORTHOGRAPHIC_MODE = 1 ;
	public final static int PERSPECTIVE_MODE  = 2 ;

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

	protected final static Vector2 maxTextureSize = new Vector2() ;						// Maximum Texture resolution supported by the GPU.

	private final HTMLCanvasElement canvas ;

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

		final HTMLDocument document = HTMLDocument.current();
		canvas = ( HTMLCanvasElement )document.getElementById( "mallet-canvas" ) ;
		MGL.setGL( ( WebGLRenderingContext )canvas.getContext( "webgl" ) ) ;

		//MGL.enable( MGL.GL_PRIMITIVE_RESTART ) ;		//GLRenderer.handleError( "Enable Primitive Restart", _gl ) ;
		//MGL.primitiveRestartIndex( MGL.GL_PRIMITIVE_RESTART_INDEX ) ;

		MGL.enable( MGL.GL_BLEND ) ;										//GLRenderer.handleError( "Enable Blend", _gl ) ;
		MGL.blendFunc( MGL.GL_SRC_ALPHA, MGL.GL_ONE_MINUS_SRC_ALPHA ) ;	//GLRenderer.handleError( "Set Blend Func", _gl ) ;

		MGL.enable( MGL.GL_CULL_FACE ) ;
		MGL.cullFace( MGL.GL_BACK ) ;  
		MGL.frontFace( MGL.GL_CCW ) ;

		MGL.clearColor( 0.0f, 0.0f, 0.0f, 1.0f ) ;
		MGL.clear( MGL.GL_COLOR_BUFFER_BIT ) ;

		initDefaultWorld() ;

		System.out.println( "Building default shaders.." ) ;
		programs.load( "SIMPLE_TEXTURE", "base/shaders/web/simple_texture.jgl" ) ;
		programs.load( "SIMPLE_FONT", "base/shaders/web/simple_font.jgl" ) ;
		programs.load( "SIMPLE_GEOMETRY", "base/shaders/web/simple_geometry.jgl" ) ;
		programs.load( "SIMPLE_STENCIL", "base/shaders/web/simple_stencil.jgl" ) ;

		programs.load( "SIMPLE_INSTANCE_TEXTURE",  "base/shaders/web/simple_instance_texture.jgl" ) ;

		{
			// Query for the Max Texture Size and store the results.
			// I doubt the size will change during the running of the engine.
			final int size = MGL.getParameteri( MGL.GL_MAX_TEXTURE_SIZE ) ;
			maxTextureSize.setXY( size, size ) ;
		}
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
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		clear() ;							// Clear the contents being rendered

		for( final GLWorld world : worlds )
		{
			world.shutdown() ;
		}

		programs.shutdown() ;
		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.shutdown() ;
		//release() ;
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
			public String[] loadFont( final String _path )
			{
				assert( true ) ;
				return new String[0] ;
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
			public Set<String> getLoadedTextures( final Set _fill )
			{
				return textures.getLoadedKeys( _fill ) ;
			}

			@Override
			public Set<String> getAllTextures( final Set _fill )
			{
				return textures.getAllKeys( _fill ) ;
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
				GLRenderer.this.invokeLater( () ->
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
					programs.load( _id, MessageFormat.format( _path, "web" ) ) ;
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
				GLRenderer.this.invokeLater( () ->
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
				GLRenderer.this.invokeLater( () ->
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
				GLRenderer.this.invokeLater( () ->
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

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		updateCameraAndWorldDisplay( _width, _height ) ;
		canvas.setWidth( _width ) ;
		canvas.setHeight( _height ) ;
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

	public void draw( final float _dt )
	{
		super.draw( _dt ) ;
		final float updateDelta = getUpdateDeltaTime() ;
		final float frameDelta = getFrameDeltaTime() ;
		final int frameNo = getFrameIteration() ;

		// Expected number of render frames before the next update is triggered.
		final int difference = ( int )( updateDelta / frameDelta ) ;

		// We limit the number of textures that can be bound
		// each draw call - ensures things still feel responsive.
		textures.resetBindCount() ;

		for( final Camera camera : cameras )
		{
			if( camera.update( difference, frameNo ) == true )
			{
				final GLCamera glCamera = cameraLookup.getRHS( camera.index() ) ;
				glCamera.update( camera ) ;
			}
		}

		int totalBufferUpdates = 0 ;
		totalBufferUpdates += updateStorageBuffers( difference, frameNo ) ;
		totalBufferUpdates += updateBuffers( difference, frameNo ) ;

		updateExecutions() ;
		getEventController().update() ;

		for( final GLWorld world : worlds )
		{
			world.draw() ;
		}

		MGL.flush() ;
		MGL.finish() ;
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
	public void clean() {}

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

	protected static GLImage getTexture( final MalletTexture _texture )
	{
		final MalletTexture.Meta meta = _texture.getMeta() ;
		final String path = meta.getPath() ;

		if( meta.getAttachmentIndex() < 0 )
		{
			return textures.get( path ) ;
		}

		for( final GLWorld world : worlds )
		{
			if( path.equals( world.getID() ) == true )
			{
				return world.getImage( meta.getAttachmentIndex() ) ;
			}
		}

		return null ;
	}

	protected static GLFont getFont( final MalletFont _font )
	{
		return fontManager.get( _font ) ;
	}

	private int updateStorageBuffers( final int _difference, final int _frameNo )
	{
		int totalBufferUpdates = 0 ;

		for( final IUpdater<Storage> updater : storageUpdaters )
		{
			updater.update( buffersToUpdate, _difference, _frameNo ) ;
			if( buffersToUpdate.isEmpty() == false )
			{
				totalBufferUpdates += buffersToUpdate.size() ;
				for( final ABuffer buffer : buffersToUpdate )
				{
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

		for( final IUpdater<? extends ABuffer> updater : drawUpdaters )
		{
			updater.update( buffersToUpdate, _difference, _frameNo ) ;
			if( buffersToUpdate.isEmpty() == false )
			{
				totalBufferUpdates += buffersToUpdate.size() ;
				for( final ABuffer buffer : buffersToUpdate )
				{
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
		}
	}

	public static void handleError( final String _txt )
	{
		int error = 0 ;
		while( ( error = MGL.getError() ) != MGL.GL_NO_ERROR )
		{
			switch( error )
			{
				case MGL.GL_NO_ERROR                      : break ;
				case MGL.GL_INVALID_ENUM                  : System.out.println( _txt + ": GL_INVALID_ENUM" ) ; break ;
				case MGL.GL_INVALID_VALUE                 : System.out.println( _txt + ": GL_INVALID_VALUE" ) ; break ;
				case MGL.GL_INVALID_OPERATION             : System.out.println( _txt + ": GL_INVALID_OPERATION" ) ; break ;
				case MGL.GL_INVALID_FRAMEBUFFER_OPERATION : System.out.println( _txt + ": GL_INVALID_FRAMEBUFFER_OPERATION" ) ; break ;
				case MGL.GL_OUT_OF_MEMORY                 : System.out.println( _txt + ": GL_OUT_OF_MEMORY" ) ; break ;
				//case MGL.STACK_UNDERFLOW               : System.out.println( _txt + ": GL_STACK_UNDERFLOW" ) ; break ;
				//case MGL.STACK_OVERFLOW                : System.out.println( _txt + ": GL_STACK_OVERFLOW" ) ; break ;
			}
		}
	}
}
