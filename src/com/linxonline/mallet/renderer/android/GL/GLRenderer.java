package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

import android.opengl.GLES30 ;
import android.opengl.EGL14 ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;
import com.linxonline.mallet.renderer.texture.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.android.GL.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer<GLWorldState>
{
	public static final int ORTHOGRAPHIC_MODE = 1 ;
	public static final int PERSPECTIVE_MODE  = 2 ;

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLDrawData> renderCache = new ObjectCache<GLDrawData>( GLDrawData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final Matrix4 uiMatrix = matrixCache.get() ;						// Used for rendering GUI elements not impacted by World/Camera position
	protected final Matrix4 worldMatrix = matrixCache.get() ;					// Used for moving the camera around the world

	protected final static Vector2 maxTextureSize = new Vector2() ;				// Maximum Texture resolution supported by the GPU.

	protected CameraData defaultCamera = new CameraData( "MAIN" ) ;
	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
		super( new GLWorldState() ) ;

		final GLWorldState worlds = getWorldState() ;
		worlds.setDefault( new GLWorld( "DEFAULT", 0, constructRemoveDelegate() ) ) ;

		defaultCamera.setDrawInterface( getCameraDraw() ) ;
		worlds.addCamera( defaultCamera, null ) ;

		initAssist() ;
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
		final ArrayList<GLWorld> worldContent = worlds.getCurrentData() ;
		for( final GLWorld world : worldContent )
		{
			final DrawState state = world.getDrawState() ;
			final ArrayList<DrawData> drawContent = state.getActiveDraws() ;
			for( final DrawData draw : drawContent )
			{
				final GLDrawData d = ( GLDrawData )draw ;
				d.clearTextures() ;
				d.setGLBuffer( null ) ;
				d.setLocation( null ) ;
				( ( ProgramMap<GLProgram> )d.getProgram() ).setProgram( null ) ;
				d.forceUpdate() ;
			}
		}
	}

	private void initGraphics()
	{
		//GLES30.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		GLES30.glEnable( GLES30.GL_BLEND ) ;

		GLES30.glEnable( GLES30.GL_CULL_FACE ) ;
		GLES30.glCullFace( GLES30.GL_BACK ) ;  
		GLES30.glFrontFace( GLES30.GL_CCW ) ;

		System.out.println( "Building default shaders.." ) ;
		programs.get( "SIMPLE_TEXTURE", "base/shaders/android/simple_texture.jgl" ) ;
		programs.get( "SIMPLE_FONT", "base/shaders/android/simple_font.jgl" ) ;
		programs.get( "SIMPLE_GEOMETRY", "base/shaders/android/simple_geometry.jgl" ) ;
		//programs.get( "SIMPLE_STENCIL", "base/shaders/android/simple_stencil.jgl" ) ;

		{
			// Query for the Max Texture Size and store the results.
			// I doubt the size will change during the running of the engine.
			final int[] size = new int[1] ;
			GLES30.glGetIntegerv( GLES30.GL_MAX_TEXTURE_SIZE, size, 0 ) ;
			maxTextureSize.setXY( size[0], size[0] ) ;
		}
		
		resize() ;
	}

	@Override
	public void initAssist()
	{
		FontAssist.setFontWrapper( new FontAssist.Assist()
		{
			@Override
			public Font createFont( final String _font, final int _style, final int _size )
			{
				// If the GLFontMap has not been previously created, 
				// then a skeleton map is provided, skeleton is capable 
				// of being queried for text length and height, however,
				// cannot be used to draw until the font texture & glyph 
				// geometry is created during a drawText phase.
				final GLFontMap fontMap = fontManager.get( _font, _size ) ;

				return new Font<GLFontMap>( fontMap )
				{
					@Override
					public int getHeight()
					{
						return fontMap.getHeight() ;
					}

					@Override
					public int stringWidth( final StringBuilder _text )
					{
						return fontMap.stringWidth( _text ) ;
					}

					@Override
					public int stringWidth( final String _text )
					{
						return fontMap.stringWidth( _text ) ;
					}
				} ;
			}
		} ) ;

		TextureAssist.setAssist( new TextureAssist.Assist()
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
		} ) ;

		DrawAssist.setAssist( new DrawAssist.Assist()
		{
			@Override
			public Draw amendShape( final Draw _draw, final Shape _shape )
			{
				( ( GLDrawData )_draw ).setDrawShape( _shape ) ;
				return _draw ;
			}

			@Override
			public Draw amendClip( final Draw _draw, final Shape _clipSpace, final Vector3 _position, final Vector3 _offset )
			{
				final GLDrawData data = ( GLDrawData )_draw ;
				if( data.getClipMatrix() == null )
				{
					data.setClipMatrix( new Matrix4() ) ;
				}

				data.setClipShape( _clipSpace ) ;
				data.setClipPosition( _position ) ;
				data.setClipOffset( _offset ) ;
				data.setClipProgram( programs.get( "SIMPLE_STENCIL" ) ) ;
				return _draw ;
			}

			@Override
			public Draw amendRotate( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setRotation( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendScale( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setScale( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendPosition( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setPosition( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendOffset( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setOffset( _x, _y, _z ) ;
				return _draw ;
			}

			@Override
			public Draw amendText( final Draw _draw, final StringBuilder _text )
			{
				( ( GLDrawData )_draw ).setText( _text ) ;
				return _draw ;
			}

			@Override
			public Draw amendUI( final Draw _draw, final boolean _ui )
			{
				( ( GLDrawData )_draw ).setUI( _ui ) ;
				return _draw ;
			}

			@Override
			public Draw amendColour( final Draw _draw, final MalletColour _colour )
			{
				( ( GLDrawData )_draw ).setColour( _colour ) ;
				return _draw ;
			}

			@Override
			public Draw amendOrder( final Draw _draw, final int _order )
			{
				( ( GLDrawData )_draw ).setOrder( _order ) ;
				return _draw ;
			}

			@Override
			public Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation )
			{
				( ( GLDrawData )_draw ).setInterpolationMode( _interpolation ) ;
				return _draw ;
			}

			@Override
			public Draw amendUpdateType( final Draw _draw, final UpdateType _type )
			{
				( ( GLDrawData )_draw ).setUpdateType( _type ) ;
				return _draw ;
			}

			@Override
			public Draw attachProgram( final Draw _draw, final Program _program )
			{
				( ( GLDrawData )_draw ).setProgram( _program ) ;
				return _draw ;
			}

			@Override
			public Draw forceUpdate( final Draw _draw )
			{
				( ( GLDrawData )_draw ).forceUpdate() ;
				return _draw ;
			}

			@Override
			public Shape getDrawShape( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getDrawShape() ;
			}

			@Override
			public Vector3 getRotate( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getRotation() ;
			}

			@Override
			public Vector3 getScale( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getScale() ;
			}

			@Override
			public Vector3 getPosition( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getPosition() ;
			}

			@Override
			public Vector3 getOffset( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getOffset() ;
			}

			@Override
			public StringBuilder getText( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getText() ;
			}

			@Override
			public MalletColour getColour( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getColour() ;
			}

			@Override
			public boolean isUI( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).isUI() ;
			}

			@Override
			public Program getProgram( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getProgram() ;
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
				final GLDrawData draw = ( GLDrawData )createDraw( _position, _offset, _rotation, _scale, _order ) ;
				final Program program = ProgramAssist.createProgram( "SIMPLE_FONT" ) ;

				attachProgram( draw, program ) ;
				draw.setText( _text ) ;
				draw.setFont( _font ) ;
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
			public Draw createDraw( final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order )
			{
				final GLDrawData draw = new GLDrawData( UpdateType.ON_DEMAND, Interpolation.NONE, _position, _offset, _rotation, _scale, _order ) ;
				return draw ;
			}
		} ) ;

		ProgramAssist.setAssist( new ProgramAssist.Assist()
		{
			public Program createProgram( final String _id )
			{
				final Program program = new ProgramMap<GLProgram>( _id ) ;
				return program ;
			}

			public Program remove( final Program _program, final String _handler )
			{
				final ProgramMap<GLProgram> program = ( ProgramMap<GLProgram> )_program ;
				program.remove( _handler ) ;
				return _program ;
			}

			public Program map( final Program _program, final String _handler, final Object _obj )
			{
				final ProgramMap<GLProgram> program = ( ProgramMap<GLProgram> )_program ;
				program.set( _handler, _obj ) ;
				return _program ;
			}
		} ) ;

		CameraAssist.setAssist( new CameraAssist.Assist()
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
				final CameraData camera = ( CameraData )_camera ;
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
				( ( CameraData )_camera ).setPosition( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendRotation( final Camera _camera, final float _x, final float _y, final float _z )
			{
				( ( CameraData )_camera ).setRotation( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendScale( final Camera _camera, final float _x, final float _y, final float _z )
			{
				( ( CameraData )_camera ).setScale( _x, _y, _z ) ;
				return _camera ;
			}

			@Override
			public Camera amendScreenResolution( final Camera _camera, final int _width, final int _height )
			{
				final CameraData.Screen screen = ( ( CameraData )_camera ).getRenderScreen() ;
				screen.setDimension( _width, _height ) ;
				return _camera ;
			}

			@Override
			public Camera amendScreenOffset( final Camera _camera, final int _x, final int _y )
			{
				final CameraData.Screen screen = ( ( CameraData )_camera ).getRenderScreen() ;
				screen.setOffset( _x, _y ) ;
				return _camera ;
			}

			@Override
			public boolean getPosition( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( ( ( CameraData )_camera ).getPosition() ) ;
				return true ;
			}

			@Override
			public boolean getRotation( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( ( ( CameraData )_camera ).getRotation() ) ;
				return true ;
			}

			@Override
			public boolean getScale( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( ( ( CameraData )_camera ).getScale() ) ;
				return true ;
			}

			@Override
			public boolean getDimensions( final Camera _camera, final Vector3 _populate )
			{
				final CameraData.Projection projection = ( ( CameraData )_camera ).getProjection() ;
				_populate.setXYZ( projection.nearPlane ) ;
				return true ;
			}

			@Override
			public Camera addCamera( final Camera _camera, final World _world )
			{
				if( _camera != null && _camera instanceof CameraData )
				{
					_camera.setDrawInterface( getCameraDraw() ) ;
					getWorldState().addCamera( ( CameraData )_camera, ( GLWorld )_world ) ;
				}

				return _camera ;
			}

			@Override
			public Camera removeCamera( final Camera _camera, final World _world )
			{
				if( _camera != null && _camera instanceof CameraData )
				{
					getWorldState().removeCamera( ( CameraData )_camera ) ;
				}

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
		} ) ;

		WorldAssist.setAssist( new WorldAssist.Assist()
		{
			@Override
			public World getDefaultWorld()
			{
				final BasicWorld world = null ;
				return getWorldState().getWorld( world ) ;
			}

			@Override
			public World addWorld( final World _world )
			{
				getWorldState().addWorld( ( GLWorld )_world ) ;
				return _world ;
			}

			@Override
			public World removeWorld( final World _world )
			{
				getWorldState().removeWorld( ( GLWorld )_world ) ;
				return _world ;
			}

			@Override
			public World constructWorld( final String _id, final int _order )
			{
				final GLWorld world = new GLWorld( _id, _order, constructRemoveDelegate() ) ;
				getWorldState().addWorld( world ) ;
				return world ;
			}
		} ) ;
	}

	@Override
	public DrawData.UploadInterface getBasicUpload()
	{
		return new DrawData.UploadInterface<GLDrawData>()
		{
			public void upload( final GLDrawData _data )
			{
				if( loadProgram( _data ) == false )
				{
					return ;
				}

				final Vector3 clipPosition = _data.getClipPosition() ;
				final Vector3 clipOffset   = _data.getClipOffset() ;
				if( clipPosition != null && clipOffset != null )
				{
					final Matrix4 clipMatrix = _data.getClipMatrix() ;
					clipMatrix.setIdentity() ;

					clipMatrix.translate( clipPosition.x, clipPosition.y, clipPosition.z ) ;
					clipMatrix.translate( clipOffset.x, clipOffset.y, clipOffset.z ) ;
				}

				final Vector3 position = _data.getPosition() ;
				final Vector3 offset   = _data.getOffset() ;
				final Vector3 rotation = _data.getRotation() ;

				final Matrix4 positionMatrix = _data.getDrawMatrix() ;
				positionMatrix.setIdentity() ;

				positionMatrix.translate( position.x, position.y, 0.0f ) ;
				positionMatrix.rotate( rotation.x, 1.0f, 0.0f, 0.0f ) ;
				positionMatrix.rotate( rotation.y, 0.0f, 1.0f, 0.0f ) ;
				positionMatrix.rotate( rotation.z, 0.0f, 0.0f, 1.0f ) ;
				positionMatrix.translate( offset.x, offset.y, offset.z ) ;

				final GLWorld world = ( GLWorld )_data.getWorld() ;
				world.upload( _data ) ;
			}
		} ;
	}

	@Override
	public DrawData.UploadInterface getTextUpload()
	{
		return new DrawData.UploadInterface<GLDrawData>()
		{
			public void upload( final GLDrawData _data )
			{
				final StringBuilder text = _data.getText() ;
				if( text == null )
				{
					System.out.println( "No Text, set." ) ;
					return ;
				}

				final MalletFont font = _data.getFont() ;
				if( font == null )
				{
					System.out.println( "No Font, set." ) ;
					return ;
				}

				final GLFontMap fm = ( GLFontMap )font.font.getFont() ;
				if( fm.fontMap.texture == null )
				{
					// If the font maps texture has yet to be set,
					// generate the texture and bind it with the 
					// current OpenGL context
					fontManager.generateFontGeometry( font ) ;
				}

				ProgramAssist.map( _data.getProgram(), "inTex0", font ) ;
				if( loadProgram( _data ) == false )
				{
					return ;
				}

				final Vector3 position = _data.getPosition() ;
				final Vector3 offset   = _data.getOffset() ;
				final Vector3 rotate   = _data.getOffset() ;
				final boolean isGUI    = _data.isUI() ;

				final int height = fm.getHeight() ;
				final int lineWidth = /*_data.getLineWidth()*/500 + ( int )position.x ;

				final MalletColour colour = _data.getColour() ;
				final Matrix4 clipMatrix = _data.getClipMatrix() ;
				if( clipMatrix != null )
				{
					final Vector3 clipPosition = _data.getClipPosition() ;
					final Vector3 clipOffset   = _data.getClipOffset() ;
					clipMatrix.setIdentity() ;

					clipMatrix.translate( clipPosition.x, clipPosition.y, clipPosition.z ) ;
					clipMatrix.translate( clipOffset.x, clipOffset.y, clipOffset.z ) ;
				}

				final Matrix4 positionMatrix = _data.getDrawMatrix() ;
				positionMatrix.setIdentity() ;

				positionMatrix.translate( position.x, position.y, 0.0f ) ;
				positionMatrix.rotate( rotate.z, 0.0f, 0.0f, 1.0f ) ;
				positionMatrix.translate( offset.x, offset.y, offset.z ) ;

				_data.setDrawShape( fm.getGlyphWithChar( ' ' ).shape ) ;

				final GLWorld world = ( GLWorld )_data.getWorld() ;
				world.upload( _data ) ;
			}
		} ;
	}

	@Override
	public Camera.DrawInterface getCameraDraw()
	{
		return new Camera.DrawInterface<CameraData>()
		{
			public void draw( final CameraData _camera )
			{
				final Vector2 scaleRtoD = getRenderInfo().getScaleRenderToDisplay() ;
				final Vector2 offset = getRenderInfo().getScreenOffset() ;
				final CameraData.Projection projection = _camera.getProjection() ;
				final CameraData.Screen screen = _camera.getRenderScreen() ;

				GLES30.glViewport( ( int )( offset.x + ( screen.offset.x * scaleRtoD.x ) ),
								   ( int )( offset.y + ( screen.offset.y * scaleRtoD.y ) ),
								   ( int )( screen.dimension.x * scaleRtoD.x ),
								   ( int )( screen.dimension.y * scaleRtoD.y ) ) ;
				//GLRenderer.handleError( "Viewport: ", gl ) ;

				final Vector3 position = _camera.getPosition() ;
				final Vector3 scale = _camera.getScale() ;
				final Vector3 rotation = _camera.getRotation() ;

				worldMatrix.setIdentity() ;
				worldMatrix.translate( projection.nearPlane.x / 2 , projection.nearPlane.y / 2, 0.0f ) ;
				worldMatrix.scale( scale.x, scale.y, scale.z ) ;
				worldMatrix.translate( -position.x, -position.y, 0.0f ) ;

				final Matrix4 worldProjection = matrixCache.get() ;
				Matrix4.multiply( projection.matrix, worldMatrix, worldProjection ) ;

				final Matrix4 uiProjection = matrixCache.get() ;
				Matrix4.multiply( projection.matrix, uiMatrix, uiProjection ) ;

				final GLWorld world = ( GLWorld )_camera.getWorld() ;
				world.draw( worldProjection, uiProjection ) ;

				matrixCache.reclaim( worldProjection ) ;
				matrixCache.reclaim( uiProjection ) ;
			}
		} ;
	}

	@Override
	public DrawState.RemoveDelegate constructRemoveDelegate()
	{
		return new DrawState.RemoveDelegate<GLDrawData>()
		{
			public void remove( final GLDrawData _data )
			{
				final GLWorld world = ( GLWorld )_data.getWorld() ;
				world.remove( _data ) ;

				_data.unregister() ;
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
				final Vector2 dimension = getRenderInfo().getRenderDimensions() ;
				final Vector2 offset = getRenderInfo().getScreenOffset() ;

				CameraAssist.amendOrthographic( defaultCamera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
				CameraAssist.amendScreenResolution( defaultCamera, ( int )dimension.x, ( int )dimension.y ) ;
				break ;
			}
		}
	}

	public void display()
	{
		GLES30.glClear( GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT ) ;
		GLES30.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		getEventController().update() ;

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
		getWorldState().clean() ;
		programs.clean() ;
		textures.clean() ;
		fontManager.clean() ;
	}

	private boolean loadProgram( final GLDrawData _data )
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
				_data.forceUpdate() ;
				return false ;
			}

			if( glProgram.isValidMap( program.getMaps() ) == false )
			{
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
}
