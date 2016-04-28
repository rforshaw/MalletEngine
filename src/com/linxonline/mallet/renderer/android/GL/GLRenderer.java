package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

import android.opengl.GLES30 ;
import android.opengl.EGL14 ;

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

import com.linxonline.mallet.renderer.android.GL.GLGeometryUploader.VertexAttrib ;

public class GLRenderer extends BasicRenderer
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
	protected final GLTextureManager textures = new GLTextureManager() ;
	protected final GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLDrawData> renderCache = new ObjectCache<GLDrawData>( GLDrawData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final Matrix4 uiMatrix = matrixCache.get() ;						// Used for rendering GUI elements not impacted by World/Camera position
	protected final Matrix4 worldMatrix = matrixCache.get() ;					// Used for moving the camera around the world

	protected Vector3 pos = new Vector3() ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
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

		uploader.shutdown() ;
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
		uploader.shutdown() ;
		programs.shutdown() ;
		textures.shutdown() ;			// Clear all Texture Data and reload everything upon rendering
		fontManager.recover() ;

		final ArrayList<DrawData> content = state.getActiveDraws() ;
		for( final DrawData draw : content )
		{
			final GLDrawData d = ( GLDrawData )draw ;
			d.getGLTextures().clear() ;
			d.forceUpdate() ; ;
		}
	}

	private void initGraphics()
	{
		//GLES30.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		GLES30.glEnable( GLES30.GL_BLEND ) ;

		GLES30.glEnable( GLES30.GL_CULL_FACE ) ;
		GLES30.glCullFace( GLES30.GL_BACK ) ;  
		GLES30.glFrontFace( GLES30.GL_CCW ) ;

		{
			final GLProgram program = programs.get( "SIMPLE_TEXTURE", "base/shaders/android/simple_texture.jgl" ) ;
			if( GLProgramManager.buildProgram( program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_FONT", "base/shaders/android/simple_font.jgl" ) ;
			if( GLProgramManager.buildProgram( program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_GEOMETRY", "base/shaders/android/simple_geometry.jgl" ) ;
			if( GLProgramManager.buildProgram( program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( program ) ;
			}
		}

		{
			final GLProgram program = programs.get( "SIMPLE_STENCIL", "base/shaders/android/simple_stencil.jgl" ) ;
			if( GLProgramManager.buildProgram( program ) == false )
			{
				System.out.println( "Failed to compile program: " + program.name ) ;
				GLProgramManager.deleteProgram( program ) ;
			}
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

				uploader.upload( _data ) ;
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
				if( fm.fontMap.texture == null )
				{
					// If the font maps texture has yet to be set,
					// generate the texture and bind it with the 
					// current OpenGL context
					fontManager.generateFontGeometry( font ) ;
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

				uploader.upload( _data ) ;
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
				uploader.remove( _data ) ;
				_data.unregister() ;
			}
		} ;
	}

	public void setViewMode( final int _mode )
	{
		viewMode = _mode ;
	}

	protected void resize()
	{
		final Vector2 renderDimensions = getRenderInfo().getRenderDimensions() ;
		final Vector2 displayDimensions = getRenderInfo().getScaledRenderDimensions() ;

		switch( viewMode )
		{
			case PERSPECTIVE_MODE  : System.out.println( "Perspective Mode currently not implemented.." ) ; break ;
			case ORTHOGRAPHIC_MODE : 
			default                :
			{
				CameraAssist.amendOrthographic( camera, 0.0f, renderDimensions.y, 0.0f, renderDimensions.x, -1000.0f, 1000.0f ) ;
				break ;
			}
		}

		final Vector2 screenOffset = getRenderInfo().getScreenOffset() ;
		GLES30.glViewport( ( int )screenOffset.x, ( int )screenOffset.y, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;
		//GLRenderer.handleError( "Viewport: " ) ;
	}

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
	}

	public void display()
	{
		GLES30.glClear( GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT ) ;
		GLES30.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		controller.update() ;

		// Calculate the current Camera Position based 
		// on oldCamera and camera
		calculateInterpolatedPosition( oldCamera.getPosition(), camera.getPosition(), pos ) ;
		final Vector2 half = getRenderInfo().getHalfRenderDimensions() ;

		worldMatrix.setIdentity() ;
		worldMatrix.translate( half.x, half.y, 0.0f ) ;
		worldMatrix.scale( cameraScale.x, cameraScale.y, cameraScale.z ) ;
		worldMatrix.translate( -pos.x, -pos.y, 0.0f ) ;

		render() ;
	}

	protected void render()
	{
		state.draw( ( int )( updateDT / drawDT ), renderIter ) ;

		final Matrix4 worldProjection = matrixCache.get() ;
		Matrix4.multiply( camera.getProjection(), worldMatrix, worldProjection ) ;

		final Matrix4 uiProjection = matrixCache.get() ;
		Matrix4.multiply( camera.getProjection(), uiMatrix, uiProjection ) ;

		uploader.draw( worldProjection, uiProjection ) ;

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