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

import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;

import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.web.gl.GLGeometryUploader.VertexAttrib ;

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
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final static ObjectCache<GLRenderData> renderCache = new ObjectCache<GLRenderData>( GLRenderData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final Matrix4 modelViewProjectionMatrix = matrixCache.get() ; 	// Combined Model View and Projection Matrix
	protected final Matrix4 uiMatrix = matrixCache.get() ;						// Used for rendering GUI elements not impacted by World/Camera position
	protected final Matrix4 worldMatrix = matrixCache.get() ;					// Used for moving the camera around the world

	protected Vector2 pos = new Vector2() ;

	protected Vector3 oldCameraPosition = new Vector3() ;
	protected Vector3 cameraPosition = new Vector3() ;

	private final HTMLCanvasElement canvas ;
	private static WebGLRenderingContext gl ;

	protected DrawInterface<GLRenderData> drawShape = null ;
	protected DrawInterface<GLRenderData> drawTexture = null ;
	protected DrawInterface<GLRenderData> drawText = null ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;
	protected float rotate = 0.0f ;

	// Keep track of the last binded ID
	// If the next item to be rendered uses one of the same 
	// ID's then we can avoid the bind call.
	// Note: Caching vboID, results in an exception
	// on some occasions.
	protected final WebGLTexture[] textureID = new WebGLTexture[1] ;
	protected final WebGLBuffer[] indexID = new WebGLBuffer[1] ;
	protected final WebGLBuffer[] bufferID = new WebGLBuffer[1] ;

	public GLRenderer()
	{
		final HTMLDocument document = HTMLDocument.current();
		canvas = ( HTMLCanvasElement )document.getElementById( "mallet-canvas" ) ;
		gl = ( WebGLRenderingContext )canvas.getContext( "webgl" ) ;

		System.out.println( "GL Contex initialised.." ) ;
		//gl.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled

		//gl.enable( GL3.CULL_FACE ) ;
		//gl.cullFace( GL3.BACK ) ;  
		//gl.frontFace( GL3.CCW ) ;

		//resize() ;

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
		initGraphics() ;
		initDrawCalls() ;
		initAssist() ;
	}

	@Override
	public void shutdown()
	{
		Logger.println( "Shutting renderer down..", Logger.Verbosity.NORMAL ) ;
		clear() ;							// Clear the contents being rendered

		programs.shutdown() ;
		textures.shutdown() ;
		fontManager.shutdown() ;
	}

	private void initGraphics()
	{
		/*final GLProfile glProfile = GLProfile.get( GLProfile.GL3 ) ;
		final GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
		capabilities.setStencilBits( 1 ) ;			// Provide ON/OFF Stencil Buffers
		capabilities.setDoubleBuffered( true ) ;

		canvas = new GLJPanel( capabilities ) ;
		canvas.setAutoSwapBufferMode( false ) ;
		canvas.addGLEventListener( this ) ;*/
	}

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
		canvas.setWidth( _width ) ;
		canvas.setHeight( _height ) ;
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

				gl.useProgram( program.id[0] ) ;

				final WebGLUniformLocation inMVPMatrix      = gl.getUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final WebGLUniformLocation inPositionMatrix = gl.getUniformLocation( program.id[0], "inPositionMatrix" ) ;

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
					newMatrix.transpose() ;

					gl.uniformMatrix4fv( inMVPMatrix, false, modelViewProjectionMatrix.matrix ) ;
					gl.uniformMatrix4fv( inPositionMatrix, false, newMatrix.matrix ) ;
					gl.lineWidth( ( float )lineWidth ) ;

					GLRenderer.bindBuffer( gl, GL3.ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( gl, GL3.ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					if( update == true )
					{
						uploader.uploadIndex( gl, geometry, shape ) ;
						uploader.uploadVBO( gl, geometry, shape ) ;
					}

					prepareVertexAttributes( attributes, geometry.getStride() ) ;
					gl.drawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.UNSIGNED_SHORT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				gl.useProgram( null ) ;
				gl.disable( GL3.STENCIL_TEST ) ;
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
				GLRenderer.bindTexture( gl, image.textureIDs, textureID ) ;

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

				gl.useProgram( program.id[0] ) ;
				gl.enable( GL3.BLEND ) ;

				final WebGLUniformLocation inMVPMatrix      = gl.getUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final WebGLUniformLocation inPositionMatrix = gl.getUniformLocation( program.id[0], "inPositionMatrix" ) ;

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
					newMatrix.transpose() ;

					gl.uniformMatrix4fv( inMVPMatrix, false, modelViewProjectionMatrix.matrix ) ;
					gl.uniformMatrix4fv( inPositionMatrix, false, newMatrix.matrix ) ;

					gl.blendFunc( GL3.SRC_ALPHA, GL3.ONE_MINUS_SRC_ALPHA ) ;

					GLRenderer.bindBuffer( gl, GL3.ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( gl, GL3.ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					// Update the UV co-ordinates of the model
					if( update == true )
					{
						uploader.uploadIndex( gl, geometry, shape ) ;
						uploader.uploadVBO( gl, geometry, shape ) ;
					}

					//System.out.println( "Draw Texture.." ) ;
					prepareVertexAttributes( attributes, geometry.getStride() ) ;
					gl.drawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.UNSIGNED_SHORT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;
				disableVertexAttributes( attributes ) ;

				gl.useProgram( null ) ;
				gl.disable( GL3.STENCIL_TEST ) ;
				gl.disable( GL3.BLEND ) ;
			}
		} ;

		drawText = new DrawInterface<GLRenderData>()
		{
			public void draw( final GLRenderData _data, final Vector2 _position ) 
			{
				/*final String text = _data.getText() ;
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

				final boolean update = _data.toUpdate() ;
				applyClip( _data, update ) ;

				final GLImage image = fm.getGLImage() ;
				GLRenderer.bindTexture( gl, image.textureIDs, textureID ) ;

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
				final Vector2 currentPos = new Vector2( _position ) ;

				final GLProgram program = programs.get( "SIMPLE_FONT" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				gl.useProgram( program.id[0] ) ;
				gl.enable( GL3.BLEND ) ;

				final WebGLUniformLocation inMVPMatrix      = gl.getUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final WebGLUniformLocation inPositionMatrix = gl.getUniformLocation( program.id[0], "inPositionMatrix" ) ;

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

					gl.uniformMatrix4fv( inMVPMatrix, true, modelViewProjectionMatrix.matrix ) ;

					gl.blendFunc( GL3.SRC_ALPHA, GL3.ONE_MINUS_SRC_ALPHA ) ;
					//gl.glAlphaFunc( GL.GL_GREATER, 0.5f ) ;

					GLRenderer.bindBuffer( gl, GL3.ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( gl, GL3.ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					prepareVertexAttributes( attributes, geometry.getStride() ) ;

					if( update == true )
					{
						uploader.uploadIndex( gl, geometry, fm.shape ) ;
						uploader.uploadVBO( gl, geometry, fm.shape ) ;
					}

					final int size = words.length ;
					for( int i = 0; i < size; ++i )
					{
						renderText( words[i], fm, newMatrix, inPositionMatrix ) ;
					}

				matrixCache.reclaim( newMatrix ) ;
				disableVertexAttributes( attributes ) ;

				gl.useProgram( null ) ;
				gl.disable( GL3.STENCIL_TEST ) ;
				gl.disable( GL3.BLEND ) ;*/
			}

			private void renderText( final String _text, final GLFontMap _fm, final Matrix4 _matrix, final WebGLUniformLocation _matrixHandle )
			{
				final GLGeometryUploader.GLGeometry geometry = _fm.getGLGeometry() ;
				final int length = _text.length() ;

				for( int i = 0; i < length; ++i )
				{
					final GLGlyph glyph = _fm.getGlyphWithChar( _text.charAt( i ) ) ;
					gl.uniformMatrix4fv( _matrixHandle, true, _matrix.matrix ) ;

					gl.drawElements( GL3.TRIANGLES, 6, GL3.UNSIGNED_INT, glyph.index * 4 ) ;
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
		gl.viewport( ( int )screenOffset.x, ( int )screenOffset.y, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;

		DEFAULT_LINEWIDTH = 5 ;//( int )renderDimensions.x ;
	}

	@Override
	public void updateState( final float _dt )
	{
		super.updateState( _dt ) ;
		oldCameraPosition.setXYZ( cameraPosition ) ;
	}

	private boolean wait ; 
	
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

		gl.clear( GL3.COLOR_BUFFER_BIT | GL3.DEPTH_BUFFER_BIT ) ;
		gl.clearColor( 0, 0, 0, 1 ) ;

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

		gl.flush() ;
		gl.finish() ;
	} 

	protected void render()
	{
		state.removeRenderData() ;
		if( state.isStateStable() == true )
		{
			//gl.getIntegerv( GL3.TEXTURE_BINDING_2D, textureID, 0 ) ;
			//gl.getIntegerv( GL3.ELEMENT_ARRAY_BUFFER_BINDING, indexID, 0 ) ;
			//gl.getIntegerv( GL3.ELEMENT_ARRAY_BUFFER_BINDING, bufferID, 0 ) ;

			state.draw() ;
		}
	}

	private static void bindTexture( final WebGLRenderingContext _gl, final WebGLTexture[] _idToBind, final WebGLTexture[] _store )
	{
		if( _store[0] != _idToBind[0] )
		{
			_store[0] = _idToBind[0] ;
			_gl.activeTexture( GL3.TEXTURE0 + 0 ) ;
			_gl.bindTexture( GL3.TEXTURE_2D, _store[0] ) ;
		}
	}

	private static void bindBuffer( final WebGLRenderingContext _gl, final int _type, final WebGLBuffer[] _idToBind, final WebGLBuffer[] _store )
	{
		if( _store[0] != _idToBind[0] )
		{
			_store[0] = _idToBind[0] ;
			_gl.bindBuffer( _type, _store[0] ) ;
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
		_matrix.transpose() ;
	}

	@Override
	protected void createTexture( final Settings _draw )
	{
		//System.out.println( "Create: " + _draw ) ;
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
			gl.enableVertexAttribArray( att.index ) ;
		}
	}

	private void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( VertexAttrib att : _atts )
		{
			gl.vertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}
	
	private void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( VertexAttrib att : _atts )
		{
			gl.disableVertexAttribArray( att.index ) ;
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

			gl.useProgram( program.id[0] ) ;

			final GLGeometryUploader.GLGeometry geometry = model.getGeometry( GLGeometryUploader.GLGeometry.class ) ;
			final boolean isGUI = _data.isUI() ;

			final WebGLUniformLocation inMVPMatrix      = gl.getUniformLocation( program.id[0], "inMVPMatrix" ) ;
			final WebGLUniformLocation inPositionMatrix = gl.getUniformLocation( program.id[0], "inPositionMatrix" ) ;

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

				gl.uniformMatrix4fv( inMVPMatrix, false, modelViewProjectionMatrix.matrix ) ;
				gl.uniformMatrix4fv( inPositionMatrix, false, newMatrix.matrix ) ;

				GLRenderer.bindBuffer( gl, GL3.ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
				GLRenderer.bindBuffer( gl, GL3.ARRAY_BUFFER, geometry.vboID, bufferID ) ;

				if( _update == true )
				{
					uploader.uploadIndex( gl, geometry, shape ) ;
					uploader.uploadVBO( gl, geometry, shape ) ;
				}

				// Don't render the element to the colour buffer
				gl.colorMask( false, false, false, false ) ;
				gl.enable( GL3.STENCIL_TEST ) ;

				gl.stencilMask( 0xFF ) ;
				gl.clear( GL3.STENCIL_BUFFER_BIT ) ;

				gl.stencilFunc( GL3.NEVER, 1, 0xFF ) ;
				gl.stencilOp( GL3.REPLACE, GL3.KEEP, GL3.KEEP ) ;

				prepareVertexAttributes( attributes, geometry.getStride() ) ;
				gl.drawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.UNSIGNED_INT, 0 ) ;

			matrixCache.reclaim( newMatrix ) ;
			disableVertexAttributes( attributes ) ;

			gl.colorMask( true, true, true, true ) ;		// Re-enable colour buffer
			gl.stencilFunc( GL3.EQUAL, 1, 1 ) ;
			// Continue drawing scene...
		}
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

	public static WebGLRenderingContext getContext()
	{
		return gl ;
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