package com.linxonline.mallet.renderer.desktop.GL ;

import javax.swing.JFrame ;
import java.util.ArrayList ;
import java.awt.Insets ;
import java.awt.Dimension ;
import java.awt.Frame ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;
import java.lang.reflect.* ;

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
	public static final int ORTHOGRAPHIC_MODE = 1 ;
	public static final int PERSPECTIVE_MODE = 2 ;

	protected static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;

	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;
	protected final ObjectCache<GLRenderData> renderCache = new ObjectCache<GLRenderData>( GLRenderData.class ) ;

	protected int numID = 0 ;
	protected static final GLU glu = new GLU() ;
	protected static GLJPanel canvas = null ;
	protected JFrame frame = null ;

	private final Vector2 UV1 = new Vector2() ;
	private final Vector2 UV2 = new Vector2( 1.0f, 1.0f ) ;

	protected final DefaultTimer timer = new DefaultTimer() ;
	protected Vector2 pos = new Vector2() ;

	protected final Matrix4 uiMatrix = Matrix4.createIdentity() ;
	protected final Matrix4 worldMatrix = Matrix4.createIdentity() ;

	protected Vector3 oldCameraPosition = new Vector3() ;
	protected Vector3 cameraPosition = null ;

	protected Vector2 renderDimensions = null ;
	protected Vector2 displayDimensions = null ;

	protected GL2 gl = null ;
	protected DrawInterface drawShape = null ;
	protected DrawInterface drawTexture = null ;
	protected DrawInterface drawText = null ;

	protected int viewMode = ORTHOGRAPHIC_MODE ;
	protected int textureID = 0 ;
	protected int indexID = 0 ;
	
	protected float rotate = 0.0f ;

	public GLRenderer() {}

	@Override
	public void start()
	{
		initGraphics() ;
		initDrawCalls() ;
		initAssist() ;
	}

	@Override
	public void shutdown()
	{
		clear() ;			// Clear the contents being rendered
		clean() ;			// Remove the resources that were in use
	}

	private void initGraphics()
	{
		final GLProfile glProfile = GLProfile.get( GLProfile.GL2 ) ;
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

				gl.glDisable( gl.GL_TEXTURE_2D ) ;
				gl.glEnableClientState( GL2.GL_VERTEX_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_COLOR_ARRAY ) ;

				gl.glPushMatrix() ;
					if( isGUI == true )
					{
						gl.glPushMatrix() ;
						gl.glLoadTransposeMatrixf( uiMatrix.matrix, 0 ) ;
					}

					gl.glTranslatef( _position.x, _position.y, 0.0f ) ;
					gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;
					gl.glTranslatef( offset.x, offset.y, 0.0f ) ;

					if( geometry.indexID != indexID )
					{
						indexID = geometry.indexID ;
						gl.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
						gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, geometry.vboID ) ;
					}

					gl.glVertexPointer( 3, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
					gl.glColorPointer( 4, GL2.GL_UNSIGNED_BYTE, GLGeometry.STRIDE, GLGeometry.COLOUR_OFFSET ) ;

					gl.glDrawElements( geometry.style, geometry.index.length, GL2.GL_UNSIGNED_INT, 0 ) ;

					if( isGUI == true )
					{
						gl.glPopMatrix() ;
					}
				gl.glPopMatrix() ;

				gl.glEnable( GL.GL_TEXTURE_2D ) ;
				gl.glDisableClientState( GL2.GL_VERTEX_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_COLOR_ARRAY ) ;
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
				if( image.textureID != textureID )
				{
					textureID = image.textureID ;
					gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;
				}

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

				final Vector2 uv1 = _settings.<Vector2>getObject( "UV1", UV1 ) ;
				final Vector2 uv2 = _settings.<Vector2>getObject( "UV2", UV2 ) ;

				gl.glEnableClientState( GL2.GL_VERTEX_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_COLOR_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_NORMAL_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_TEXTURE_COORD_ARRAY ) ;

				gl.glPushMatrix() ;
					if( isGUI == true )
					{
						gl.glPushMatrix() ;
						gl.glLoadTransposeMatrixf( uiMatrix.matrix, 0 ) ;
					}

					gl.glTranslatef( _position.x, _position.y, 0.0f ) ;
					gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;
					gl.glTranslatef( offset.x, offset.y, 0.0f ) ;

					gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA ) ;

					if( geometry.indexID != indexID )
					{
						indexID = geometry.indexID ;
						gl.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
						gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, geometry.vboID ) ;
					}

					// Update the UV co-ordinates of the model
					GLModelGenerator.updatePlaneModelUV( model, uv1, uv2 ) ;
					GLModelManager.updateVBO( gl, geometry ) ;
					
					gl.glVertexPointer( 3, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
					gl.glColorPointer( 4, GL2.GL_UNSIGNED_BYTE, GLGeometry.STRIDE, GLGeometry.COLOUR_OFFSET ) ;
					gl.glTexCoordPointer( 2, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.TEXCOORD_OFFSET ) ;
					gl.glNormalPointer( GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

					gl.glDrawElements( GL2.GL_TRIANGLES, geometry.index.length, GL2.GL_UNSIGNED_INT, 0 ) ;

					if( isGUI == true )
					{
						gl.glPopMatrix() ;
					}
				gl.glPopMatrix() ;

				gl.glDisableClientState( GL2.GL_VERTEX_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_NORMAL_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_COLOR_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_TEXTURE_COORD_ARRAY ) ;
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
				if( image.textureID != textureID )
				{
					textureID = image.textureID ;
					gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;
				}

				final int height = fm.getHeight() ;
				final int lineWidth = _settings.getInteger( "LINEWIDTH", ( int )renderDimensions.x ) + ( int )_position.x ;
				String[] words = _settings.getObject( "WORDS", null ) ;
				if( words == null )
				{
					words = optimiseText( fm, text, _position, lineWidth ) ;
					_settings.addObject( "WORDS", words ) ;
					_settings.addInteger( "TEXTWIDTH", -1 ) ;
				}

				final int alignment = _settings.getInteger( "ALIGNMENT", ALIGN_LEFT ) ;
				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", DEFAULT_OFFSET ) ;
				final boolean isGUI = _settings.getBoolean( "GUI", false ) ;
				final Vector2 currentPos = new Vector2( _position ) ;

				gl.glEnableClientState( GL2.GL_VERTEX_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_COLOR_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_NORMAL_ARRAY ) ;
				gl.glEnableClientState( GL2.GL_TEXTURE_COORD_ARRAY ) ;

				gl.glPushMatrix() ;
					setTextAlignment( alignment, currentPos, fm.stringWidth( words[0] ) ) ;
					if( isGUI == true )
					{
						gl.glPushMatrix() ;
						gl.glLoadTransposeMatrixf( uiMatrix.matrix, 0 ) ;
					}

					gl.glTranslatef( ( int )currentPos.x, ( int )currentPos.y, 0.0f ) ;
					gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;
					gl.glTranslatef( ( int )offset.x, ( int )offset.y, 0.0f ) ;
					gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA ) ;

					final int size = words.length ;
					for( int i = 0; i < size; ++i )
					{
						renderText( words[i], fm ) ;
						gl.glTranslatef( -fm.stringWidth( words[i] ), height, 0.0f ) ;
					}

					if( isGUI == true )
					{
						gl.glPopMatrix() ;
					}
				gl.glPopMatrix() ;

				gl.glDisableClientState( GL2.GL_VERTEX_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_COLOR_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_NORMAL_ARRAY ) ;
				gl.glDisableClientState( GL2.GL_TEXTURE_COORD_ARRAY ) ;
			}

			private void renderText( final String _text, final GLFontMap _fm )
			{
				final int length = _text.length() ;
				for( int i = 0; i < length; ++i )
				{
					final GLGlyph glyph = _fm.getGlyphWithChar( _text.charAt( i ) ) ;
					final GLGeometry geometry = glyph.getGLGeometry() ;

					if( geometry.indexID != indexID )
					{
						indexID = geometry.indexID ;
						gl.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
						gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, geometry.vboID ) ;
					}

					gl.glVertexPointer( 3, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
					gl.glColorPointer( 4, GL2.GL_UNSIGNED_BYTE, GLGeometry.STRIDE, GLGeometry.COLOUR_OFFSET ) ;
					gl.glTexCoordPointer( 2, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.TEXCOORD_OFFSET ) ;
					gl.glNormalPointer( GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

					gl.glDrawElements( GL2.GL_TRIANGLES, geometry.index.length, GL2.GL_UNSIGNED_INT, 0 ) ;
					gl.glTranslatef( glyph.advance, 0.0f, 0.0f ) ;
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
		gl = _drawable.getGL().getGL2() ;

		gl.glEnable( GL.GL_TEXTURE_2D ) ;
		gl.setSwapInterval( GlobalConfig.getInteger( "VSYNC", 0 ) ) ; // V-Sync 1 = Enabled, 0 = Disabled
		gl.glEnable( GL.GL_BLEND ) ;

		resize() ;
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

		gl.glMatrixMode( GL2.GL_PROJECTION );
		gl.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		if( viewMode == ORTHOGRAPHIC_MODE )
		{
			glu.gluOrtho2D( 0.0f, renderDimensions.x, renderDimensions.y, 0.0f ) ;
		}
		else
		{
			final Vector2 ratio = renderInfo.getRatioRenderToDisplay() ;
			glu.gluPerspective( 65.0f, ratio.x, 1.0f, 900.0f ) ;
			gl.glScalef( 1.0f, -1.0f, 1.0f ) ;															// Invert Y axis to everything is upright
			gl.glTranslatef( -( renderDimensions.x / 2.0f ), -( renderDimensions.y / 2.0f ), 0.0f ) ; 	// To shift the camera back to centre 
		}

		gl.glMatrixMode( GL2.GL_MODELVIEW ) ;
		gl.glLoadIdentity() ;

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
		gl = _drawable.getGL().getGL2() ;
		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT ) ;
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		gl.glLoadIdentity() ;

		updateEvents() ;

		// Calculate the current Camera Position based 
		// on oldCameraPosition and future cameraPosition
		calculateInterpolatedPosition( oldCameraPosition, cameraPosition, pos ) ;
		renderInfo.setCameraZoom( cameraScale.x, cameraScale.y ) ;

		gl.glPushMatrix() ;
			final Vector2 half = renderInfo.getHalfRenderDimensions() ;
			gl.glTranslatef( half.x, half.y, 0.0f ) ;
			gl.glScalef( cameraScale.x, cameraScale.y, cameraScale.z ) ;
			gl.glTranslatef( -pos.x, -pos.y, 0.0f ) ;
			render() ;
		gl.glPopMatrix() ;

		canvas.swapBuffers() ;
	}

	protected void render()
	{
		state.removeRenderData() ;
		if( state.isStateStable() == true )
		{
			state.draw() ;
		}
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
			final Line line = _draw.<Line>getObject( "DRAWLINE", null ) ;
			if( line != null )
			{
				_draw.addObject( "MODEL", GLModelGenerator.genLineModel( line ) ) ;
				final GLRenderData data = renderCache.get() ;
				data.set( numID++, DrawRequestType.GEOMETRY, _draw, position, layer ) ;
				//Logger.println( "GLRenderer - Create Line: " + data.id, Logger.Verbosity.MINOR ) ;

				passIDToCallback( data.id, _draw.<IDInterface>getObject( "CALLBACK", null ) ) ;
				data.drawCall = drawShape ;
				insert( data ) ;
				return ;
			}

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

		public GLRenderData( final int _id, final int _type,
							final Settings _draw, final Vector3 _position,
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