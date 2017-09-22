package com.linxonline.mallet.renderer.android.GL ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;

import android.opengl.GLES30 ;
import android.opengl.EGL14 ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.android.GL.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer<GLDrawData, CameraData, GLWorld, GLWorldState>
{
	public final static int ORTHOGRAPHIC_MODE = 1 ;
	public final static int PERSPECTIVE_MODE  = 2 ;

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;

	protected final static Vector2 maxTextureSize = new Vector2() ;						// Maximum Texture resolution supported by the GPU.

	protected CameraData<CameraData> defaultCamera = new CameraData<CameraData>( "MAIN" ) ;
	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
		super( new GLWorldState() ) ;
		textures.setWorldState( getWorldState() ) ;
		initAssist() ;
		initDefaultWorld() ;
	}

	@Override
	public void start()
	{
		Logger.println( "Starting renderer..", Logger.Verbosity.NORMAL ) ;
		super.start() ;
		initGraphics() ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		clear() ;							// Clear the contents being rendered

		getWorldState().shutdown() ;
		programs.shutdown() ;
		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.shutdown() ;
	}

	/**
		It's possible for the OpenGL Context 
		to be lost on Android devices.
		We need to remove previous references to 
		OpenGL resources and reload them.
	*/
	public void recover()
	{
		final GLWorldState worlds = getWorldState() ;
		worlds.shutdown() ;
		programs.shutdown() ;
		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.recover() ;

		Logger.println( "Recovering renderer state..", Logger.Verbosity.NORMAL ) ;
		final List<GLWorld> worldContent = worlds.getCurrentData() ;
		for( final GLWorld world : worldContent )
		{
			world.init() ;
			final DrawState state = world.getDrawState() ;
			final List<DrawData> drawContent = state.getActiveDraws() ;
			for( final DrawData draw : drawContent )
			{
				final GLDrawData d = ( GLDrawData )draw ;
				d.setBuffer( null ) ;
				d.setLocation( null ) ;

				final ProgramMap<GLProgram> program = ( ProgramMap<GLProgram> )d.getProgram() ;
				program.setProgram( null ) ;
				program.dirty() ;
				d.forceUpdate() ;
			}
		}

		//initGraphics() ;
	}

	private void initGraphics()
	{
		//GLES30.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		GLES30.glEnable( GLES30.GL_BLEND ) ;

		GLES30.glEnable( GLES30.GL_CULL_FACE ) ;
		GLES30.glCullFace( GLES30.GL_BACK ) ;  
		GLES30.glFrontFace( GLES30.GL_CCW ) ;

		System.out.println( "Building default shaders.." ) ;
		programs.load( "SIMPLE_TEXTURE", "base/shaders/android/simple_texture.jgl" ) ;
		programs.load( "SIMPLE_FONT", "base/shaders/android/simple_font.jgl" ) ;
		programs.load( "SIMPLE_GEOMETRY", "base/shaders/android/simple_geometry.jgl" ) ;
		//programs.load( "SIMPLE_STENCIL", "base/shaders/android/simple_stencil.jgl" ) ;

		{
			// Query for the Max Texture Size and store the results.
			// I doubt the size will change during the running of the engine.
			final int[] size = new int[1] ;
			GLES30.glGetIntegerv( GLES30.GL_MAX_TEXTURE_SIZE, size, 0 ) ;
			maxTextureSize.setXY( size[0], size[0] ) ;
		}

		resize() ;
	}

	private void initDefaultWorld()
	{
		final GLWorldState worlds = getWorldState() ;

		final GLWorld world = GLWorld.createDefaultWorld( "DEFAULT", 0 ) ;
		world.getDrawState().setUploadInterface( new GLBasicUpload( world ) ) ;
		world.getDrawState().setRemoveDelegate( new GLBasicRemove( world ) ) ;

		worlds.addWorld( world ) ;
		worlds.setDefault( world ) ;

		world.getCameraState().setDrawInterface( new GLCameraDraw( world ) ) ;
		worlds.addCamera( defaultCamera, null ) ;
	}

	@Override
	public FontAssist.Assist getFontAssist()
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

	@Override
	public TextureAssist.Assist getTextureAssist()
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

	@Override
	public DrawAssist.Assist getDrawAssist()
	{
		return new DrawAssist.Assist()
		{
			@Override
			public Draw amendShape( final Draw _draw, final Shape _shape )
			{
				cast( _draw ).setDrawShape( _shape ) ;
				return _draw ;
			}

			@Override
			public Draw amendRotate( final Draw _draw, final float _x, final float _y, final float _z )
			{
				cast( _draw ).setRotation( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendScale( final Draw _draw, final float _x, final float _y, final float _z )
			{
				cast( _draw ).setScale( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendPosition( final Draw _draw, final float _x, final float _y, final float _z )
			{
				cast( _draw ).setPosition( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendOffset( final Draw _draw, final float _x, final float _y, final float _z )
			{
				cast( _draw ).setOffset( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendText( final Draw _draw, final StringBuilder _text )
			{
				final GLDrawData draw = cast( _draw ) ;
				if( draw.getMode() == GLDrawData.Mode.TEXT )
				{
					// Only a text draw object can have text.
					draw.setText( _text ) ;
				}
				return _draw ;
			}

			@Override
			public Draw amendTextStart( final Draw _draw, final int _start )
			{
				final GLDrawData draw = cast( _draw ) ;
				if( draw.getMode() == GLDrawData.Mode.TEXT )
				{
					// Only a text draw object can have text.
					draw.setTextStart( _start ) ;
				}
				return _draw ;
			}

			@Override
			public Draw amendTextEnd( final Draw _draw, final int _end )
			{
				final GLDrawData draw = cast( _draw ) ;
				if( draw.getMode() == GLDrawData.Mode.TEXT )
				{
					// Only a text draw object can have text.
					draw.setTextEnd( _end ) ;
				}
				return _draw ;
			}

			@Override
			public Draw amendUI( final Draw _draw, final boolean _ui )
			{
				cast( _draw ).setUI( _ui ) ;
				return _draw ;
			}

			@Override
			public Draw amendColour( final Draw _draw, final MalletColour _colour )
			{
				cast( _draw ).setColour( _colour ) ;
				return _draw ;
			}

			@Override
			public Draw amendOrder( final Draw _draw, final int _order )
			{
				cast( _draw ).setOrder( _order ) ;
				return _draw ;
			}

			@Override
			public Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation )
			{
				cast( _draw ).setInterpolationMode( _interpolation ) ;
				return _draw ;
			}

			@Override
			public Draw amendUpdateType( final Draw _draw, final UpdateType _type )
			{
				cast( _draw ).setUpdateType( _type ) ;
				return _draw ;
			}

			@Override
			public Draw attachProgram( final Draw _draw, final Program _program )
			{
				cast( _draw ).setProgram( _program ) ;
				return _draw ;
			}

			@Override
			public Draw forceUpdate( final Draw _draw )
			{
				cast( _draw ).forceUpdate() ;
				return _draw ;
			}

			@Override
			public Shape getDrawShape( final Draw _draw )
			{
				return cast( _draw ).getDrawShape() ;
			}

			@Override
			public Vector3 getRotate( final Draw _draw )
			{
				return cast( _draw ).getRotation() ;
			}

			@Override
			public Vector3 getScale( final Draw _draw )
			{
				return cast( _draw ).getScale() ;
			}

			@Override
			public Vector3 getPosition( final Draw _draw )
			{
				return cast( _draw ).getPosition() ;
			}

			@Override
			public Vector3 getOffset( final Draw _draw )
			{
				return cast( _draw ).getOffset() ;
			}

			@Override
			public StringBuilder getText( final Draw _draw )
			{
				// This will return null if not a Text Draw.
				return cast( _draw ).getText() ;
			}

			@Override
			public MalletColour getColour( final Draw _draw )
			{
				return cast( _draw ).getColour() ;
			}

			@Override
			public boolean isUI( final Draw _draw )
			{
				return cast( _draw ).isUI() ;
			}

			@Override
			public Program getProgram( final Draw _draw )
			{
				return cast( _draw ).getProgram() ;
			}

			@Override
			public Draw createTextDraw( final StringBuilder _text,
										final MalletFont _font,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order )
			{
				final GLDrawData draw = cast( createDraw( _position, _offset, _rotation, _scale, _order ) ) ;
				draw.setMode( GLDrawData.Mode.TEXT ) ;

				final Program program = ProgramAssist.create( "SIMPLE_FONT" ) ;
				ProgramAssist.map( program, "inTex0", _font ) ;

				attachProgram( draw, program ) ;
				draw.setText( _text ) ;
				return draw ;
			}

			@Override
			public Draw createTextDraw( final String _text,
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
			public Draw createClipDraw( final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _startOrder,
										final int _endOrder )
			{
				final GLDrawData draw = cast( createDraw( _position, _offset, _rotation, _scale, _startOrder ) ) ;
				draw.setMode( GLDrawData.Mode.STENCIL ) ;
				draw.setEndOrder( _endOrder ) ;

				attachProgram( draw, ProgramAssist.create( "SIMPLE_STENCIL" ) ) ;
				return draw ;
			}

			@Override
			public Draw createDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _order )
			{
				return new GLDrawData( GLDrawData.Mode.BASIC,
									   UpdateType.ON_DEMAND,
									   Interpolation.NONE,
									   _position,
									   _offset,
									   _rotation,
									   _scale,
									   _order ) ;
			}

			private GLDrawData cast( final Draw _draw )
			{
				assert( _draw != null ) ;
				assert( !( _draw instanceof GLDrawData ) ) ;
				return ( GLDrawData )_draw ;
			}
		} ;
	}

	@Override
	public ProgramAssist.Assist getProgramAssist()
	{
		return new ProgramAssist.Assist()
		{
			public void load( final String _id, final String _path ) {}

			public Program create( final String _id )
			{
				final Program program = new ProgramMap<GLProgram>( _id ) ;
				return program ;
			}

			public Program remove( final Program _program, final String _handler )
			{
				final ProgramMap<GLProgram> program = cast( _program ) ;
				program.remove( _handler ) ;
				return _program ;
			}

			public Program map( final Program _program, final String _handler, final Object _obj )
			{
				final ProgramMap<GLProgram> program = cast( _program ) ;
				program.set( _handler, _obj ) ;
				return _program ;
			}

			private ProgramMap<GLProgram> cast( final Program _program )
			{
				assert( _program != null ) ;
				assert( !( _program instanceof ProgramMap ) ) ;
				return ( ProgramMap<GLProgram> )_program ;
			}
		} ;
	}

	@Override
	public WorldAssist.Assist getWorldAssist()
	{
		return new WorldAssist.Assist()
		{
			@Override
			public World getDefaultWorld()
			{
				return getWorldState().getWorld( ( GLWorld )null ) ;
			}

			@Override
			public World getWorld( final String _id )
			{
				return getWorldState().getWorld( _id ) ;
			}

			@Override
			public World addWorld( final World _world )
			{
				final GLWorld world = cast( _world ) ;
				getWorldState().addWorld( world ) ;
				return _world ;
			}

			@Override
			public World removeWorld( final World _world )
			{
				final GLWorld world = cast( _world ) ;
				getWorldState().removeWorld( world ) ;
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
				final GLWorld world = new GLWorld( _id, _order ) ;
				world.getDrawState().setUploadInterface( new GLBasicUpload( world ) ) ;
				world.getDrawState().setRemoveDelegate( new GLBasicRemove( world ) ) ;
				getWorldState().addWorld( world ) ;
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

	@Override
	public CameraAssist.Assist getCameraAssist()
	{
		return new CameraAssist.Assist()
		{
			@Override
			public Camera getDefaultCamera()
			{
				return defaultCamera ;
			}

			@Override
			public Camera amendOrthographic( final Camera _camera,
											 final float _top,
											 final float _bottom,
											 final float _left,
											 final float _right,
											 final float _near,
											 final float _far )
			{
				final CameraData camera = cast( _camera ) ;
				final CameraData.Projection projection = camera.getProjection() ;

				projection.nearPlane.setXYZ( _right - _left, _bottom - _top, _near ) ;
				projection.farPlane.setXYZ( projection.nearPlane.x, projection.nearPlane.y, _far ) ;

				final float invZ = 1.0f / ( _far - _near ) ;
				final float invY = 1.0f / ( _top - _bottom ) ;
				final float invX = 1.0f / ( _right - _left ) ;

				final Matrix4 proj = projection.matrix ;
				proj.set( 2.0f * invX, 0.0f,        0.0f,         ( -( _right + _left ) * invX ),
						  0.0f,        2.0f * invY, 0.0f,         ( -( _top + _bottom ) * invY ),
						  0.0f,        0.0f,        -2.0f * invZ, ( -( _far + _near ) * invZ ),
						  0.0f,        0.0f,        0.0f,         1.0f ) ;
				return _camera ;
			}

			@Override
			public Camera amendPosition( final Camera _camera, final float _x, final float _y, final float _z )
			{
				cast( _camera ).setPosition( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendRotation( final Camera _camera, final float _x, final float _y, final float _z )
			{
				cast( _camera ).setRotation( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendScale( final Camera _camera, final float _x, final float _y, final float _z )
			{
				cast( _camera ).setScale( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendUIPosition( final Camera _camera, final float _x, final float _y, final float _z )
			{
				cast( _camera ).setUIPosition( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendScreenResolution( final Camera _camera, final int _width, final int _height )
			{
				final CameraData.Screen screen = cast( _camera ).getRenderScreen() ;
				screen.setDimension( _width, _height ) ;
				return _camera ;
			}

			@Override
			public Camera amendScreenOffset( final Camera _camera, final int _x, final int _y )
			{
				final CameraData.Screen screen = cast( _camera ).getRenderScreen() ;
				screen.setOffset( _x, _y ) ;
				return _camera ;
			}

			public Camera amendDisplayResolution( final Camera _camera, final int _width, final int _height )
			{
				final CameraData.Screen screen = cast( _camera ).getDisplayScreen() ;
				screen.setDimension( _width, _height ) ;
				return _camera ;
			}

			public Camera amendDisplayOffset( final Camera _camera, final int _x, final int _y )
			{
				final CameraData.Screen screen = cast( _camera ).getDisplayScreen() ;
				screen.setOffset( _x, _y ) ;
				return _camera ;
			}

			@Override
			public boolean getPosition( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( cast( _camera ).getPosition() ) ;
				return true ;
			}

			@Override
			public boolean getRotation( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( cast( _camera ).getRotation() ) ;
				return true ;
			}

			@Override
			public boolean getScale( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( cast( _camera ).getScale() ) ;
				return true ;
			}

			@Override
			public boolean getDimensions( final Camera _camera, final Vector3 _populate )
			{
				final CameraData.Projection projection = cast( _camera ).getProjection() ;
				_populate.setXYZ( projection.nearPlane ) ;
				return true ;
			}

			@Override
			public boolean getUIPosition( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( cast( _camera ).getUIPosition() ) ;
				return true ;
			}

			@Override
			public float convertInputToCameraX( final Camera _camera, final float _inputX )
			{
				final CameraData camera = cast( _camera ) ;
				return camera.convertInputToX( _inputX ) ;
			}

			@Override
			public float convertInputToCameraY( final Camera _camera, final float _inputY )
			{
				final CameraData camera = cast( _camera ) ;
				return camera.convertInputToY( _inputY ) ;
			}

			@Override
			public float convertInputToUICameraX( final Camera _camera, final float _inputX )
			{
				final CameraData camera = cast( _camera ) ;
				return camera.convertInputToUIX( _inputX ) ;
			}

			@Override
			public float convertInputToUICameraY( final Camera _camera, final float _inputY )
			{
				final CameraData camera = cast( _camera ) ;
				return camera.convertInputToUIY( _inputY ) ;
			}

			@Override
			public Camera addCamera( final Camera _camera, final World _world )
			{
				final GLWorld world = getWorldState().getWorld( cast( _world ) ) ;
				world.getCameraState().setDrawInterface( new GLCameraDraw( world ) ) ;
				getWorldState().addCamera( cast( _camera ), world ) ;
				return _camera ;
			}

			@Override
			public Camera removeCamera( final Camera _camera, final World _world )
			{
				getWorldState().removeCamera( cast( _camera ) ) ;
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

	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		super.setRenderDimensions( _width, _height ) ;
		resize() ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		super.setDisplayDimensions( _width, _height ) ;
		resize() ;
	}

	public void setViewMode( final int _mode )
	{
		viewMode = _mode ;
	}

	protected void resize()
	{
		switch( viewMode )
		{
			case PERSPECTIVE_MODE  : System.out.println( "Perspective Mode currently not implemented.." ) ; break ;
			case ORTHOGRAPHIC_MODE : 
			default                :
			{
				final Vector2 dimension = defaultCamera.getRenderScreen().dimension ;
				CameraAssist.amendOrthographic( defaultCamera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
				break ;
			}
		}
	}

	public void display()
	{
		updateExecutions() ;

		getEventController().update() ;

		//System.out.println( "Draw" ) ;
		getWorldState().upload( ( int )( updateDT / drawDT ), renderIter ) ;
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
		getWorldState().clean( activeKeys ) ;

		programs.clean( activeKeys ) ;
		textures.clean( activeKeys ) ;
		fontManager.clean( activeKeys ) ;
	}

	/**
		Attempt to acquire a compatible GLProgram from 
		the ProgramManager. Make sure the GLProgram 
		requested maps correctly with the ProgramMap 
		defined in the GLDrawData object.
	*/
	private static boolean loadProgram( final GLDrawData _data )
	{
		final ProgramMap<GLProgram> program = ( ProgramMap<GLProgram> )_data.getProgram() ;
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
				_data.forceUpdate() ;
				return false ;
			}

			if( glProgram.isValidMap( program.getMaps() ) == false )
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

	public static void handleError( final String _txt )
	{
		int error = 0 ;
		while( ( error = GLES30.glGetError() ) != GLES30.GL_NO_ERROR )
		{
			switch( error )
			{
				case GLES30.GL_NO_ERROR                      : break ;
				case GLES30.GL_INVALID_ENUM                  : System.out.println( _txt + ": GL_INVALID_ENUM" ) ; break ;
				case GLES30.GL_INVALID_VALUE                 : System.out.println( _txt + ": GL_INVALID_VALUE" ) ; break ;
				case GLES30.GL_INVALID_OPERATION             : System.out.println( _txt + ": GL_INVALID_OPERATION" ) ; break ;
				case GLES30.GL_INVALID_FRAMEBUFFER_OPERATION : System.out.println( _txt + ": GL_INVALID_FRAMEBUFFER_OPERATION" ) ; break ;
				case GLES30.GL_OUT_OF_MEMORY                 : System.out.println( _txt + ": GL_OUT_OF_MEMORY" ) ; break ;
			}
		}
	}

	private final static class GLBasicRemove implements DrawState.RemoveDelegate<GLDrawData>
	{
		private final GLWorld world ;

		public GLBasicRemove( final GLWorld _world )
		{
			world = _world ;
		}

		@Override
		public void remove( final GLDrawData _data )
		{
			final GLGeometryUploader uploader = world.getUploader() ;
			uploader.remove( _data ) ;
		}
	}

	private final static class GLBasicUpload implements DrawState.IUpload<GLDrawData>
	{
		private final GLWorld world ;

		public GLBasicUpload( final GLWorld _world )
		{
			world = _world ;
		}

		@Override
		public void upload( final GLDrawData _data )
		{
			if( loadProgram( _data ) == false )
			{
				return ;
			}

			final Vector3 position = _data.getPosition() ;
			final Vector3 offset   = _data.getOffset() ;
			final Vector3 rotation = _data.getRotation() ;

			final Matrix4 positionMatrix = _data.getDrawMatrix() ;
			positionMatrix.setIdentity() ;

			positionMatrix.setTranslate( position.x, position.y, 0.0f ) ;
			positionMatrix.rotate( rotation.x, 1.0f, 0.0f, 0.0f ) ;
			positionMatrix.rotate( rotation.y, 0.0f, 1.0f, 0.0f ) ;
			positionMatrix.rotate( rotation.z, 0.0f, 0.0f, 1.0f ) ;
			positionMatrix.translate( offset.x, offset.y, offset.z ) ;

			final GLGeometryUploader uploader = world.getUploader() ;
			uploader.upload( _data ) ;
		}
	}

	private final static class GLCameraDraw implements CameraState.IDraw<CameraData>
	{
		private final GLWorld world ;

		private final static Matrix4 uiMatrix = new Matrix4() ;		// Used for rendering GUI elements not impacted by World/Camera position
		private final static Matrix4 worldMatrix = new Matrix4() ;	// Used for moving the camera around the world

		private final Matrix4 worldProjection = new Matrix4() ;
		private final Matrix4 uiProjection = new Matrix4() ;

		public GLCameraDraw( final GLWorld _world )
		{
			world = _world ;
		}

		@Override
		public void draw( final CameraData _camera )
		{
			final CameraData.Projection projection = _camera.getProjection() ;
			final CameraData.Screen screen = _camera.getRenderScreen() ;

			final int width = ( int )screen.dimension.x ;
			final int height = ( int )screen.dimension.y ;
			GLES30.glViewport( 0, 0, width, height ) ;

			final Vector3 uiPosition = _camera.getUIPosition() ;
			final Vector3 position = _camera.getPosition() ;
			final Vector3 scale = _camera.getScale() ;
			//final Vector3 rotation = _camera.getRotation() ;

			worldMatrix.setIdentity() ;
			worldMatrix.translate( projection.nearPlane.x / 2 , projection.nearPlane.y / 2, 0.0f ) ;
			worldMatrix.scale( scale.x, scale.y, scale.z ) ;
			worldMatrix.translate( -position.x, -position.y, 0.0f ) ;

			uiMatrix.setIdentity() ;
			uiMatrix.translate( -uiPosition.x, -uiPosition.y, 0.0f ) ;

			worldProjection.setIdentity() ;
			Matrix4.multiply( projection.matrix, worldMatrix, worldProjection ) ;

			uiProjection.setIdentity() ;
			Matrix4.multiply( projection.matrix, uiMatrix, uiProjection ) ;

			final GLGeometryUploader uploader = world.getUploader() ;
			uploader.draw( worldProjection, uiProjection ) ;
		}
	}
}
