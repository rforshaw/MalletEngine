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

	protected final static GLGeometryUploader uploader = new GLGeometryUploader( 1000, 1000 ) ;
	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final GLTextureManager textures = new GLTextureManager() ;
	protected final GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLRenderData> renderCache = new ObjectCache<GLRenderData>( GLRenderData.class ) ;

	protected final ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
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

	protected final int[] textureID = new int[1] ;
	protected final int[] indexID = new int[1] ;
	protected final int[] bufferID = new int[1] ;

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
		// Reset ID's so they aren't refering a potentially 
		// destroyed resource.
		textureID[0] = -1 ;
		indexID[0] = -1 ;
		bufferID[0] = -1 ;

		programs.shutdown() ;
		textures.shutdown() ;			// Clear all Texture Data and reload everything upon rendering
		fontManager.recover() ;
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
				final Shape shape = _data.getShape() ;
				if( shape == null )
				{
					Logger.println( "GLRenderer - Render Data for non-existent shape: " + _data.getID(), Logger.Verbosity.MINOR ) ;
					return ;
				}

				final Model model = _data.getModel() ;
				if( model == null )
				{
					Logger.println( "GLRenderer - Render Data for non-existent model: " + _data.getID(), Logger.Verbosity.MINOR ) ;
					return ;
				}

				final boolean update = _data.toUpdate() ;
				applyClip( _data, update ) ;

				final float rotation = _data.getRotation() ;
				final Vector2 offset = _data.getOffset() ;

				final GLGeometryUploader.GLGeometry geometry = model.getGeometry( GLGeometryUploader.GLGeometry.class ) ;
				final boolean isGUI = _data.isUI() ;
				final int lineWidth = _data.getLineWidth() ;

				final GLProgram program = programs.get( "SIMPLE_GEOMETRY" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				GLES20.glUseProgram( program.id[0] ) ;

				final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

				//System.out.println( "inVertex: " + inVertex ) ;
				//System.out.println( "inColour: " + inColour ) ;

				final VertexAttrib[] attributes =  geometry.getAttributes() ;
				enableVertexAttributes( attributes ) ;

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

					if( update == true )
					{
						uploader.uploadIndex( geometry, shape ) ;
						uploader.uploadVBO( geometry, shape ) ;
					}

					prepareVertexAttributes( attributes, geometry.getStride() ) ;
					GLES20.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GLES20.GL_UNSIGNED_SHORT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				GLES20.glUseProgram( 0 ) ;
				GLES20.glDisable( GLES20.GL_STENCIL_TEST ) ;
				disableVertexAttributes( attributes ) ;
			}
		} ;

		drawTexture = new DrawInterface<GLRenderData>()
		{
			public void draw( final GLRenderData _data, final Vector2 _position ) 
			{
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

				final Model model = _data.getModel() ;
				if( model == null )
				{
					// If we can't map the texture to a plane, then no point in rendering.
					Logger.println( "GLRenderer - Render Data for non-existent model: " + _data.getID(), Logger.Verbosity.MINOR ) ;
					return ;
				}

				final boolean update = _data.toUpdate() ;
				applyClip( _data, update ) ;

				final Shape shape = _data.getShape() ;
				final GLImage image = texture.getImage() ;
				GLRenderer.bindTexture( image.textureIDs, textureID ) ;

				final float rotation = _data.getRotation() ;
				final Vector2 offset = _data.getOffset() ;

				final GLGeometryUploader.GLGeometry geometry = model.getGeometry( GLGeometryUploader.GLGeometry.class ) ;
				final boolean isGUI = _data.isUI() ;

				final GLProgram program = programs.get( "SIMPLE_TEXTURE" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				GLES20.glUseProgram( program.id[0] ) ;
				GLES20.glEnable( GLES20.GL_BLEND ) ;

				final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

				final VertexAttrib[] attributes =  geometry.getAttributes() ;
				enableVertexAttributes( attributes ) ;

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

					// Update the UV co-ordinates of the model
					if( update == true )
					{
						uploader.uploadIndex( geometry, shape ) ;
						uploader.uploadVBO( geometry, shape ) ;
					}

					prepareVertexAttributes( attributes, geometry.getStride() ) ;
					GLES20.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GLES20.GL_UNSIGNED_SHORT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				GLES20.glUseProgram( 0 ) ;
				GLES20.glDisable( GLES20.GL_STENCIL_TEST ) ;
				GLES20.glDisable( GLES20.GL_BLEND ) ;
				disableVertexAttributes( attributes ) ;
			}
		} ;

		drawText = new DrawInterface<GLRenderData>()
		{
			public void draw( final GLRenderData _data, final Vector2 _position ) 
			{
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

				final boolean update = _data.toUpdate() ;
				applyClip( _data, update ) ;

				final GLImage image = fm.getGLImage() ;
				GLRenderer.bindTexture( image.textureIDs, textureID ) ;

				final int height = fm.getHeight() ;
				final int lineWidth = _data.getLineWidth() + ( int )_position.x ;
				String[] words = _data.data.getObject( "WORDS", null ) ;
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
				final Vector2 currentPos = new Vector2( _position ) ;

				final GLProgram program = programs.get( "SIMPLE_FONT" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				GLES20.glUseProgram( program.id[0] ) ;
				GLES20.glEnable( GLES20.GL_BLEND ) ;

				final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

				final GLGeometryUploader.GLGeometry geometry = fm.getGLGeometry() ;
				final VertexAttrib[] attributes =  geometry.getAttributes() ;
				enableVertexAttributes( attributes ) ;

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

					GLRenderer.bindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					prepareVertexAttributes( attributes, geometry.getStride() ) ;

					if( update == true )
					{
						uploader.uploadIndex( geometry, fm.shape ) ;
						uploader.uploadVBO( geometry, fm.shape ) ;
					}

					final int size = words.length ;
					for( int i = 0; i < size; ++i )
					{
						renderText( words[i], fm, newMatrix, inPositionMatrix ) ;
					}

				matrixCache.reclaim( newMatrix ) ;
				disableVertexAttributes( attributes ) ;

				GLES20.glUseProgram( 0 ) ;
				GLES20.glDisable( GLES20.GL_STENCIL_TEST ) ;
				GLES20.glDisable( GLES20.GL_BLEND ) ;
			}

			private void renderText( final String _text, final GLFontMap _fm, final Matrix4 _matrix, final int _matrixHandle )
			{
				final GLGeometryUploader.GLGeometry geometry = _fm.getGLGeometry() ;
				final int length = _text.length() ;

				for( int i = 0; i < length; ++i )
				{
					final GLGlyph glyph = _fm.getGlyphWithChar( _text.charAt( i ) ) ;

					GLES20.glUniformMatrix4fv( _matrixHandle, 1, true, _matrix.matrix, 0 ) ;
					GLES20.glDrawElements( GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, glyph.index * 2 ) ;
					_matrix.translate( glyph.advance, 0.0f, 0.0f ) ;
				}
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

	private static void bindBuffer( final int _type, final int[] _idToBind, final int[] _store )
	{
		if( _store[0] != _idToBind[0] )
		{
			_store[0] = _idToBind[0] ;
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
		if( position != null )
		{
			final Shape shape = _draw.<Shape>getObject( "SHAPE", null ) ;
			if( shape != null )
			{
				final GLRenderData data = renderCache.get() ;
				data.set( _draw, drawTexture, DrawRequestType.TEXTURE ) ;
				data.setModel( GLModelGenerator.genShapeModel( shape ) ) ;
				//Logger.println( "GLRenderer - Create Texture: " + data.id, Logger.Verbosity.MINOR ) ;

				final Shape clipShape = _draw.<Shape>getObject( "CLIP_SHAPE", null ) ;
				if( clipShape != null )
				{
					data.setClipModel( GLModelGenerator.genShapeModel( clipShape ) ) ;
				}

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
				data.set( _draw, drawShape, DrawRequestType.GEOMETRY ) ;
				data.setModel( GLModelGenerator.genShapeModel( shape ) ) ;
				//Logger.println( "GLRenderer - Create Lines: " + data.id, Logger.Verbosity.MINOR ) ;

				final Shape clipShape = _draw.<Shape>getObject( "CLIP_SHAPE", null ) ;
				if( clipShape != null )
				{
					data.setClipModel( GLModelGenerator.genShapeModel( clipShape ) ) ;
				}

				passIDToCallback( data.getID(), _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
				insert( data ) ;
			}
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
			data.set( _draw, drawText, DrawRequestType.TEXT ) ;
			//Logger.println( getName() + " - Create Text: " + data.id, Logger.Verbosity.MINOR ) ;

			final Shape clipShape = _draw.<Shape>getObject( "CLIP_SHAPE", null ) ;
			if( clipShape != null )
			{
				data.setClipModel( GLModelGenerator.genShapeModel( clipShape ) ) ;
			}

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

	private void enableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			GLES20.glEnableVertexAttribArray( att.index ) ;
		}
	}

	private void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( VertexAttrib att : _atts )
		{
			GLES20.glVertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}
	
	private void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			GLES20.glDisableVertexAttribArray( att.index ) ;
		}
	}

	/**
		Called before drawing another element.
		Constructs a stencil buffer that prevents 
		the element from being rendered outside the 
		clip-shape.
		Anything that uses applyClip must disable 
		GL_STENCIL_TEST afterwards.
		Unlike glScissor clip-shapes are affected 
		by worldspace and UI space. Allowing it to be much 
		more flexible to use when limiting where a graphic 
		can render out.
	*/
	private void applyClip( final GLRenderData _data, final boolean _update )
	{
		final Vector3 position = _data.getClipPosition() ;
		final Vector3 offset = _data.getClipOffset() ;
		final Model model = _data.getClipModel() ;
		final Shape shape = _data.getClipShape() ;

		if( position != null && offset != null && model != null && shape != null )
		{
			final GLProgram program = programs.get( "SIMPLE_STENCIL" ) ;
			if( program == null )
			{
				System.out.println( "Program doesn't exist.." ) ;
				return ;
			}

			GLES20.glUseProgram( program.id[0] ) ;

			final GLGeometryUploader.GLGeometry geometry = model.getGeometry( GLGeometryUploader.GLGeometry.class ) ;
			final boolean isGUI = _data.isUI() ;

			final int inMVPMatrix      = GLES20.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
			final int inPositionMatrix = GLES20.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;

			final VertexAttrib[] attributes =  geometry.getAttributes() ;
			enableVertexAttributes( attributes ) ;

				final Matrix4 newMatrix = matrixCache.get() ;
				if( isGUI == true )
				{
					newMatrix.multiply( uiMatrix ) ;
				}
				else
				{
					newMatrix.multiply( worldMatrix ) ;
				}

				newMatrix.translate( position.x, position.y, 0.0f ) ;
				//newMatrix.rotate( rotation, 0.0f, 0.0f, 1.0f ) ;
				newMatrix.translate( offset.x, offset.y, 0.0f ) ;

				GLES20.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;
				GLES20.glUniformMatrix4fv( inPositionMatrix, 1, true, newMatrix.matrix, 0 ) ;

				GLRenderer.bindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
				GLRenderer.bindBuffer( GLES20.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

				if( _update == true )
				{
					uploader.uploadIndex( geometry, shape ) ;
					uploader.uploadVBO( geometry, shape ) ;
				}

				// Don't render the element to the colour buffer
				GLES20.glColorMask( false, false, false, false ) ;
				GLES20.glEnable( GLES20.GL_STENCIL_TEST ) ;

				GLES20.glStencilMask( 0xFF ) ;
				GLES20.glClear( GLES20.GL_STENCIL_BUFFER_BIT ) ;

				GLES20.glStencilFunc( GLES20.GL_NEVER, 1, 0xFF ) ;
				GLES20.glStencilOp( GLES20.GL_REPLACE, GLES20.GL_KEEP, GLES20.GL_KEEP ) ;

				prepareVertexAttributes( attributes, geometry.getStride() ) ;
				GLES20.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GLES20.GL_UNSIGNED_SHORT, 0 ) ;

			matrixCache.reclaim( newMatrix ) ;
			disableVertexAttributes( attributes ) ;

			GLES20.glColorMask( true, true, true, true ) ;		// Re-enable colour buffer
			GLES20.glStencilFunc( GLES20.GL_EQUAL, 1, 1 ) ;
			// Continue drawing scene...
		}
	}

	public static void handleError( final String _txt )
	{
		int error = 0 ;
		while( ( error = GLES20.glGetError() ) != GLES20.GL_NO_ERROR )
		{
			switch( error )
			{
				case GLES20.GL_NO_ERROR                      : break ;
				case GLES20.GL_INVALID_ENUM                  : System.out.println( _txt + ": GL_INVALID_ENUM" ) ; break ;
				case GLES20.GL_INVALID_VALUE                 : System.out.println( _txt + ": GL_INVALID_VALUE" ) ; break ;
				case GLES20.GL_INVALID_OPERATION             : System.out.println( _txt + ": GL_INVALID_OPERATION" ) ; break ;
				case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION : System.out.println( _txt + ": GL_INVALID_FRAMEBUFFER_OPERATION" ) ; break ;
				case GLES20.GL_OUT_OF_MEMORY                 : System.out.println( _txt + ": GL_OUT_OF_MEMORY" ) ; break ;
				//case GLES20.GL_STACK_UNDERFLOW               : System.out.println( _txt + ": GL_STACK_UNDERFLOW" ) ; break ;
				//case GLES20.GL_STACK_OVERFLOW                : System.out.println( _txt + ": GL_STACK_OVERFLOW" ) ; break ;
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
		private MalletColour colour    = null ;
		private Shape shape            = null ;
		private Shape clipShape        = null ;
		private String[] words         = null ;
		private Vector3 clipPosition   = null ;
		private Vector3 clipOffset     = null ;

		// Must be nulled when reclaimed by cache
		// Renderer data
		private Texture texture ;
		private Model model ;
		private Model clipModel ;
		
		public GLRenderData()
		{
			super() ;
		}

		@Override
		public void set( final Settings _data, final DrawInterface _call, final DrawRequestType _type )
		{
			super.set( _data, _call, _type ) ;
			data.addInteger( "ID", getID() ) ;
			data.addBoolean( "UPDATE", true ) ;
			updateData() ;
		}

		private void updateData()
		{
			position       = data.<Vector3>getObject( "POSITION", null ) ;
			offset         = data.<Vector2>getObject( "OFFSET", DEFAULT_OFFSET ) ;
			layer          = data.getInteger( "LAYER", 0 ) ;
			interpolation  = data.<Interpolation>getObject( "INTERPOLATION", Interpolation.LINEAR ) ;
			uiElement      = data.getBoolean( "GUI", false ) ;
			rotation       = ( float )Math.toDegrees( data.getFloat( "ROTATE", 0.0f ) ) ;
			lineWidth      = data.getInteger( "LINEWIDTH", 2 ) ;
			textAlignment  = data.getInteger( "ALIGNMENT", ALIGN_LEFT ) ;
			colour         = data.<MalletColour>getObject( "COLOUR", WHITE ) ;
			shape          = data.<Shape>getObject( "SHAPE", null ) ;
			clipPosition   = data.<Vector3>getObject( "CLIP_POSITION", null ) ;
			clipOffset     = data.<Vector3>getObject( "CLIP_OFFSET", null ) ;
			clipShape      = data.<Shape>getObject( "CLIP_SHAPE", null ) ;
			words          = null ;
		}

		public void setTexture( final Texture _texture )
		{
			texture = _texture ;
		}

		public void setClipModel( final Model _model )
		{
			clipModel = _model ;
		}

		public void setModel( final Model _model )
		{
			model = _model ;
		}

		public void setWords( final String[] _words )
		{
			words = _words ;
		}

		public int getID()
		{
			return id ;
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

		public Model getModel()
		{
			return model ;
		}

		public Model getClipModel()
		{
			return clipModel ;
		}
		
		public MalletColour getColour()
		{
			return colour ;
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
		}

		public int sortValue()
		{
			return getLayer() ;
		}

		@Override
		public void removeResources()
		{
			data.remove( "ID" ) ;
			if( texture != null )
			{
				texture.unregister() ;
			}

			if( model != null )
			{
				model.unregister() ;
				if( type == DrawRequestType.GEOMETRY || type == DrawRequestType.TEXTURE )
				{
					// Geometry Requests are not stored.
					// So must be destroyed explicity.
					model.destroy() ;
				}
			}

			if( clipModel != null )
			{
				clipModel.destroy() ;
			}

			renderCache.reclaim( this ) ;
		}

		@Override
		public void reset()
		{
			position = null ;
			offset = null ;
			colour = null ;
			shape = null ;
			words = null ;
			texture = null ;
			model = null ;
			super.reset() ;
		}

		private static int getUniqueID()
		{
			return numID++ ;
		}
	}
}