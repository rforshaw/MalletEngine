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

public class GLRenderer extends Basic2DRender implements GLEventListener
{
	private static final MalletColour WHITE = MalletColour.white() ;
	private static final MalletColour BLACK = MalletColour.black() ;

	private static final MalletColour RED   = MalletColour.red() ;
	private static final MalletColour GREEN = MalletColour.green() ;
	private static final MalletColour BLUE  = MalletColour.blue() ;

	public static final int ORTHOGRAPHIC_MODE = 1 ;
	public static final int PERSPECTIVE_MODE = 2 ;

	protected static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;

	protected final static GLProgramManager programs = new GLProgramManager() ;
	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final ObjectCache<GLRenderData> renderCache = new ObjectCache<GLRenderData>( GLRenderData.class ) ;

	protected final static ObjectCache<Matrix4> matrixCache = new ObjectCache<Matrix4>( Matrix4.class ) ;
	protected final Matrix4 modelViewProjectionMatrix = matrixCache.get() ; 	// Combined Model View and Projection Matrix
	protected final Matrix4 uiMatrix = matrixCache.get() ;						// Used for rendering GUI elements not impacted by World/Camera position
	protected final Matrix4 worldMatrix = matrixCache.get() ;					// Used for moving the camera around the world

	protected int numID = 0 ;
	protected static final GLU glu = new GLU() ;
	protected static GLJPanel canvas = null ;
	protected JFrame frame = null ;

	private final Vector2 UV1 = new Vector2() ;
	private final Vector2 UV2 = new Vector2( 1.0f, 1.0f ) ;

	protected Vector2 pos = new Vector2() ;

	protected Vector3 oldCameraPosition = new Vector3() ;
	protected Vector3 cameraPosition = null ;

	protected Vector2 renderDimensions = null ;
	protected Vector2 displayDimensions = null ;

	protected GL3 gl = null ;
	protected DrawInterface drawShape = null ;
	protected DrawInterface drawTexture = null ;
	protected DrawInterface drawText = null ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;
	protected float rotate = 0.0f ;

	// Keep track of the last binded ID
	// If the next item to be rendered uses one of the same 
	// ID's then we can avoid the bind call.
	// Note: Caching vboID, results in an exception
	// on some occasions.
	protected final int[] textureID = new int[1] ;
	protected final int[] indexID = new int[1] ;
	protected final int[] bufferID = new int[1] ;

	public GLRenderer() {}

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
		GLModelGenerator.shutdown() ;
		textures.shutdown() ;
		fontManager.shutdown() ;
	}

	private void initGraphics()
	{
		final GLProfile glProfile = GLProfile.get( GLProfile.GL3 ) ;
		final GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
		capabilities.setDoubleBuffered( true ) ;

		canvas = new GLJPanel( capabilities ) ;
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

	private void initDrawCalls()
	{
		drawShape = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position ) 
			{
				final Model model = _settings.getObject( "MODEL", null ) ;
				if( model == null )
				{
					return ;
				}

				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				final GLGeometry geometry = model.getGeometry( GLGeometry.class ) ;
				final boolean isGUI = _settings.getBoolean( "GUI", false ) ;
				final int lineWidth = _settings.getInteger( "LINEWIDTH", 2 ) ;

				final GLProgram program = programs.get( "SIMPLE_GEOMETRY" ) ;
				if( program == null )
				{
					System.out.println( "Program doesn't exist.." ) ;
					return ;
				}

				gl.glUseProgram( program.id[0] ) ;

				final int inMVPMatrix      = gl.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = gl.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;
				final int inVertex         = gl.glGetAttribLocation( program.id[0], "inVertex" ) ;
				final int inColour         = gl.glGetAttribLocation( program.id[0], "inColour" ) ;

				//System.out.println( "MVP Matrix: " + inMVPMatrix ) ;
				//System.out.println( "Position Matrix: " + inPositionMatrix ) ;

				gl.glEnableVertexAttribArray( inVertex ) ;		// VERTEX ARRAY
				gl.glEnableVertexAttribArray( inColour ) ;		// COLOUR ARRAY

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

					gl.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;
					gl.glUniformMatrix4fv( inPositionMatrix, 1, true, newMatrix.matrix, 0 ) ;
					//gl.glLineWidth( ( float )lineWidth ) ;

					GLRenderer.bindBuffer( gl, GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( gl, GL3.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					if( _settings.getBoolean( "UPDATE", false ) == true )
					{
						GLModelGenerator.updateShapeModel( model, _settings.<Shape>getObject( "DRAWLINES", null ) ) ;
						GLModelManager.updateVBO( gl, geometry ) ;
						_settings.addObject( "UPDATE", false ) ;
					}

					gl.glVertexAttribPointer( inVertex,   3, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.POSITION_OFFSET ) ;
					gl.glVertexAttribPointer( inColour,   4, GL3.GL_UNSIGNED_BYTE, true,  GLGeometry.STRIDE, ( long )GLGeometry.COLOUR_OFFSET ) ;

					gl.glDrawElements( geometry.style, geometry.index.length, GL3.GL_UNSIGNED_INT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				gl.glUseProgram( 0 ) ;
				gl.glDisableVertexAttribArray( inVertex ) ;		// VERTEX ARRAY
				gl.glDisableVertexAttribArray( inColour ) ;		// COLOUR ARRAY
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
				GLRenderer.bindTexture( gl, image.textureIDs, textureID ) ;

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

				gl.glUseProgram( program.id[0] ) ;

				final int inMVPMatrix      = gl.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = gl.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;
				final int inVertex         = gl.glGetAttribLocation( program.id[0], "inVertex" ) ;
				final int inColour         = gl.glGetAttribLocation( program.id[0], "inColour" ) ;
				final int inTexCoord       = gl.glGetAttribLocation( program.id[0], "inTexCoord" ) ;
				final int inNormal         = gl.glGetAttribLocation( program.id[0], "inNormal" ) ;

				gl.glEnableVertexAttribArray( inVertex ) ;		// VERTEX ARRAY
				gl.glEnableVertexAttribArray( inColour ) ;		// COLOUR ARRAY
				gl.glEnableVertexAttribArray( inTexCoord ) ;	// TEXTURE COORD ARRAY
				gl.glEnableVertexAttribArray( inNormal ) ;		// NORMAL ARRAY

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

					gl.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;
					gl.glUniformMatrix4fv( inPositionMatrix, 1, true, newMatrix.matrix, 0 ) ;

					gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA ) ;

					GLRenderer.bindBuffer( gl, GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.indexID, indexID ) ;
					GLRenderer.bindBuffer( gl, GL3.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					// Update the UV co-ordinates of the model
					if( _settings.getBoolean( "UPDATE", false ) == true )
					{
						GLModelGenerator.updatePlaneModelColour( model, GLModelGenerator.getABGR( colour ) ) ;
						_settings.addObject( "UPDATE", false ) ;
					}

					GLModelGenerator.updatePlaneModelUV( model, uv1, uv2 ) ;
					GLModelManager.updateVBO( gl, geometry ) ;

					gl.glVertexAttribPointer( inVertex,   3, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.POSITION_OFFSET ) ;
					gl.glVertexAttribPointer( inColour,   4, GL3.GL_UNSIGNED_BYTE, true,  GLGeometry.STRIDE, ( long )GLGeometry.COLOUR_OFFSET ) ;
					gl.glVertexAttribPointer( inTexCoord, 2, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.TEXCOORD_OFFSET ) ;
					gl.glVertexAttribPointer( inNormal,   3, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.NORMAL_OFFSET ) ;

					gl.glDrawElements( GL3.GL_TRIANGLES, geometry.index.length, GL3.GL_UNSIGNED_INT, 0 ) ;

				matrixCache.reclaim( newMatrix ) ;

				gl.glDisableVertexAttribArray( inVertex ) ;		// VERTEX ARRAY
				gl.glDisableVertexAttribArray( inColour ) ;		// COLOUR ARRAY
				gl.glDisableVertexAttribArray( inTexCoord ) ;		// TEXTURE COORD ARRAY
				gl.glDisableVertexAttribArray( inNormal ) ;		// NORMAL ARRAY
				gl.glUseProgram( 0 ) ;
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
				if( fm == null ) { return ; }

				final GLImage image = fm.getGLImage() ;
				GLRenderer.bindTexture( gl, image.textureIDs, textureID ) ;

				final int height = fm.getHeight() ;
				final int lineWidth = _settings.getInteger( "LINEWIDTH", ( int )renderDimensions.x ) + ( int )_position.x ;
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

				gl.glUseProgram( program.id[0] ) ;

				final int inMVPMatrix      = gl.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;
				final int inPositionMatrix = gl.glGetUniformLocation( program.id[0], "inPositionMatrix" ) ;
				final int inVertex         = gl.glGetAttribLocation( program.id[0], "inVertex" ) ;
				final int inColour         = gl.glGetAttribLocation( program.id[0], "inColour" ) ;
				final int inTexCoord       = gl.glGetAttribLocation( program.id[0], "inTexCoord" ) ;
				final int inNormal         = gl.glGetAttribLocation( program.id[0], "inNormal" ) ;

				gl.glEnableVertexAttribArray( inVertex ) ;		// VERTEX ARRAY
				gl.glEnableVertexAttribArray( inColour ) ;		// COLOUR ARRAY
				gl.glEnableVertexAttribArray( inTexCoord ) ;	// TEXTURE COORD ARRAY
				gl.glEnableVertexAttribArray( inNormal ) ;		// NORMAL ARRAY

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

					gl.glUniformMatrix4fv( inMVPMatrix, 1, true, modelViewProjectionMatrix.matrix, 0 ) ;

					gl.glBlendFunc( GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA ) ;
					//gl.glAlphaFunc( GL.GL_GREATER, 0.5f ) ;

					final GLGeometry geometry = fm.getGLGeometry() ;
					GLRenderer.bindBuffer( gl, GL3.GL_ARRAY_BUFFER, geometry.vboID, bufferID ) ;

					gl.glVertexAttribPointer( inVertex,   3, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.POSITION_OFFSET ) ;
					gl.glVertexAttribPointer( inColour,   4, GL3.GL_UNSIGNED_BYTE, true,  GLGeometry.STRIDE, ( long )GLGeometry.COLOUR_OFFSET ) ;
					gl.glVertexAttribPointer( inTexCoord, 2, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.TEXCOORD_OFFSET ) ;
					gl.glVertexAttribPointer( inNormal,   3, GL3.GL_FLOAT,         false, GLGeometry.STRIDE, ( long )GLGeometry.NORMAL_OFFSET ) ;

					if( _settings.getBoolean( "UPDATE", false ) == true )
					{
						GLModelGenerator.updateModelColour( fm.model, GLModelGenerator.getABGR( colour ) ) ;
						GLModelManager.updateVBO( gl, geometry ) ;
						_settings.addObject( "UPDATE", false ) ;
					}

					final int size = words.length ;
					for( int i = 0; i < size; ++i )
					{
						renderText( words[i], fm, newMatrix, inPositionMatrix ) ;
					}

				matrixCache.reclaim( newMatrix ) ;

				gl.glDisableVertexAttribArray( inVertex ) ;		// VERTEX ARRAY
				gl.glDisableVertexAttribArray( inColour ) ;		// COLOUR ARRAY
				gl.glDisableVertexAttribArray( inTexCoord ) ;	// TEXTURE COORD ARRAY
				gl.glDisableVertexAttribArray( inNormal ) ;		// NORMAL ARRAY
				gl.glUseProgram( 0 ) ;
			}

			private void renderText( final String _text, final GLFontMap _fm, final Matrix4 _matrix, final int _matrixHandle )
			{
				final int length = _text.length() ;
				for( int i = 0; i < length; ++i )
				{
					final GLGlyph glyph = _fm.getGlyphWithChar( _text.charAt( i ) ) ;
					GLRenderer.bindBuffer( gl, GL3.GL_ELEMENT_ARRAY_BUFFER, glyph.index.indexID, indexID ) ;

					gl.glUniformMatrix4fv( _matrixHandle, 1, true, _matrix.matrix, 0 ) ;
					gl.glDrawElements( GL3.GL_TRIANGLES, glyph.index.index.length, GL3.GL_UNSIGNED_INT, 0 ) ;
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

	@Override
	public void init( GLAutoDrawable _drawable )
	{
		System.out.println( "GL Contex initialised.." ) ;
		gl = _drawable.getGL().getGL3() ;

		//gl.glEnable( GL.GL_TEXTURE_2D ) ;
		gl.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		gl.glEnable( GL.GL_BLEND ) ;

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

		final Vector2 display = renderInfo.getDisplayDimensions() ;
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
		renderDimensions = renderInfo.getRenderDimensions() ;
		displayDimensions = renderInfo.getScaledRenderDimensions() ;

		constructOrhto2D( modelViewProjectionMatrix, 0.0f, renderDimensions.x, renderDimensions.y, 0.0f ) ;

		/*gl.glMatrixMode( GL3.GL_PROJECTION );
		final Matrix4 matrix = matrixCache.get() ;			// identity by default
		gl.glLoadTransposeMatrixf( matrix.matrix, 0 ) ;

		// coordinate system origin at lower left with width and height same as the window
		if( viewMode == ORTHOGRAPHIC_MODE )
		{
			glu.gluOrtho2D( 0.0f, renderDimensions.x, renderDimensions.y, 0.0f ) ;
		}
		else
		{
			final Vector2 ratio = renderInfo.getRatioRenderToDisplay() ;
			glu.gluPerspective( 65.0f, ratio.x, 1.0f, 900.0f ) ;

			matrix.scale( 1.0f, -1.0f, 1.0f ) ;															// Invert Y axis to everything is upright
			matrix.translate( -( renderDimensions.x / 2.0f ), -( renderDimensions.y / 2.0f ), 0.0f ) ;	// To shift the camera back to centre 
			gl.glLoadTransposeMatrixf( matrix.matrix, 0 ) ;
		}

		matrixCache.reclaim( matrix ) ;

		gl.glMatrixMode( GL3.GL_MODELVIEW ) ;*/
		final Vector2 screenOffset = renderInfo.getScreenOffset() ;
		gl.glViewport( ( int )screenOffset.x, ( int )screenOffset.y, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;
	}

	@Override
	public void reshape( GLAutoDrawable _drawable, int _x, int _y, int _width, int _height )
	{
		renderInfo.setDisplayDimensions( new Vector2( _width, _height ) ) ;
		resize() ;
	}

	@Override
	public void dispose( GLAutoDrawable _drawable ) {}

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

		canvas.display() ;
	}

	@Override
	public void display( GLAutoDrawable _drawable )
	{
		gl = _drawable.getGL().getGL3() ;
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT ) ;
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

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

		canvas.swapBuffers() ;
	}

	protected void render()
	{
		state.removeRenderData() ;
		if( state.isStateStable() == true )
		{
			gl.glGetIntegerv( GL3.GL_TEXTURE_BINDING_2D, textureID, 0 ) ;
			gl.glGetIntegerv( GL3.GL_ELEMENT_ARRAY_BUFFER_BINDING, indexID, 0 ) ;
			gl.glGetIntegerv( GL3.GL_ELEMENT_ARRAY_BUFFER_BINDING, bufferID, 0 ) ;

			state.draw() ;
		}
	}

	private static void bindTexture( final GL3 _gl, final int[] _idToBind, final int[] _store )
	{
		if( _store[0] != _idToBind[0] )
		{
			_store[0] = _idToBind[0] ;
			_gl.glActiveTexture( GL3.GL_TEXTURE0 + 0 ) ;
			_gl.glBindTexture( GL.GL_TEXTURE_2D, _store[0] ) ;
		}
	}

	private static void bindBuffer( final GL3 _gl, final int _type, final int _idToBind, final int[] _store )
	{
		if( _store[0] != _idToBind )
		{
			_store[0] = _idToBind ;
			_gl.glBindBuffer( _type, _store[0] ) ;
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
			data.set( numID++,
					  DrawRequestType.TEXTURE,
					  _draw,
					  position,
					  layer ) ;
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
			final Shape shape = _draw.<Shape>getObject( "DRAWLINES", null ) ;
			if( shape != null )
			{
				_draw.addObject( "MODEL", GLModelGenerator.genShapeModel( shape ) ) ;
				final GLRenderData data = renderCache.get() ;
				data.set( numID++, DrawRequestType.GEOMETRY, _draw, position, layer ) ;
				//Logger.println( "GLRenderer - Create Lines: " + data.id, Logger.Verbosity.MINOR ) ;

				passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
				data.drawCall = drawShape ;
				insert( data ) ;
				return ;
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

	public static GLJPanel getCanvas()
	{
		return canvas ;
	}

	@Override
	public String getName()
	{
		return "GLRenderer" ;
	}

	private Texture loadTexture( final Settings _draw )
	{
		final Texture texture = textures.get( _draw.getString( "FILE", null ) ) ;
		if( texture == null )
		{
			return null ;
		}

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
				}
			}
		}
	}
}