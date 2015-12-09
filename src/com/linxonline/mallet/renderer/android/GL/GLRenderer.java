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

public class GLRenderer extends Basic2DRender
{
	private static final MalletColour WHITE = MalletColour.white() ;
	private static final MalletColour BLACK = MalletColour.black() ;

	private static final MalletColour RED   = MalletColour.red() ;
	private static final MalletColour GREEN = MalletColour.green() ;
	private static final MalletColour BLUE  = MalletColour.blue() ;

	public static final int ORTHOGRAPHIC_MODE = 1 ;
	public static final int PERSPECTIVE_MODE = 2 ;

	protected static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;
	protected static int DEFAULT_LINEWIDTH = 50 ;								// Is set in resize to the width of render dimensions

	protected final static GLGeometryUploader uploader = new GLGeometryUploader( 10000, 10000 ) ;
	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final GLTextureManager textures = new GLTextureManager() ;
	protected final GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLRenderData> renderCache = new ObjectCache<GLRenderData>( GLRenderData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final Matrix4 modelViewProjectionMatrix = matrixCache.get() ; 	// Combined Model View and Projection Matrix
	protected final Matrix4 uiMatrix = matrixCache.get() ;						// Used for rendering GUI elements not impacted by World/Camera position
	protected final Matrix4 worldMatrix = matrixCache.get() ;					// Used for moving the camera around the world

	protected Vector2 pos = new Vector2() ;

	protected Vector3 oldCameraPosition = new Vector3() ;
	protected Vector3 cameraPosition = new Vector3() ;

	protected DrawInterface<GLRenderData> drawShape = null ;
	protected DrawInterface<GLRenderData> drawTexture = null ;
	protected DrawInterface<GLRenderData> drawText = null ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;

	public GLRenderer()
	{
		initDrawCalls() ;
		initAssist() ;
	}

	@Override
	public void start()
	{
		Logger.println( "Starting renderer..", Logger.Verbosity.NORMAL ) ;
		initGraphics() ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		clear() ;							// Clear the contents being rendered

		textures.shutdown() ;				// We'll loose all texture and font resources
		fontManager.shutdown() ;
		programs.shutdown() ;
	}

	/**
		It's possible for the OpenGL Context 
		to be lost on Android devices.
		We need to remove previous references to 
		OpenGL resources and reload them.
	*/
	public void recover()
	{
		programs.shutdown() ;
		textures.shutdown() ;			// Clear all Texture Data and reload everything upon rendering
		fontManager.recover() ;
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
					public int stringWidth( final String _text )
					{
						return fontMap.stringWidth( _text ) ;
					}
				} ;
			}
		} ) ;

		TextureAssist.setAssist( new TextureAssist.Assist()
		{
			public MalletTexture.Meta create( final String _path )
			{
				return textures.getMeta( _path ) ;
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

	private void initDrawCalls()
	{
		drawShape = new DrawInterface<GLRenderData>()
		{
			public void draw( final GLRenderData _data, final Vector2 _position ) 
			{
				if( _data.toUpdate() == false &&
					_data.getUpdateType() == DrawRequestType.ON_DEMAND )
				{
					return ;
				}

				final float rotation = _data.getRotation() ;
				final Vector2 offset = _data.getOffset() ;
				final boolean isGUI = _data.isUI() ;

				final Vector3 clipPosition = _data.getClipPosition() ;
				final Vector3 clipOffset   = _data.getClipOffset() ;
				if( clipPosition != null && clipOffset != null )
				{
					final Matrix4 clipMatrix = _data.getClipMatrix() ;
					clipMatrix.setIdentity() ;

					clipMatrix.translate( clipPosition.x, clipPosition.y, clipPosition.z ) ;
					clipMatrix.translate( clipOffset.x, clipOffset.y, clipOffset.z ) ;
				}

				final Matrix4 positionMatrix = _data.getPositionMatrix() ;
				positionMatrix.setIdentity() ;

				positionMatrix.translate( _position.x, _position.y, 0.0f ) ;
				positionMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
				positionMatrix.translate( offset.x, offset.y, 0.0f ) ;

				uploader.upload( _data ) ;
			}
		} ;

		drawTexture = new DrawInterface<GLRenderData>()
		{
			public void draw( final GLRenderData _data, final Vector2 _position ) 
			{
				if( _data.toUpdate() == false &&
					_data.getUpdateType() == DrawRequestType.ON_DEMAND )
				{
					return ;
				}

				Texture<GLImage> texture = _data.getTexture() ;
				if( texture == null )
				{
					texture = loadTexture( _data ) ;
					if( texture == null )
					{
						//Logger.println( "GLRenderer - Render Data for non-existent texture: " + _data.getID(), Logger.Verbosity.MINOR ) ;
						return ;
					}
				}

				final float rotation = _data.getRotation() ;
				final Vector2 offset = _data.getOffset() ;
				final boolean isGUI = _data.isUI() ;

				final Vector3 clipPosition = _data.getClipPosition() ;
				final Vector3 clipOffset   = _data.getClipOffset() ;
				if( clipPosition != null && clipOffset != null )
				{
					final Matrix4 clipMatrix = _data.getClipMatrix() ;
					clipMatrix.setIdentity() ;

					clipMatrix.translate( clipPosition.x, clipPosition.y, clipPosition.z ) ;
					clipMatrix.translate( clipOffset.x, clipOffset.y, clipOffset.z ) ;
				}

				final Matrix4 positionMatrix = _data.getPositionMatrix() ;
				positionMatrix.setIdentity() ;

				positionMatrix.translate( _position.x, _position.y, 0.0f ) ;
				positionMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
				positionMatrix.translate( offset.x, offset.y, 0.0f ) ;

				uploader.upload( _data ) ;
			}
		} ;

		drawText = new DrawInterface<GLRenderData>()
		{
			public void draw( final GLRenderData _data, final Vector2 _position ) 
			{
				if( _data.toUpdate() == false &&
					_data.getUpdateType() == DrawRequestType.ON_DEMAND )
				{
					return ;
				}

				final String text = _data.getText() ;
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

				_data.setTexture( fm.getTexture() ) ;

				final int height = fm.getHeight() ;
				final int lineWidth = _data.getLineWidth() + ( int )_position.x ;
				String[] words = _data.getWords() ;
				if( words == null )
				{
					words = optimiseText( fm, text, _position, lineWidth ) ;
					_data.setWords( words ) ;
				}

				final MalletColour colour = _data.getColour() ;
				final int alignment = _data.getTextAlignment() ;
				final float rotation = _data.getRotation() ;
				final Vector2 offset = _data.getOffset() ;
				final boolean isGUI = _data.isUI() ;

				final Matrix4 clipMatrix = _data.getClipMatrix() ;
				if( clipMatrix != null )
				{
					final Vector3 clipPosition = _data.getClipPosition() ;
					final Vector3 clipOffset   = _data.getClipOffset() ;
					clipMatrix.setIdentity() ;

					clipMatrix.translate( clipPosition.x, clipPosition.y, clipPosition.z ) ;
					clipMatrix.translate( clipOffset.x, clipOffset.y, clipOffset.z ) ;
				}

				final Vector2 currentPos = new Vector2( _position ) ;

				setTextAlignment( alignment, currentPos, fm.stringWidth( words[0] ) ) ;
				final Matrix4 positionMatrix = _data.getPositionMatrix() ;
				positionMatrix.setIdentity() ;

				positionMatrix.translate( _position.x, _position.y, 0.0f ) ;
				positionMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
				positionMatrix.translate( offset.x, offset.y, 0.0f ) ;

				_data.setShape( fm.getGlyphWithChar( ' ' ).shape ) ;

				uploader.upload( _data ) ;
			}

			private String[] optimiseText( final GLFontMap _fm, final String _text, final Vector2 _position, final int _lineWidth )
			{
				int length = 0 ;
				float wordWidth = 0.0f ;
				final Vector2 currentPos = new Vector2( _position.x, _position.y ) ;
				String[] words = _text.split( "(?<= )" ) ;

				final ArrayList<String> txt = new ArrayList<String>() ;
				final StringBuilder buffer = new StringBuilder() ;

				String word = null ;
				for( int i = 0; i < words.length; ++i )
				{
					word = words[i] ;
					wordWidth = _fm.stringWidth( word ) ;

					if( word.contains( "<br>" ) == true )
					{
						if( length > 0 )
						{
							txt.add( buffer.toString() ) ;
							buffer.delete( 0, length ) ;
						}
						else
						{
							txt.add( "" ) ;
						}

						currentPos.x = _position.x ;
						continue ;
					}
					else if( currentPos.x + wordWidth >= _lineWidth )
					{
						txt.add( buffer.toString() ) ;
						buffer.delete( 0, length ) ;
						currentPos.x = _position.x ;
					}

					currentPos.x += wordWidth ;
					buffer.append( word ) ;
					length = buffer.length() ;
				}

				if( length > 0 )
				{
					txt.add( buffer.toString() ) ;
					buffer.delete( 0, length ) ;
				}

				words = new String[txt.size()] ;
				words = txt.toArray( words ) ;
				return words ;
			}

			private void setTextAlignment( final int _alignment, final Vector2 _position, final int _wordWidth )
			{
				switch( _alignment )
				{
					case ALIGN_RIGHT  : _position.x -= _wordWidth ;     break ;
					case ALIGN_CENTRE : _position.x -= _wordWidth / 2 ; break ;
					default           : return ;
				}
			}
		} ;
	}

	public void setViewMode( final int _mode )
	{
		viewMode = _mode ;
	}

	protected void resize()
	{
		final Vector2 renderDimensions = renderInfo.getRenderDimensions() ;
		final Vector2 displayDimensions = renderInfo.getScaledRenderDimensions() ;

		switch( viewMode )
		{
			case PERSPECTIVE_MODE  : System.out.println( "Perspective Mode currently not implemented.." ) ; break ;
			case ORTHOGRAPHIC_MODE : 
			default                : constructOrhto2D( modelViewProjectionMatrix, 0.0f, renderDimensions.x, renderDimensions.y, 0.0f ) ; break ;
		}

		final Vector2 screenOffset = renderInfo.getScreenOffset() ;
		GLES30.glViewport( ( int )screenOffset.x, ( int )screenOffset.y, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;
		//GLRenderer.handleError( "Viewport: " ) ;

		DEFAULT_LINEWIDTH = ( int )renderDimensions.x ;
	}

	@Override
	public void updateState( final float _dt )
	{
		super.updateState( _dt ) ;
		oldCameraPosition.setXYZ( cameraPosition ) ;
	}

	public void draw( final float _dt )
	{
		cameraPosition = renderInfo.getCameraPosition() ;
		if( cameraPosition == null )
		{
			System.out.println( "Camera Not Set" ) ;
			return ;
		}

		++renderIter ;
		drawDT = _dt ;
	}

	public void display()
	{
		GLES30.glClear( GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT ) ;
		GLES30.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		updateEvents() ;

		// Calculate the current Camera Position based 
		// on oldCameraPosition and future cameraPosition
		calculateInterpolatedPosition( oldCameraPosition, cameraPosition, pos ) ;
		renderInfo.setCameraZoom( cameraScale.x, cameraScale.y ) ;
		final Vector2 half = renderInfo.getHalfRenderDimensions() ;

		worldMatrix.setIdentity() ;
		worldMatrix.translate( half.x, half.y, 0.0f ) ;
		worldMatrix.scale( cameraScale.x, cameraScale.y, cameraScale.z ) ;
		worldMatrix.translate( -pos.x, -pos.y, 0.0f ) ;

		render() ;
	}

	protected void render()
	{
		state.removeRenderData() ;
		if( state.isStateStable() == true )
		{
			state.draw() ;

			final Matrix4 worldProjection = matrixCache.get() ;
			Matrix4.multiply( modelViewProjectionMatrix, worldMatrix, worldProjection ) ;

			final Matrix4 uiProjection = matrixCache.get() ;
			Matrix4.multiply( modelViewProjectionMatrix, uiMatrix, uiProjection ) ;

			uploader.draw( worldProjection, uiProjection ) ;

			matrixCache.reclaim( worldProjection ) ;
			matrixCache.reclaim( uiProjection ) ;
		}
	}

	private static void constructOrhto2D( final Matrix4 _matrix, final float _left, final float _right, final float _bottom, final float _top )
	{
		final float zNear = -1.0f ;
		final float zFar = 1.0f ;
		final float invZ = 1.0f / ( zFar - zNear ) ;
		final float invY = 1.0f / ( _top - _bottom ) ;
		final float invX = 1.0f / ( _right - _left ) ;

		_matrix.set( 2.0f * invX, 0.0f,        0.0f,         ( -( _right + _left ) * invX ),
					 0.0f,        2.0f * invY, 0.0f,         ( -( _top + _bottom ) * invY ),
					 0.0f,        0.0f,        -2.0f * invZ, ( -( zFar + zNear ) * invZ ),
					 0.0f,        0.0f,        0.0f,         1.0f ) ;
	}

	@Override
	protected void createTexture( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		if( position != null )
		{
			final Shape shape = _draw.<Shape>getObject( "SHAPE", null ) ;
			if( shape != null )
			{
				final GLRenderData data = renderCache.get() ;
				data.set( _draw, drawTexture, DrawRequestType.TEXTURE, _draw.<DrawRequestType>getObject( "UPDATE_TYPE", DrawRequestType.CONTINUOUS ) ) ;
				data.setProgram( programs.get( "SIMPLE_TEXTURE" ) ) ;
				data.setStencilProgram( programs.get( "SIMPLE_STENCIL" ) ) ;

				//Logger.println( "GLRenderer - Create Texture: " + data.id, Logger.Verbosity.MINOR ) ;
				passIDToCallback( data.getID(), _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
				insert( data ) ;
			}
		}
	}

	@Override
	protected void createGeometry( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		if( position != null )
		{
			final Shape shape = _draw.<Shape>getObject( "SHAPE", null ) ;
			if( shape != null )
			{
				final GLRenderData data = renderCache.get() ;
				data.set( _draw, drawShape, DrawRequestType.GEOMETRY, _draw.<DrawRequestType>getObject( "UPDATE_TYPE", DrawRequestType.CONTINUOUS ) ) ;
				data.setProgram( programs.get( "SIMPLE_GEOMETRY" ) ) ;
				data.setStencilProgram( programs.get( "SIMPLE_STENCIL" ) ) ;

				//Logger.println( "GLRenderer - Create Lines: " + data.id, Logger.Verbosity.MINOR ) ;
				passIDToCallback( data.getID(), _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
				insert( data ) ;
			}
		}
	}

	@Override
	protected void createText( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		if( position != null )
		{
			final GLRenderData data = renderCache.get() ;
			data.set( _draw, drawText, DrawRequestType.TEXT, _draw.<DrawRequestType>getObject( "UPDATE_TYPE", DrawRequestType.CONTINUOUS ) ) ;
			data.setProgram( programs.get( "SIMPLE_FONT" ) ) ;
			data.setStencilProgram( programs.get( "SIMPLE_STENCIL" ) ) ;

			//Logger.println( getName() + " - Create Text: " + data.id, Logger.Verbosity.MINOR ) ;
			passIDToCallback( data.getID(), _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			insert( data ) ;
		}
	}

	public void sort() {}

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

	@Override
	public String getName()
	{
		return "GLRenderer" ;
	}

	private Texture loadTexture( final GLRenderData _data )
	{
		final Texture texture = textures.get( _data.data.getString( "FILE", null ) ) ;
		if( texture == null )
		{
			return null ;
		}

		_data.setTexture( texture ) ;
		return texture ;
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

	public static class GLRenderData extends RenderData
	{
		private static int numID = 0 ;

		private final int id = getUniqueID() ;
		private int layer ;
		private Interpolation interpolation ;
		private boolean uiElement ;
		private float rotation ;
		private int lineWidth ;
		private int textAlignment ;

		// Must be nulled when reclaimed by cache
		// User data
		private Vector3 position       = null ;
		private Vector2 offset         = null ;
		private Matrix4 positionMatrix = null ;
		private MalletColour colour    = null ;
		private Shape shape            = null ;
		private Shape clipShape        = null ;
		private String[] words         = null ;
		
		private Vector3 clipPosition   = null ;
		private Vector3 clipOffset     = null ;
		private Matrix4 clipMatrix     = null ;

		// Must be nulled when reclaimed by cache
		// Renderer data
		private GLProgram stencilProgram ;
		private GLProgram program ;
		private Texture texture ;
		
		public GLRenderData()
		{
			super() ;
		}

		@Override
		public void set( final Settings _data, final DrawInterface _call, final DrawRequestType _type, final DrawRequestType _updateType )
		{
			super.set( _data, _call, _type, _updateType ) ;
			data.addInteger( "ID", getID() ) ;
			data.addBoolean( "UPDATE", true ) ;
			updateData() ;
		}

		private void updateData()
		{
			position       = data.<Vector3>getObject( "POSITION", null ) ;
			offset         = data.<Vector2>getObject( "OFFSET", DEFAULT_OFFSET ) ;
			positionMatrix = ( positionMatrix == null ) ? matrixCache.get() : positionMatrix ;

			layer          = data.getInteger( "LAYER", 0 ) ;
			interpolation  = data.<Interpolation>getObject( "INTERPOLATION", Interpolation.LINEAR ) ;
			uiElement      = data.getBoolean( "GUI", false ) ;
			rotation       = ( float )Math.toDegrees( data.getFloat( "ROTATE", 0.0f ) ) ;
			lineWidth      = data.getInteger( "LINEWIDTH", 2 ) ;
			textAlignment  = data.getInteger( "ALIGNMENT", ALIGN_LEFT ) ;
			colour         = data.<MalletColour>getObject( "COLOUR", WHITE ) ;
			shape          = data.<Shape>getObject( "SHAPE", null ) ;
			words          = null ;

			clipShape      = data.<Shape>getObject( "CLIP_SHAPE", null ) ;
			clipPosition   = data.<Vector3>getObject( "CLIP_POSITION", null ) ;
			clipOffset     = data.<Vector3>getObject( "CLIP_OFFSET", null ) ;

			if( clipPosition != null && clipOffset != null )
			{
				clipMatrix = ( clipMatrix == null ) ? matrixCache.get() : clipMatrix ;
			}
		}

		public void setStencilProgram( final GLProgram _program )
		{
			stencilProgram = _program ;
		}

		public void setProgram( final GLProgram _program )
		{
			program = _program ;
		}

		public void setTexture( final Texture _texture )
		{
			texture = _texture ;
		}

		public void setShape( final Shape _shape )
		{
			shape = _shape ;
		}

		public void setWords( final String[] _words )
		{
			words = _words ;
		}

		public int getID()
		{
			return id ;
		}

		public Matrix4 getPositionMatrix()
		{
			return positionMatrix ;
		}

		public Vector3 getPosition()
		{
			return position ;
		}

		public int getLayer()
		{
			return layer ;
		}

		public Interpolation getInterpolation()
		{
			return interpolation ;
		}

		public boolean isUI()
		{
			return uiElement ;
		}

		public float getRotation()
		{
			return rotation ;
		}

		public Vector2 getOffset()
		{
			return offset ;
		}

		public Shape getShape()
		{
			return shape ;
		}

		public Shape getClipShape()
		{
			return clipShape ;
		}

		public MalletColour getColour()
		{
			return colour ;
		}

		public GLProgram getStencilProgram()
		{
			return stencilProgram ;
		}

		public GLProgram getProgram()
		{
			return program ;
		}

		public Texture getTexture()
		{
			return texture ;
		}

		public int getLineWidth()
		{
			return lineWidth ;
		}

		public String[] getWords()
		{
			return words ;
		}

		public Vector3 getClipPosition()
		{
			return clipPosition ;
		}

		public Vector3 getClipOffset()
		{
			return clipOffset ;
		}

		public Matrix4 getClipMatrix()
		{
			return clipMatrix ;
		}

		public boolean toUpdate()
		{
			final boolean update = data.getBoolean( "UPDATE", false ) ;
			if( update == true )
			{
				data.addBoolean( "UPDATE", false ) ;
				updateData() ;
			}

			return update ;
		}

		public int getTextAlignment()
		{
			return textAlignment ;
		}

		public String getText()
		{
			return data.getString( "TEXT", null ) ;
		}

		public MalletFont getFont()
		{
			return data.<MalletFont>getObject( "FONT", null ) ;
		}

		public void copy( final RenderData _data )
		{
			data = _data.data ;
			call = _data.call ;
			type = _data.type ;
			updateType = _data.updateType ;
		}

		public int sortValue()
		{
			return getLayer() ;
		}

		@Override
		public void removeResources()
		{
			uploader.remove( this ) ;
			data.remove( "ID" ) ;
			if( texture != null )
			{
				texture.unregister() ;
			}

			renderCache.reclaim( this ) ;
		}

		@Override
		public void reset()
		{
			if( positionMatrix != null )
			{
				matrixCache.reclaim( positionMatrix ) ;
				positionMatrix = null ;
			}

			position = null ;
			offset = null ;
			colour = null ;
			shape = null ;
			clipShape = null ;
			clipPosition = null ;
			clipOffset = null ;

			if( clipMatrix != null )
			{
				matrixCache.reclaim( clipMatrix ) ;
				clipMatrix = null ;
			}

			words = null ;
			program = null ;
			stencilProgram = null ;
			texture = null ;	
			super.reset() ;
		}

		private static int getUniqueID()
		{
			return numID++ ;
		}
	}
}