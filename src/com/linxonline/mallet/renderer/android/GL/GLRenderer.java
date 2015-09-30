package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

import android.opengl.GLES20 ;
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

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final GLTextureManager textures = new GLTextureManager() ;
	protected final GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final ObjectCache<GLRenderData> renderCache = new ObjectCache<GLRenderData>( GLRenderData.class ) ;

	protected final ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final Matrix4 modelViewProjectionMatrix = matrixCache.get() ; 	// Combined Model View and Projection Matrix
	protected final Matrix4 uiMatrix = matrixCache.get() ;						// Used for rendering GUI elements not impacted by World/Camera position
	protected final Matrix4 worldMatrix = matrixCache.get() ;					// Used for moving the camera around the world
	
	protected int numID = 0 ;

	private final Vector2 UV1 = new Vector2() ;
	private final Vector2 UV2 = new Vector2( 1.0f, 1.0f ) ;

	protected Vector2 pos = new Vector2() ;

	protected Vector3 oldCameraPosition = new Vector3() ;
	protected Vector3 cameraPosition = new Vector3() ;

	protected DrawInterface drawShape = null ;
	protected DrawInterface drawTexture = null ;
	protected DrawInterface drawText = null ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;

	protected final int[] textureID = new int[1] ;
	protected final int[] indexID = new int[1] ;
	protected final int[] bufferID = new int[1] ;
	
	protected float rotate = 0.0f ;

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
		GLModelGenerator.shutdown() ;		// ModelManager is static and so persists even after a shutdown
	}

	/**
		It's possible for the OpenGL Context 
		to be lost on Android devices.
		We need to remove previous references to 
		OpenGL resources and reload them.
	*/
	public void recover()
	{
		// Reset ID's so they aren't refering a potentially 
		// destroyed resource.
		textureID[0] = -1 ;
		indexID[0] = -1 ;
		bufferID[0] = -1 ;

		programs.shutdown() ;
		textures.shutdown() ;			// Clear all Texture Data and reload everything upon rendering
		GLModelGenerator.shutdown() ;	// Clear all Geometry Data and reload everything upon rendering
		fontManager.recover() ;

		final ArrayList<RenderData> content = state.getContent() ;
		for( final RenderData data : content )
		{
			final Settings draw = data.drawData ;
			draw.remove( "TEXTURE" ) ;
			draw.remove( "MODEL" ) ;
		}
	}

	private void initGraphics()
	{
		//GLES20.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		GLES20.glEnable( GLES20.GL_BLEND ) ;

		GLES20.glEnable( GLES20.GL_CULL_FACE ) ;
		GLES20.glCullFace( GLES20.GL_BACK ) ;  
		GLES20.glFrontFace( GLES20.GL_CCW ) ;

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
		drawShape = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position ) 
			{
				Model model = _settings.getObject( "MODEL", null ) ;
				if( model == null )
				{
					final Shape shape = _settings.<Shape>getObject( "DRAWLINES", null ) ;
					if( shape != null )
					{
						model = GLModelGenerator.genShapeModel( shape ) ;
						_settings.addObject( "MODEL", model ) ;
					}
				}

				if( model == null )
				{
					return ;
				}

				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				final GLGeometry geometry = model.getGeometry( GLGeometry.class ) ;
				final boolean isGUI = _settings.getBoolean( "GUI", false ) ;
				final int lineWidth = _settings.getInteger( "LINEWIDTH", 5 ) ;

				final GLProgram program = programs.get( "SIMPLE_GEOMETRY" ) ;
				if( program != null )
				{
					GLES20.glUseProgram( program.id[0] ) ;
				}

				final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

				//System.out.println( "inVertex: " + inVertex ) ;
				//System.out.println( "inColour: " + inColour ) ;

				GLES20.glEnableVertexAttribArray( GLProgramManager.VERTEX_ARRAY ) ;		// VERTEX ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.COLOUR_ARRAY ) ;		// COLOUR ARRAY

					final Matrix4 newMatrix = matrixCache.get() ;
					if( isGUI == true )
					{
						newMatrix.multiply( uiMatrix ) ;
					}
					else
					{
						newMatrix.multiply( worldMatrix ) ;
					}

					newMatrix.translate( _position.x, _position.y, 0.0f ) ;
					newMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
					newMatrix.translate( offset.x, offset.y, 0.0f ) ;

					GLES20.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;
					GLES20.glUniformMatrix4fv( inPositionMatrix, 1, true, newMatrix.matrix, 0 ) ;
					GLES20.glLineWidth( ( float )lineWidth ) ;

					GLRenderer.bindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					if( _settings.getBoolean( "UPDATE", false ) == true )
					{
						GLModelGenerator.updateShapeModel( model, _settings.<Shape>getObject( "DRAWLINES", null ) ) ;
						GLModelManager.updateVBO( geometry ) ;
						_settings.addObject( "UPDATE", false ) ;
					}

					GLES20.glVertexAttribPointer( GLProgramManager.VERTEX_ARRAY, 3, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.COLOUR_ARRAY, 4, GLES20.GL_UNSIGNED_BYTE, true,  GLGeometry.STRIDE, GLGeometry.COLOUR_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.NORMAL_ARRAY, 3, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

					final short length = ( short )geometry.index.length ;
					GLES20.glDrawElements( geometry.style, length, GLES20.GL_UNSIGNED_SHORT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				GLES20.glUseProgram( 0 ) ;
				GLES20.glDisableVertexAttribArray( GLProgramManager.VERTEX_ARRAY ) ;		// VERTEX ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.COLOUR_ARRAY ) ;		// COLOUR ARRAY
			}
		} ;

		drawTexture = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position ) 
			{
				Texture<GLImage> texture = _settings.getObject( "TEXTURE", null ) ;
				if( texture == null )
				{
					texture = loadTexture( _settings ) ;
					if( texture == null )
					{
						return ;
					}
				}

				final GLImage image = texture.getImage() ;
				GLRenderer.bindTexture( image.textureIDs, textureID ) ;

				final Model model = _settings.getObject( "MODEL", null ) ;
				if( model == null )
				{
					// If we can't map the texture to a plane, then no point in rendering.
					return ;
				}

				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				final GLGeometry geometry = model.getGeometry( GLGeometry.class ) ;
				final boolean isGUI = _settings.getBoolean( "GUI", false ) ;

				final MalletColour colour = _settings.<MalletColour>getObject( "COLOUR", WHITE ) ;
				final Vector2 uv1 = _settings.<Vector2>getObject( "UV1", UV1 ) ;
				final Vector2 uv2 = _settings.<Vector2>getObject( "UV2", UV2 ) ;

				final GLProgram program = programs.get( "SIMPLE_TEXTURE" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				GLES20.glUseProgram( program.id[0] ) ;

				final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

				GLES20.glEnableVertexAttribArray( GLProgramManager.VERTEX_ARRAY ) ;		// VERTEX ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.COLOUR_ARRAY ) ;		// COLOUR ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.TEXTURE_COORD_ARRAY ) ;	// TEXTURE COORD ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.NORMAL_ARRAY ) ;		// NORMAL ARRAY

					final Matrix4 newMatrix = matrixCache.get() ;
					if( isGUI == true )
					{
						newMatrix.multiply( uiMatrix ) ;
					}
					else
					{
						newMatrix.multiply( worldMatrix ) ;
					}

					newMatrix.translate( _position.x, _position.y, 0.0f ) ;
					newMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
					newMatrix.translate( offset.x, offset.y, 0.0f ) ;

					GLES20.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;
					GLES20.glUniformMatrix4fv( inPositionMatrix, 1, true, newMatrix.matrix, 0 ) ;

					GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA ) ;

					GLRenderer.bindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					if( _settings.getBoolean( "UPDATE", false ) == true )
					{
						GLModelGenerator.updatePlaneModelColour( model, GLModelGenerator.getABGR( colour ) ) ;
						_settings.addObject( "UPDATE", false ) ;
					}

					// Texture's share geometry so we must update the VBO's
					// every frame as the uv coordinates can change.
					GLModelGenerator.updatePlaneModelUV( model, uv1, uv2 ) ;
					GLModelManager.updateVBO( geometry ) ;

					GLES20.glVertexAttribPointer( GLProgramManager.VERTEX_ARRAY,        3, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.COLOUR_ARRAY,        4, GLES20.GL_UNSIGNED_BYTE, true,  GLGeometry.STRIDE, GLGeometry.COLOUR_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.TEXTURE_COORD_ARRAY, 2, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.TEXCOORD_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.NORMAL_ARRAY,        3, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

					GLES20.glDrawElements( GLES20.GL_TRIANGLES, geometry.index.length, GLES20.GL_UNSIGNED_SHORT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				GLES20.glDisableVertexAttribArray( GLProgramManager.VERTEX_ARRAY ) ;		// VERTEX ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.COLOUR_ARRAY ) ;		// COLOUR ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.TEXTURE_COORD_ARRAY ) ;	// TEXTURE COORD ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.NORMAL_ARRAY ) ;		// NORMAL ARRAY
				GLES20.glUseProgram( 0 ) ;
			}
		} ;

		drawText = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position ) 
			{
				final String text = _settings.getString( "TEXT", null ) ;
				if( text == null )
				{
					System.out.println( "No Text, set." ) ;
					return ;
				}

				final MalletFont font = _settings.getObject( "FONT", null ) ;
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

				final GLImage image = fm.getGLImage() ;
				GLRenderer.bindTexture( image.textureIDs, textureID ) ;

				final int height = fm.getHeight() ;
				final int lineWidth = _settings.getInteger( "LINEWIDTH", DEFAULT_LINEWIDTH ) + ( int )_position.x ;
				String[] words = _settings.getObject( "WORDS", null ) ;
				if( words == null )
				{
					words = optimiseText( fm, text, _position, lineWidth ) ;
					_settings.addObject( "WORDS", words ) ;
					_settings.addInteger( "TEXTWIDTH", -1 ) ;
				}

				final MalletColour colour = _settings.getObject( "COLOUR", WHITE ) ;
				final int alignment = _settings.getInteger( "ALIGNMENT", ALIGN_LEFT ) ;
				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				final boolean isGUI = _settings.getBoolean( "GUI", false ) ;
				final Vector2 currentPos = new Vector2( _position ) ;

				final GLProgram program = programs.get( "SIMPLE_FONT" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				GLES20.glUseProgram( program.id[0] ) ;

				final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

				GLES20.glEnableVertexAttribArray( GLProgramManager.VERTEX_ARRAY ) ;			// VERTEX ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.COLOUR_ARRAY ) ;			// COLOUR ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.TEXTURE_COORD_ARRAY ) ;	// TEXTURE COORD ARRAY
				GLES20.glEnableVertexAttribArray( GLProgramManager.NORMAL_ARRAY ) ;			// NORMAL ARRAY

					setTextAlignment( alignment, currentPos, fm.stringWidth( words[0] ) ) ;
					final Matrix4 newMatrix = matrixCache.get() ;
					if( isGUI == true )
					{
						newMatrix.multiply( uiMatrix ) ;
					}
					else
					{
						newMatrix.multiply( worldMatrix ) ;
					}

					newMatrix.translate( _position.x, _position.y, 0.0f ) ;
					newMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
					newMatrix.translate( offset.x, offset.y, 0.0f ) ;

					GLES20.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;

					GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA ) ;

					final GLGeometry geometry = fm.getGLGeometry() ;
					GLRenderer.bindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					GLES20.glVertexAttribPointer( GLProgramManager.VERTEX_ARRAY,        3, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.COLOUR_ARRAY,        4, GLES20.GL_UNSIGNED_BYTE, true,  GLGeometry.STRIDE, GLGeometry.COLOUR_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.TEXTURE_COORD_ARRAY, 2, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.TEXCOORD_OFFSET ) ;
					GLES20.glVertexAttribPointer( GLProgramManager.NORMAL_ARRAY,        3, GLES20.GL_FLOAT,         false, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

					if( _settings.getBoolean( "UPDATE", false ) == true )
					{
						GLModelGenerator.updateModelColour( fm.model, GLModelGenerator.getABGR( colour ) ) ;
						GLModelManager.updateVBO( geometry ) ;
						_settings.addObject( "UPDATE", false ) ;
					}

					final int size = words.length ;
					for( int i = 0; i < size; ++i )
					{
						renderText( words[i], fm, newMatrix, inPositionMatrix ) ;
					}

				matrixCache.reclaim( newMatrix ) ;

				GLES20.glDisableVertexAttribArray( GLProgramManager.VERTEX_ARRAY ) ;		// VERTEX ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.COLOUR_ARRAY ) ;		// COLOUR ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.TEXTURE_COORD_ARRAY ) ;	// TEXTURE COORD ARRAY
				GLES20.glDisableVertexAttribArray( GLProgramManager.NORMAL_ARRAY ) ;		// NORMAL ARRAY
				GLES20.glUseProgram( 0 ) ;
			}

			private void renderText( final String _text, final GLFontMap _fm, final Matrix4 _matrix, final int _matrixHandle )
			{
				final Matrix4 transpose = matrixCache.get() ;
			
				final int length = _text.length() ;
				for( int i = 0; i < length; ++i )
				{
					final GLGlyph glyph = _fm.getGlyphWithChar( _text.charAt( i ) ) ;
					GLRenderer.bindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, glyph.index.indexID, indexID ) ;

					GLES20.glUniformMatrix4fv( _matrixHandle, 1, true, _matrix.matrix, 0 ) ;
					GLES20.glDrawElements( GLES20.GL_TRIANGLES, glyph.index.index.length, GLES20.GL_UNSIGNED_SHORT, 0 ) ;
					_matrix.translate( glyph.advance, 0.0f, 0.0f ) ;
				}

				matrixCache.reclaim( transpose ) ;
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
		GLES20.glViewport( ( int )screenOffset.x, ( int )screenOffset.y, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;

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
		GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT ) ;
		GLES20.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

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
			GLES20.glGetIntegerv( GLES20.GL_TEXTURE_BINDING_2D, textureID, 0 ) ;
			GLES20.glGetIntegerv( GLES20.GL_ELEMENT_ARRAY_BUFFER_BINDING, indexID, 0 ) ;
			GLES20.glGetIntegerv( GLES20.GL_ELEMENT_ARRAY_BUFFER_BINDING, bufferID, 0 ) ;

			state.draw() ;
		}
	}

	private static void bindTexture( final int[] _idToBind, final int[] _store )
	{
		if( _store[0] != _idToBind[0] )
		{
			_store[0] = _idToBind[0] ;
			GLES20.glBindTexture( GLES20.GL_TEXTURE_2D, _store[0] ) ;
		}
	}

	private static void bindBuffer( final int _type, final int _idToBind, final int[] _store )
	{
		if( _store[0] != _idToBind )
		{
			_store[0] = _idToBind ;
			GLES20.glBindBuffer( _type, _store[0] ) ;
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
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final GLRenderData data = renderCache.get() ;
			data.set( numID++, DrawRequestType.TEXTURE, _draw, position, layer ) ;
			//Logger.println( "GLRenderer - Create Texture: " + data.id, Logger.Verbosity.MINOR ) ;

			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawTexture ;
			insert( data ) ;
		}
	}

	@Override
	protected void createGeometry( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final GLRenderData data = renderCache.get() ;
			data.set( numID++, DrawRequestType.GEOMETRY, _draw, position, layer ) ;
			//Logger.println( "GLRenderer - Create Lines: " + data.id, Logger.Verbosity.MINOR ) ;

			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawShape ;
			insert( data ) ;
			return ;
		}
	}

	@Override
	protected void createText( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final GLRenderData data = renderCache.get() ;
			data.set( numID++, DrawRequestType.TEXT, _draw, position, layer ) ;
			//Logger.println( getName() + " - Create Text: " + data.id, Logger.Verbosity.MINOR ) ;

			passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
			data.drawCall = drawText ;
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
		textures.clean() ;
		fontManager.clean() ;
		GLModelGenerator.clean() ;
	}

	@Override
	public String getName()
	{
		return "GLRenderer" ;
	}

	private Texture loadTexture( final Settings _draw )
	{
		final Texture texture = textures.get( _draw.getString( "FILE", null ) ) ;
		if( texture == null ) { return null ; }

		final Vector2 fillDim = _draw.getObject( "FILL", null ) ;
		Vector2 dimension = _draw.getObject( "DIM", null ) ;
		if( dimension == null )
		{
			dimension = new Vector2( texture.getWidth(), texture.getHeight() ) ;
		}

		if( fillDim == null )
		{
			final String name = dimension.toString() ;
			_draw.addObject( "MODEL", GLModelGenerator.genPlaneModel( name, dimension ) ) ;
			_draw.addObject( "TEXTURE", texture ) ;
		}
		else
		{
			final Vector2 div = Vector2.divide( fillDim, dimension ) ;
			final String name = fillDim.toString() + dimension.toString() ;
			_draw.addObject( "MODEL", GLModelGenerator.genPlaneModel( name, fillDim, new Vector2( 0.0f, 0.0f ), div ) ) ;
			_draw.addObject( "TEXTURE", texture ) ;
		}

		return texture ;
	}

	public static class GLRenderData extends RenderData
	{
		public GLRenderData()
		{
			super() ;
		}

		public GLRenderData( final int _id,
							 final DrawRequestType _type,
							 final Settings _draw,
							 final Vector3 _position,
							 final int _layer )
		{
			super( _id, _type, _draw, _position, _layer ) ;
		}

		@Override
		public void unregisterResources()
		{
			final Texture texture = drawData.getObject( "TEXTURE", null ) ;
			if( texture != null )
			{
				texture.unregister() ;
				drawData.remove( "TEXTURE" ) ;
			}

			final Model model = drawData.getObject( "MODEL", null ) ;
			if( model != null )
			{
				model.unregister() ;
				if( type == DrawRequestType.GEOMETRY )
				{
					// Geometry Requests are not stored.
					// So must be destroyed explicity.
					model.destroy() ;
					drawData.remove( "MODEL" ) ;
				}
			}
		}
	}
}