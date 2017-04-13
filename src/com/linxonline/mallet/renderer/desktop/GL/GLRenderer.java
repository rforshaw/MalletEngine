package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Set ;
import java.util.HashSet ;

import com.jogamp.newt.opengl.GLWindow ;

import javax.media.opengl.GLEventListener ;
import javax.media.opengl.GLAutoDrawable ;
import javax.media.opengl.GLCapabilities ;
import javax.media.opengl.GLContext ;
import javax.media.opengl.GLProfile ;
import javax.media.opengl.GL3 ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;
import com.linxonline.mallet.renderer.texture.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.desktop.GL.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer<GLDrawData, CameraData, GLWorld, GLWorldState> implements GLEventListener
{
	public final static int ORTHOGRAPHIC_MODE = 1 ;
	public final static int PERSPECTIVE_MODE  = 2 ;

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final static Matrix4 uiMatrix                 = matrixCache.get() ;		// Used for rendering GUI elements not impacted by World/Camera position
	protected final static Matrix4 worldMatrix              = matrixCache.get() ;		// Used for moving the camera around the world

	protected final static Vector2 maxTextureSize = new Vector2() ;						// Maximum Texture resolution supported by the GPU.

	protected GLWindow canvas ;
	private static GL3 gl ;

	protected CameraData defaultCamera = new CameraData( "MAIN" ) ;
	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
		super( new GLWorldState() ) ;

		final GLWorldState worlds = getWorldState() ;
		worlds.setDefault( new GLWorld( "DEFAULT", 0, constructRemoveDelegate() ) ) ;

		defaultCamera.setDrawInterface( getCameraDraw() ) ;
		worlds.addCamera( defaultCamera, null ) ;

		initWindow() ;
	}

	@Override
	public void start()
	{
		Logger.println( "Starting renderer..", Logger.Verbosity.NORMAL ) ;
		super.start() ;

		canvas.addGLEventListener( this ) ;

		initAssist() ;
		canvas.setVisible( true ) ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		if( makeCurrent() == true )
		{
			clear() ;							// Clear the contents being rendered

			getWorldState().shutdown() ;
			programs.shutdown() ;
			textures.shutdown() ;				// We'll loose all texture and font resources
			fontManager.shutdown() ;
			release() ;
		}
	}

	@Override
	public FontAssist.Assist getFontAssist()
	{
		return new FontAssist.Assist()
		{
			@Override
			public MalletFont.Metrics createMetrics( final String _font,
													 final int _style,
													 final int _size )
			{
				return fontManager.generateMetrics( _font, _style, _size ) ;
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
			public Vector2 getMaximumTextureSize()
			{
				return new Vector2( maxTextureSize ) ;
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
			public World addWorld( final World _world )
			{
				if( _world instanceof GLWorld )
				{
					getWorldState().addWorld( ( GLWorld )_world ) ;
					return _world ;
				}
				return null ;
			}

			@Override
			public World removeWorld( final World _world )
			{
				if( _world instanceof GLWorld )
				{
					getWorldState().removeWorld( ( GLWorld )_world ) ;
					return _world ;
				}
				return null ;
			}

			@Override
			public World constructWorld( final String _id, final int _order )
			{
				final GLWorld world = new GLWorld( _id, _order, constructRemoveDelegate() ) ;
				getWorldState().addWorld( world ) ;
				return world ;
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
			public Camera addCamera( final Camera _camera, final World _world )
			{
				_camera.setDrawInterface( getCameraDraw() ) ;
				getWorldState().addCamera( cast( _camera ), cast( _world ) ) ;
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
	public GLDrawData.UploadInterface<GLDrawData> getBasicUpload()
	{
		return new GLDrawData.UploadInterface<GLDrawData>()
		{
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

				final GLWorld world = ( GLWorld )_data.getWorld() ;
				world.upload( gl, _data ) ;
			}
		} ;
	}

	@Override
	public CameraData.DrawInterface<CameraData> getCameraDraw()
	{
		return new CameraData.DrawInterface<CameraData>()
		{
			public void draw( final CameraData _camera )
			{
				final Vector2 scaleRtoD = getRenderInfo().getScaleRenderToDisplay() ;
				final Vector2 offset = getRenderInfo().getScreenOffset() ;
				final CameraData.Projection projection = _camera.getProjection() ;
				final CameraData.Screen screen = _camera.getRenderScreen() ;

				gl.glViewport( ( int )( offset.x + ( screen.offset.x * scaleRtoD.x ) ),
							   ( int )( offset.y + ( screen.offset.y * scaleRtoD.y ) ),
							   ( int )( screen.dimension.x * scaleRtoD.x ),
							   ( int )( screen.dimension.y * scaleRtoD.y ) ) ;

				final Vector3 position = _camera.getPosition() ;
				final Vector3 scale = _camera.getScale() ;
				//final Vector3 rotation = _camera.getRotation() ;

				worldMatrix.setIdentity() ;
				worldMatrix.translate( projection.nearPlane.x / 2 , projection.nearPlane.y / 2, 0.0f ) ;
				worldMatrix.scale( scale.x, scale.y, scale.z ) ;
				worldMatrix.translate( -position.x, -position.y, 0.0f ) ;

				final Matrix4 worldProjection = matrixCache.get() ;
				Matrix4.multiply( projection.matrix, worldMatrix, worldProjection ) ;

				final Matrix4 uiProjection = matrixCache.get() ;
				Matrix4.multiply( projection.matrix, uiMatrix, uiProjection ) ;

				final GLWorld world = ( GLWorld )_camera.getWorld() ;
				world.draw( gl, worldProjection, uiProjection ) ;

				matrixCache.reclaim( worldProjection ) ;
				matrixCache.reclaim( uiProjection ) ;
			}
		} ;
	}

	@Override
	public DrawState.RemoveDelegate<GLDrawData> constructRemoveDelegate()
	{
		return new DrawState.RemoveDelegate<GLDrawData>()
		{
			public void remove( final GLDrawData _data )
			{
				final GLWorld world = ( GLWorld )_data.getWorld() ;
				world.remove( gl, _data ) ;
			}
		} ;
	}

	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		super.setRenderDimensions( _width, _height ) ;
		if( makeCurrent() == true )
		{
			resize() ;
			release() ;
		}
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		super.setDisplayDimensions( _width, _height ) ;
		canvas.setSize( _width, _height ) ;

		if( makeCurrent() == true )
		{
			resize() ;
			release() ;
		}
	}

	@Override
	public void init( final GLAutoDrawable _drawable )
	{
		System.out.println( "GL3 Contex initialised.." ) ;
		gl = _drawable.getGL().getGL3() ;

		//System.out.println( "Vsync: " + GlobalConfig.getInteger( "VSYNC", 0 ) ) ;
		gl.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled

		gl.glEnable( GL3.GL_PRIMITIVE_RESTART ) ;		//GLRenderer.handleError( "Enable Primitive Restart", _gl ) ;
		gl.glPrimitiveRestartIndex( GLGeometryUploader.PRIMITIVE_RESTART_INDEX ) ;

		gl.glEnable( GL3.GL_BLEND ) ;										//GLRenderer.handleError( "Enable Blend", _gl ) ;
		gl.glBlendFunc( GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA ) ;	//GLRenderer.handleError( "Set Blend Func", _gl ) ;

		gl.glEnable( GL3.GL_CULL_FACE ) ;
		gl.glCullFace( GL3.GL_BACK ) ;  
		gl.glFrontFace( GL3.GL_CCW ) ;

		System.out.println( "Building default shaders.." ) ;
		programs.load( "SIMPLE_TEXTURE",  "base/shaders/desktop/simple_texture.jgl" ) ;
		programs.load( "SIMPLE_FONT",     "base/shaders/desktop/simple_font.jgl" ) ;
		programs.load( "SIMPLE_GEOMETRY", "base/shaders/desktop/simple_geometry.jgl" ) ;
		programs.load( "SIMPLE_STENCIL",  "base/shaders/desktop/simple_stencil.jgl" ) ;

		{
			// Query for the Max Texture Size and store the results.
			// I doubt the size will change during the running of the engine.
			final int[] size = new int[1] ;
			gl.glGetIntegerv( GL3.GL_MAX_TEXTURE_SIZE, size, 0 ) ;
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
		super.setDisplayDimensions( _width, _height ) ;
		if( GlobalConfig.getBoolean( "DISPLAYRENDERPARITY", false ) == true )
		{
			// Update the render dimensions if the window size 
			// and render size are meant to be identical.
			// Some users will not want parity, using a larger window 
			// size but rendering to a smaller size and subsequently being upscaled.
			super.setRenderDimensions( _width, _height ) ;
		}
		resize() ;
	}

	protected void resize()
	{
		switch( viewMode )
		{
			case PERSPECTIVE_MODE  : System.out.println( "Perspective Mode currently not implemented.." ) ; break ;
			case ORTHOGRAPHIC_MODE : 
			default                :
			{
				final Vector2 dimension = getRenderInfo().getRenderDimensions() ;
				//final Vector2 offset = getRenderInfo().getScreenOffset() ;

				CameraAssist.amendOrthographic( defaultCamera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( defaultCamera, ( int )dimension.x, ( int )dimension.y ) ;
				break ;
			}
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
		gl.glClear( GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT ) ;

		getEventController().update() ;

		getWorldState().upload( ( int )( updateDT / drawDT ), renderIter ) ;
		canvas.swapBuffers() ;
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

	public static GL3 getGL()
	{
		return gl ;
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

			final Vector2 display = getRenderInfo().getDisplayDimensions() ;
			canvas.setSize( ( int )display.x, ( int )display.y ) ;

			// We want to be in complete control of any swapBuffer calls
			canvas.setAutoSwapBufferMode( false ) ;
		}
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

	protected static Texture<GLImage> getTexture( final String _path )
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

	public static void handleError( final String _txt, final GL3 _gl )
	{
		int error = 0 ;
		while( ( error = _gl.glGetError() ) != GL3.GL_NO_ERROR )
		{
			switch( error )
			{
				case GL3.GL_NO_ERROR                      : break ;
				case GL3.GL_INVALID_ENUM                  : System.out.println( _txt + ": GL_INVALID_ENUM" ) ; break ;
				case GL3.GL_INVALID_VALUE                 : System.out.println( _txt + ": GL_INVALID_VALUE" ) ; break ;
				case GL3.GL_INVALID_OPERATION             : System.out.println( _txt + ": GL_INVALID_OPERATION" ) ; break ;
				case GL3.GL_INVALID_FRAMEBUFFER_OPERATION : System.out.println( _txt + ": GL_INVALID_FRAMEBUFFER_OPERATION" ) ; break ;
				case GL3.GL_OUT_OF_MEMORY                 : System.out.println( _txt + ": GL_OUT_OF_MEMORY" ) ; break ;
				case GL3.GL_STACK_UNDERFLOW               : System.out.println( _txt + ": GL_STACK_UNDERFLOW" ) ; break ;
				case GL3.GL_STACK_OVERFLOW                : System.out.println( _txt + ": GL_STACK_OVERFLOW" ) ; break ;
				default                                   : System.out.println( _txt + ": Unknown Error." ) ; break ;
			}
		}
	}
}
