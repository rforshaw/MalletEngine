package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;

import org.teavm.jso.* ;
import org.teavm.jso.browser.* ;
import org.teavm.jso.dom.html.* ;
import org.teavm.jso.dom.events.* ;
import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLUniformLocation ;
import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.webgl.WebGLTexture ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.renderer.font.* ;
import com.linxonline.mallet.renderer.texture.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.web.gl.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer<GLWorldState>
{
	public static final int ORTHOGRAPHIC_MODE = 1 ;
	public static final int PERSPECTIVE_MODE  = 2 ;

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLDrawData> renderCache = new ObjectCache<GLDrawData>( GLDrawData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache  = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final static Matrix4 uiMatrix                  = matrixCache.get() ;		// Used for rendering GUI elements not impacted by World/Camera position
	protected final static Matrix4 worldMatrix               = matrixCache.get() ;		// Used for moving the camera around the world

	private final HTMLCanvasElement canvas ;
	private static WebGLRenderingContext gl ;

	protected CameraData defaultCamera = new CameraData( "MAIN" ) ;
	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
		super( new GLWorldState() ) ;

		final HTMLDocument document = HTMLDocument.current();
		canvas = ( HTMLCanvasElement )document.getElementById( "mallet-canvas" ) ;
		gl = ( WebGLRenderingContext )canvas.getContext( "webgl" ) ;

		final GLWorldState worlds = getWorldState() ;
		worlds.setDefault( new GLWorld( "DEFAULT", 0, constructRemoveDelegate() ) ) ;

		defaultCamera.setDrawInterface( getCameraDraw() ) ;
		worlds.addCamera( defaultCamera, null ) ;

		System.out.println( "Building default shaders.." ) ;
		{
			final GLProgram program = programs.get( "SIMPLE_TEXTURE", "base/shaders/web/simple_texture.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_FONT", "base/shaders/web/simple_font.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_GEOMETRY", "base/shaders/web/simple_geometry.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_STENCIL", "base/shaders/web/simple_stencil.jgl" ) ;
			if( GLProgramManager.buildProgram( gl, program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( gl, program ) ;
			}
		}
		System.out.println( "Shaders built.." ) ;
	}

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

		getWorldState().shutdown() ;
		programs.shutdown() ;
		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.shutdown() ;
	}

	private void initGraphics() {}

	@Override
	public void initAssist()
	{
		FontAssist.setFontWrapper( new FontAssist.Assist()
		{
			@Override
			public Font createFont( final String _font, final int _style, final int _size )
			{
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
			public Draw amendTexture( final Draw _draw, final MalletTexture _texture )
			{
				( ( GLDrawData )_draw ).addTexture( _texture ) ;
				return _draw ;
			}

			@Override
			public Draw removeTexture( final Draw _draw, final MalletTexture _texture )
			{
				( ( GLDrawData )_draw ).removeTexture( _texture ) ;
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
			public Draw attachProgram( final Draw _draw, final String _key )
			{
				( ( GLDrawData )_draw ).setDrawProgram( programs.get( _key ) ) ;
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
			public int getTextureSize( final Draw _draw )
			{
				return ( ( GLDrawData )_draw ).getMalletTextures().size() ;
			}

			@Override
			public MalletTexture getTexture( final Draw _draw, final int _index )
			{
				return ( ( GLDrawData )_draw ).getMalletTexture( _index ) ;
			}

			@Override
			public void clearTextures( final Draw _draw )
			{
				( ( GLDrawData )_draw ).clearTextures() ;
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
				//positionMatrix.transpose() ;

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

				gl.viewport( ( int )( offset.x + ( screen.offset.x * scaleRtoD.x ) ),
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

				worldProjection.transpose() ;
				uiProjection.transpose() ;

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
		resize() ;
	}

	@Override
	public void setDisplayDimensions( final int _width, final int _height )
	{
		super.setDisplayDimensions( _width, _height ) ;
		canvas.setWidth( _width ) ;
		canvas.setHeight( _height ) ;
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

	public void draw( final float _dt )
	{
		super.draw( _dt ) ;
		//GLRenderer.handleError( "Previous: ", gl ) ;
		gl.clear( GL3.COLOR_BUFFER_BIT | GL3.DEPTH_BUFFER_BIT ) ;	//GLRenderer.handleError( "Clear Buffers: ", gl ) ;
		gl.clearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;						//GLRenderer.handleError( "Clear Colour: ", gl ) ;

		getEventController().update() ;

		getWorldState().upload( ( int )( updateDT / drawDT ), renderIter ) ;
	
		gl.flush() ;
		gl.finish() ;
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

	public static WebGLRenderingContext getContext()
	{
		return gl ;
	}

	private boolean loadTexture( final GLDrawData _data )
	{
		final ArrayList<MalletTexture> mltTextures = _data.getMalletTextures() ;
		final ArrayList<Texture<GLImage>> glTextures = _data.getGLTextures() ;

		for( final MalletTexture texture : mltTextures )
		{
			final Texture<GLImage> glTexture = textures.get( texture.getPath() ) ;
			if( glTexture == null )
			{
				return false ;
			}

			glTextures.add( glTexture ) ;
		}

		return true ;
	}

	public static void handleError( final String _txt, final WebGLRenderingContext _gl )
	{
		int error = 0 ;
		while( ( error = _gl.getError() ) != GL3.NO_ERROR )
		{
			switch( error )
			{
				case GL3.NO_ERROR                      : break ;
				case GL3.INVALID_ENUM                  : System.out.println( _txt + ": GL_INVALID_ENUM" ) ; break ;
				case GL3.INVALID_VALUE                 : System.out.println( _txt + ": GL_INVALID_VALUE" ) ; break ;
				case GL3.INVALID_OPERATION             : System.out.println( _txt + ": GL_INVALID_OPERATION" ) ; break ;
				case GL3.INVALID_FRAMEBUFFER_OPERATION : System.out.println( _txt + ": GL_INVALID_FRAMEBUFFER_OPERATION" ) ; break ;
				case GL3.OUT_OF_MEMORY                 : System.out.println( _txt + ": GL_OUT_OF_MEMORY" ) ; break ;
				//case GL3.STACK_UNDERFLOW               : System.out.println( _txt + ": GL_STACK_UNDERFLOW" ) ; break ;
				//case GL3.STACK_OVERFLOW                : System.out.println( _txt + ": GL_STACK_OVERFLOW" ) ; break ;
			}
		}
	}
}