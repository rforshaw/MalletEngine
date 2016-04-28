package com.linxonline.mallet.renderer.desktop.GL ;

import javax.swing.JFrame ;
import java.util.ArrayList ;
import java.awt.Insets ;
import java.awt.Dimension ;
import java.awt.Frame ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;
import java.lang.reflect.* ;
import java.util.Stack ;

import javax.media.opengl.* ;
import javax.media.opengl.awt.GLCanvas ;
import javax.media.opengl.awt.GLJPanel ;
import javax.media.opengl.glu.GLU ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;

import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.desktop.GL.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer implements GLEventListener
{
	private static final MalletColour WHITE = MalletColour.white() ;
	private static final MalletColour BLACK = MalletColour.black() ;

	private static final MalletColour RED   = MalletColour.red() ;
	private static final MalletColour GREEN = MalletColour.green() ;
	private static final MalletColour BLUE  = MalletColour.blue() ;

	public static final int ORTHOGRAPHIC_MODE = 1 ;
	public static final int PERSPECTIVE_MODE  = 2 ;

	protected final static GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;
	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLDrawData> renderCache = new ObjectCache<GLDrawData>( GLDrawData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache  = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final static Matrix4 uiMatrix                  = matrixCache.get() ;		// Used for rendering GUI elements not impacted by World/Camera position
	protected final static Matrix4 worldMatrix               = matrixCache.get() ;		// Used for moving the camera around the world

	protected static final GLU glu = new GLU() ;
	protected static GLCanvas canvas = null ;
	protected static GL3 gl = null ;
	protected JFrame frame = null ;

	protected Vector3 pos = new Vector3() ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer() {}

	@Override
	public void start()
	{
		Logger.println( "Starting renderer..", Logger.Verbosity.NORMAL ) ;
		super.start() ;
		initGraphics() ;
		initAssist() ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		clear() ;							// Clear the contents being rendered

		uploader.shutdown() ;
		programs.shutdown() ;
		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.shutdown() ;

		frame.dispose() ;
	}

	private void initGraphics()
	{
		final GLProfile glProfile = GLProfile.get( GLProfile.GL3 ) ;
		final GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
		capabilities.setStencilBits( 1 ) ;			// Provide ON/OFF Stencil Buffers
		capabilities.setDoubleBuffered( true ) ;

		canvas = new GLCanvas( capabilities ) ;
		canvas.setAutoSwapBufferMode( false ) ;
		canvas.addGLEventListener( this ) ;
	}

	@Override
	public void initAssist()
	{
		FontAssist.setFontWrapper( new FontAssist.Assist()
		{
			@Override
			public Font createFont( final String _font, final int _style, final int _size )
			{
				canvas.getContext().makeCurrent() ;
				final GLFontMap fontMap = fontManager.get( _font, _size ) ;
				canvas.getContext().release() ;

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
			public MalletTexture.Meta createMeta( final String _path )
			{
				return textures.getMeta( _path ) ;
			}
		} ) ;

		DrawAssist.setAssist( new DrawAssist.Assist()
		{
			public Draw amendShape( final Draw _draw, final Shape _shape )
			{
				( ( GLDrawData )_draw ).setDrawShape( _shape ) ;
				return _draw ;
			}

			public Draw amendTexture( final Draw _draw, final MalletTexture _texture )
			{
				( ( GLDrawData )_draw ).addTexture( _texture ) ;
				return _draw ;
			}

			public Draw removeTexture( final Draw _draw, final MalletTexture _texture )
			{
				( ( GLDrawData )_draw ).removeTexture( _texture ) ;
				return _draw ;
			}

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

			public Draw amendRotate( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setRotation( _x, _y, _z ) ;
				return _draw ;
			}

			public Draw amendScale( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setScale( _x, _y, _z ) ;
				return _draw ;
			}

			public Draw amendPosition( final Draw _draw, final float _x, final float _y, final float _z )
			{
				( ( GLDrawData )_draw ).setPosition( _x, _y, _z ) ;
				return _draw ;
			}

			public Draw amendText( final Draw _draw, final StringBuilder _text )
			{
				( ( GLDrawData )_draw ).setText( _text ) ;
				return _draw ;
			}

			public Draw amendUI( final Draw _draw, final boolean _ui )
			{
				( ( GLDrawData )_draw ).setUI( _ui ) ;
				return _draw ;
			}

			public Draw amendColour( final Draw _draw, final MalletColour _colour )
			{
				( ( GLDrawData )_draw ).setColour( _colour ) ;
				return _draw ;
			}

			public Draw amendOrder( final Draw _draw, final int _order )
			{
				( ( GLDrawData )_draw ).setOrder( _order ) ;
				return _draw ;
			}

			public Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation )
			{
				( ( GLDrawData )_draw ).setInterpolationMode( _interpolation ) ;
				return _draw ;
			}

			public Draw amendUpdateType( final Draw _draw, final UpdateType _type )
			{
				( ( GLDrawData )_draw ).setUpdateType( _type ) ;
				return _draw ;
			}

			public Draw attachProgram( final Draw _draw, final String _key )
			{
				( ( GLDrawData )_draw ).setDrawProgram( programs.get( _key ) ) ;
				return _draw ;
			}

			public Draw forceUpdate( final Draw _draw )
			{
				( ( GLDrawData )_draw ).forceUpdate() ;
				return _draw ;
			}

			public Shape getDrawShape( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getDrawShape() ;
			}

			public int getTextureSize( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getMalletTextures().size() ;
			}

			public MalletTexture getTexture( final Draw _draw, final int _index )
			{
				return ( ( GLDrawData )_draw ).getMalletTexture( _index ) ;
			}

			public void clearTextures( final Draw _draw )
			{
				( ( GLDrawData )_draw ).clearTextures() ;
			}
			
			public Vector3 getRotate( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getRotation() ;
			}

			public Vector3 getScale( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getScale() ;
			}

			public Vector3 getPosition( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getPosition() ;
			}

			public Vector3 getOffset( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getOffset() ;
			}

			public StringBuilder getText( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getText() ;
			}

			public MalletColour getColour( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getColour() ;
			}

			public boolean isUI( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).isUI() ;
			}

			public Draw createTextDraw( final StringBuilder _text,
										final MalletFont _font,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order )
			{
				final GLDrawData draw = ( GLDrawData )createDraw( _position, _offset, _rotation, _scale, _order ) ;
				attachProgram( draw, "SIMPLE_FONT" ) ;
				draw.setText( _text ) ;
				draw.setFont( _font ) ;
				return draw ;
			}

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

		CameraAssist.setAssist( new CameraAssist.Assist()
		{
			public Camera getCamera()
			{
				return camera ;
			}

			public Camera amendOrthographic( final Camera _camera,
											 final float _top,
											 final float _bottom,
											 final float _left,
											 final float _right,
											 final float _near,
											 final float _far )
			{
				final float invZ = 1.0f / ( _far - _near ) ;
				final float invY = 1.0f / ( _top - _bottom ) ;
				final float invX = 1.0f / ( _right - _left ) ;

				final Matrix4 proj = ( ( BasicCamera )_camera ).getProjection() ;
				proj.set( 2.0f * invX, 0.0f,        0.0f,         ( -( _right + _left ) * invX ),
						  0.0f,        2.0f * invY, 0.0f,         ( -( _top + _bottom ) * invY ),
						  0.0f,        0.0f,        -2.0f * invZ, ( -( _far + _near ) * invZ ),
						  0.0f,        0.0f,        0.0f,         1.0f ) ;
				return _camera ;
			}

			public Camera amendPosition( final Camera _camera, final float _x, final float _y, final float _z )
			{
				( ( BasicCamera )_camera ).setPosition( _x, _y, _z ) ;
				return camera ;
			}

			public Camera amendRotation( final Camera _camera, final float _x, final float _y, final float _z )
			{
				( ( BasicCamera )_camera ).setRotation( _x, _y, _z ) ;
				return camera ;
			}

			public Camera amendScale( final Camera _camera, final float _x, final float _y, final float _z )
			{
				( ( BasicCamera )_camera ).setScale( _x, _y, _z ) ;
				return camera ;
			}

			public boolean getPosition( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( ( ( BasicCamera )_camera ).getPosition() ) ;
				return true ;
			}

			public boolean getRotation( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( ( ( BasicCamera )_camera ).getRotation() ) ;
				return true ;
			}

			public boolean getScale( final Camera _camera, final Vector3 _populate )
			{
				_populate.setXYZ( ( ( BasicCamera )_camera ).getScale() ) ;
				return true ;
			}
		} ) ;
	}

	@Override
	public DrawData.DrawInterface getBasicDraw()
	{
		return new DrawData.DrawInterface<GLDrawData>()
		{
			public void draw( final GLDrawData _data )
			{
				if( _data.toUpdate() == false &&
					_data.getUpdateType() == UpdateType.ON_DEMAND )
				{
					return ;
				}

				final ArrayList<MalletTexture> malletTextures = _data.getMalletTextures() ;
				if( malletTextures.isEmpty() == false )
				{
					final ArrayList<Texture<GLImage>> glTextures = _data.getGLTextures() ;
					if( glTextures.isEmpty() == true )
					{
						if( loadTexture( _data ) == false )
						{
							//Logger.println( "GLRenderer - Render Data for non-existent texture", Logger.Verbosity.MINOR ) ;
							_data.forceUpdate() ;
							return ;
						}
					}
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

				uploader.upload( gl, _data ) ;
			}
		} ;
	}

	@Override
	public DrawData.DrawInterface getTextDraw()
	{
		return new DrawData.DrawInterface<GLDrawData>()
		{
			public void draw( final GLDrawData _data )
			{
				if( _data.toUpdate() == false &&
					_data.getUpdateType() == UpdateType.ON_DEMAND )
				{
					return ;
				}

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
				if( fm == null )
				{
					return ;
				}

				final ArrayList<Texture<GLImage>> textures = _data.getGLTextures() ;
				if( textures.isEmpty() == true )
				{
					textures.add( fm.getTexture() ) ;
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

				uploader.upload( gl, _data ) ;
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
				uploader.remove( gl, _data ) ;
				_data.unregister() ;
			}
		} ;
	}
	
	@Override
	public void setRenderDimensions( final int _width, final int _height )
	{
		super.setRenderDimensions( _width, _height ) ;

		canvas.getContext().makeCurrent() ;
		resize() ;
		canvas.getContext().release() ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		int dimX = _width ;
		int dimY = _height ;

		if( GlobalConfig.getBoolean( "FULLSCREEN", false ) == false )
		{
			// Need to take into account decorated border 
			// when not in fullscreen mode.
			final JFrame temp = new JFrame() ;
			temp.pack() ;

			final Insets insets = temp.getInsets() ;
			dimX += insets.left + insets.right ;
			dimY += insets.top + insets.bottom ;
		}

		final Dimension dim = new Dimension( dimX, dimY ) ;
		frame.setMinimumSize( dim ) ;
		frame.setSize( dim ) ;
		frame.validate() ;

		super.setDisplayDimensions( _width, _height ) ;
		canvas.setSize( _width, _height ) ;

		canvas.getContext().makeCurrent() ;
		resize() ;
		canvas.getContext().release() ;
	}

	@Override
	public void init( GLAutoDrawable _drawable )
	{
		System.out.println( "GL Contex initialised.." ) ;
		gl = _drawable.getGL().getGL3() ;

		gl.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		//GLRenderer.handleError( "VSync: ", gl ) ;

		gl.glPrimitiveRestartIndex( GLGeometryUploader.PRIMITIVE_RESTART_INDEX ) ;

		gl.glEnable( GL.GL_CULL_FACE ) ;
		gl.glCullFace( GL.GL_BACK ) ;  
		gl.glFrontFace( GL.GL_CCW ) ;

		resize() ;

		System.out.println( "Building default shaders.." ) ;
		{
			final GLProgram program = programs.get( "SIMPLE_TEXTURE", "base/shaders/desktop/simple_texture.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_FONT", "base/shaders/desktop/simple_font.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_GEOMETRY", "base/shaders/desktop/simple_geometry.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_STENCIL", "base/shaders/desktop/simple_stencil.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}
		System.out.println( "Shaders built.." ) ;
	}

	public void setViewMode( final int _mode )
	{
		viewMode = _mode ;
	}

	public void hookToWindow( final JFrame _frame )
	{
		frame = _frame ;
		frame.createBufferStrategy( 1 ) ;
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
		frame.setIgnoreRepaint( true ) ;

		frame.add( canvas ) ;

		final Vector2 display = getRenderInfo().getDisplayDimensions() ;
		frame.setSize( ( int )display.x, ( int )display.y ) ;
		frame.setMinimumSize( new Dimension( ( int )display.x, ( int )display.y ) ) ;
		
		if( GlobalConfig.getBoolean( "FULLSCREEN", false ) == true )
		{
			frame.setAlwaysOnTop( true ) ;
			frame.setUndecorated( true ) ;
		}

		frame.pack() ;
		frame.validate() ;
		frame.setVisible( true ) ;

		draw( 0.0f ) ;
	}

	protected void resize()
	{
		switch( viewMode )
		{
			case PERSPECTIVE_MODE  : System.out.println( "Perspective Mode currently not implemented.." ) ; break ;
			case ORTHOGRAPHIC_MODE : 
			default                :
			{
				final Vector2 renderDimensions = getRenderInfo().getRenderDimensions() ;
				CameraAssist.amendOrthographic( camera, 0.0f, renderDimensions.y, 0.0f, renderDimensions.x, -1000.0f, 1000.0f ) ;
				break ;
			}
		}

		final Vector2 displayDimensions = getRenderInfo().getScaledRenderDimensions() ;
		final Vector2 screenOffset = getRenderInfo().getScreenOffset() ;
		gl.glViewport( ( int )screenOffset.x, ( int )screenOffset.y, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;
		//GLRenderer.handleError( "Viewport: ", gl ) ;
	}

	@Override
	public void reshape( GLAutoDrawable _drawable, int _x, int _y, int _width, int _height )
	{
		super.setDisplayDimensions( _width, _height ) ;
		resize() ;
	}

	@Override
	public void dispose( GLAutoDrawable _drawable ) {}

	@Override
	public void updateState( final float _dt )
	{
		super.updateState( _dt ) ;

		final Vector3 cam = camera.getPosition() ;
		oldCamera.setPosition( cam.x, cam.y, cam.z ) ;
	}

	public void draw( final float _dt )
	{
		++renderIter ;
		drawDT = _dt ;

		canvas.display() ;
	}

	@Override
	public void display( GLAutoDrawable _drawable )
	{
		gl = _drawable.getGL().getGL3() ;
		//GLRenderer.handleError( "Previous: ", gl ) ;
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT ) ;	//GLRenderer.handleError( "Clear Buffers: ", gl ) ;
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;						//GLRenderer.handleError( "Clear Colour: ", gl ) ;

		controller.update() ;

		// Calculate the current Camera Position based 
		// on oldCameraPosition and future cameraPosition
		calculateInterpolatedPosition( oldCamera.getPosition(), camera.getPosition(), pos ) ;
		
		final Vector3 scale = camera.getScale() ;
		final Vector2 half = getRenderInfo().getHalfRenderDimensions() ;

		worldMatrix.setIdentity() ;
		worldMatrix.translate( half.x, half.y, 0.0f ) ;
		worldMatrix.scale( scale.x, scale.y, scale.z ) ;
		worldMatrix.translate( -pos.x, -pos.y, 0.0f ) ;

		render() ;
		canvas.swapBuffers() ;
	}

	protected void render()
	{
		state.draw( ( int )( updateDT / drawDT ), renderIter ) ;

		final Matrix4 worldProjection = matrixCache.get() ;
		Matrix4.multiply( camera.getProjection(), worldMatrix, worldProjection ) ;

		final Matrix4 uiProjection = matrixCache.get() ;
		Matrix4.multiply( camera.getProjection(), uiMatrix, uiProjection ) ;

		uploader.draw( gl, worldProjection, uiProjection ) ;

		matrixCache.reclaim( worldProjection ) ;
		matrixCache.reclaim( uiProjection ) ;
	}

	/**
		Remove resources that are not being used.
		Does not remove resources that are still 
		flagged for use.
	*/
	@Override
	public void clean()
	{
		uploader.clean() ;
		programs.clean() ;
		textures.clean() ;
		fontManager.clean() ;
	}

	public static GLCanvas getCanvas()
	{
		return canvas ;
	}

	private boolean loadTexture( final GLDrawData _data )
	{
		final ArrayList<MalletTexture> mltTextures = _data.getMalletTextures() ;
		final ArrayList<Texture<GLImage>> glTextures = _data.getGLTextures() ;

		for( final MalletTexture texture : mltTextures )
		{
			//System.out.println( "Load: " + texture.getPath() ) ;
			final Texture<GLImage> glTexture = textures.get( texture.getPath() ) ;
			if( glTexture == null )
			{
				return false ;
			}

			glTextures.add( glTexture ) ;
		}

		return true ;
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