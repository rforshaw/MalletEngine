package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import java.awt.Insets ;
import java.awt.Dimension ;
import java.awt.Frame ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;
import java.util.Stack ;

import com.jogamp.newt.opengl.GLWindow ;
import javax.media.opengl.* ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;
import com.linxonline.mallet.renderer.texture.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.desktop.GL.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer<GLWorldState> implements GLEventListener
{
	public final static int ORTHOGRAPHIC_MODE = 1 ;
	public final static int PERSPECTIVE_MODE  = 2 ;

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLDrawData> renderCache = new ObjectCache<GLDrawData>( GLDrawData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final static Matrix4 uiMatrix                 = matrixCache.get() ;		// Used for rendering GUI elements not impacted by World/Camera position
	protected final static Matrix4 worldMatrix              = matrixCache.get() ;		// Used for moving the camera around the world

	protected GLWindow canvas ;
	protected static GL3 gl ;

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
			public Camera addCamera( final Camera _camera, final World _world )
			{
				if( _camera instanceof CameraData )
				{
					_camera.setDrawInterface( getCameraDraw() ) ;
					getWorldState().addCamera( ( CameraData )_camera, ( GLWorld )_world ) ;
				}

				return _camera ;
			}

			@Override
			public Camera removeCamera( final Camera _camera, final World _world )
			{
				if( _camera instanceof CameraData )
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
				world.upload( gl, _data ) ;
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
				world.upload( gl, _data ) ;
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

				gl.glViewport( ( int )( offset.x + ( screen.offset.x * scaleRtoD.x ) ),
							   ( int )( offset.y + ( screen.offset.y * scaleRtoD.y ) ),
							   ( int )( screen.dimension.x * scaleRtoD.x ),
							   ( int )( screen.dimension.y * scaleRtoD.y ) ) ;

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
				world.draw( gl, worldProjection, uiProjection ) ;

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
				world.remove( gl, _data ) ;

				_data.unregister() ;
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
		System.out.println( "GL Contex initialised.." ) ;
		gl = _drawable.getGL().getGL3() ;

		//System.out.println( "Vsync: " + GlobalConfig.getInteger( "VSYNC", 0 ) ) ;
		gl.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		gl.glPrimitiveRestartIndex( GLGeometryUploader.PRIMITIVE_RESTART_INDEX ) ;

		gl.glEnable( GL.GL_CULL_FACE ) ;
		gl.glCullFace( GL.GL_BACK ) ;  
		gl.glFrontFace( GL.GL_CCW ) ;

		System.out.println( "Building default shaders.." ) ;
		programs.get( "SIMPLE_TEXTURE",  "base/shaders/desktop/simple_texture.jgl" ) ;
		programs.get( "SIMPLE_FONT",     "base/shaders/desktop/simple_font.jgl" ) ;
		programs.get( "SIMPLE_GEOMETRY", "base/shaders/desktop/simple_geometry.jgl" ) ;
		programs.get( "SIMPLE_STENCIL",  "base/shaders/desktop/simple_stencil.jgl" ) ;
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
				final Vector2 offset = getRenderInfo().getScreenOffset() ;

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
		canvas.display() ;
	}

	@Override
	public void display( final GLAutoDrawable _drawable )
	{
		gl = _drawable.getGL().getGL3() ;
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT ) ;
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

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
		getWorldState().clean() ;
		programs.clean() ;
		textures.clean() ;
		fontManager.clean() ;
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
			capabilities.setDoubleBuffered( true ) ;

			canvas = GLWindow.create( capabilities ) ;

			final Vector2 display = getRenderInfo().getDisplayDimensions() ;
			canvas.setSize( ( int )display.x, ( int )display.y ) ;
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
			}
		}
	}
}
