package com.linxonline.mallet.renderer.GL ;

import javax.swing.JFrame ;
import java.util.ArrayList ;
import java.awt.Insets ;
import java.awt.Dimension ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;

import javax.media.opengl.* ;
import javax.media.opengl.awt.GLCanvas ;
import javax.media.opengl.glu.GLU ;

import com.linxonline.mallet.physics.AABB ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;
import com.linxonline.mallet.util.id.IDInterface ;
import com.linxonline.mallet.util.time.DefaultTimer ;

public class GLRenderer extends Basic2DRender implements GLEventListener
{
	private static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;

	protected final static GLTextureManager textures = new GLTextureManager() ;
	protected final static GLFontManager fontManager = new GLFontManager( textures ) ;

	private int numID = 0 ;
	private static GLU glu = new GLU() ;
	private static GLCanvas canvas = null ;
	private JFrame frame = null ;

	private final DefaultTimer timer = new DefaultTimer() ;
	private Vector2 pos = new Vector2() ;
	private Vector3 cameraPosition = null ;
	private Vector2 renderDimensions = null ;
	private Vector2 displayDimensions = null ;

	protected GL2 gl = null ;
	protected DrawInterface drawShape = null ;
	protected DrawInterface drawTexture = null ;
	protected DrawInterface drawText = null ;

	private int textureID = 0 ;
	private int indexID = 0 ;
	
	public GLRenderer() {}

	@Override
	public void start()
	{
		initGraphics() ;
		initDrawCalls() ;
	}

	@Override
	public void shutdown() {}
	
	private void initGraphics()
	{
		GLProfile glProfile = GLProfile.getDefault() ;
		GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
		capabilities.setDoubleBuffered( true ) ;
		
		canvas = new GLCanvas( capabilities ) ;
		canvas.setAutoSwapBufferMode( false ) ;
		canvas.addGLEventListener( this ) ;
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
		final JFrame temp = new JFrame() ;
		temp.pack() ;

		final Insets insets = temp.getInsets() ;
		final int dimX = insets.left + insets.right + _width ;
		final int dimY = insets.top + insets.bottom + _height ;
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
				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ) ;

				gl.glPushMatrix() ;
					gl.glTranslatef( _position.x + offset.x, _position.y + offset.y, 0.0f ) ;
					gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;

					final Line line = ( Line )_settings.getObject( "DRAWLINE", Line.class, null ) ;
					if( line != null )
					{
						gl.glBegin( GL2.GL_LINES ) ;
							gl.glVertex2f( line.start.x, line.start.y ) ;
							gl.glVertex2f( line.end.x, line.end.y ) ;
						gl.glEnd() ;
					}

					Shape shape = ( Shape )_settings.getObject( "DRAWLINES", Shape.class, null ) ;
					if( shape != null )
					{
						final int size = shape.indicies.size() ;
						for( int i = 0; i < size; i += 2 )
						{
							final Vector2 start = shape.points.get( shape.indicies.get( i ) ) ;
							final Vector2 end = shape.points.get( shape.indicies.get( i + 1 ) ) ;

							gl.glBegin( GL2.GL_LINES ) ;
								gl.glColor3f( 1.0f, 1.0f, 1.0f ) ;
								gl.glVertex2f( start.x, start.y ) ;
								gl.glVertex2f( end.x, end.y ) ;
							gl.glEnd() ;
						}
					}

					shape = ( Shape )_settings.getObject( "POINTS", Shape.class, null ) ;
					if( shape != null )
					{
						final int size = shape.indicies.size() ;
						for( int i = 0; i < size; ++i )
						{
							final Vector2 point = shape.points.get( shape.indicies.get( i ) ) ;
							gl.glBegin( GL2.GL_POINT ) ;
								gl.glColor3f( 1.0f, 0.0f, 0.0f ) ;
								gl.glVertex2f( point.x + offset.x, point.y + offset.y ) ;
							gl.glEnd() ;
						}
					}
				gl.glPopMatrix() ;
			}
		} ;

		drawTexture = new DrawInterface()
		{
			public void draw( final Settings _settings, final Vector2 _position ) 
			{
				
				Texture texture = _settings.getObject( "TEXTURE", Texture.class, null ) ;
				if( texture == null )
				{
					texture = loadTexture( _settings ) ;
					if( texture == null ) { return ; }
				}
				
				final GLImage image = texture.getImage( GLImage.class ) ;
				if( image.textureID != textureID )
				{
					textureID = image.textureID ;
					gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;
				}

				final Model model = _settings.getObject( "MODEL", Model.class, null ) ;
				if( model == null )
				{
					// If we can't map the texture to a plane, then no point in rendering.
					return ;
				}

				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ) ;
				final GLGeometry geometry = model.getGeometry( GLGeometry.class ) ;

				gl.glPushMatrix() ;
					
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
					gl.glTexCoordPointer( 2, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.TEXCOORD_OFFSET ) ;
					gl.glNormalPointer( GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

					gl.glDrawElements( GL2.GL_TRIANGLES, geometry.index.length, GL2.GL_UNSIGNED_INT, 0 ) ;
				gl.glPopMatrix() ;
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

				final MalletFont font = _settings.getObject( "FONT", MalletFont.class, null ) ;
				if( font == null )
				{
					System.out.println( "No Font, set." ) ;
					return ; }
				else
				{
					if( font.font == null )
					{
						font.font = fontManager.get( font.fontName, font.size ) ;
					}
				}

				final GLFontMap fm = ( GLFontMap )font.font ;
				if( fm == null ) { return ; }

				final GLImage image = fm.getGLImage() ;
				if( image.textureID != textureID )
				{
					textureID = image.textureID ;
					gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;
				}

				final int height = fm.getHeight() ;
				final int lineWidth = _settings.getInteger( "LINEWIDTH", ( int )renderDimensions.x ) + ( int )_position.x ;
				String[] words = _settings.getObject( "WORDS", String[].class, null ) ;
				if( words == null )
				{
					words = optimiseText( fm, text, _position, lineWidth ) ;
					_settings.addObject( "WORDS", words ) ;
					_settings.addInteger( "TEXTWIDTH", -1 ) ;
				}

				final int alignment = _settings.getInteger( "ALIGNMENT", ALIGN_LEFT ) ;
				final float rotation = ( float )Math.toDegrees( _settings.getFloat( "ROTATE", 0.0f ) ) ;
				final Vector2 offset = _settings.getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ) ;
				final Vector2 currentPos = new Vector2( _position ) ;

				gl.glPushMatrix() ;
					setTextAlignment( alignment, currentPos, fm.stringWidth( words[0] ) ) ;
					gl.glTranslatef( currentPos.x, currentPos.y, 0.0f ) ;
					gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;
					gl.glTranslatef( offset.x, offset.y, 0.0f ) ;

					final int size = words.length ;
					for( int i = 0; i < size; ++i )
					{
						renderText( words[i], fm ) ;
						gl.glTranslatef( -fm.stringWidth( words[i] ), height, 0.0f ) ;
					}
				gl.glPopMatrix() ;
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
				//System.out.println( "WORD WIDTH: " + _wordWidth ) ;
				switch( _alignment )
				{
					case ALIGN_RIGHT :
					{
						_position.x -= _wordWidth ;
						break ;
					}
					case ALIGN_CENTRE :
					{
						_position.x -= _wordWidth / 2 ;
						break ;
					}
					default:
					{
						return ;
					}
				}
			}
		} ;
	}
	
	public void hookToWindow( final JFrame _frame )
	{
		frame = _frame ;
		frame.createBufferStrategy( 1 ) ;
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ) ;
		frame.setIgnoreRepaint( true ) ;

		frame.add( canvas ) ;
		frame.pack() ;
		
		final Vector2 display = renderInfo.getDisplayDimensions() ;
		frame.setSize( ( int )display.x, ( int )display.y ) ;
		frame.setMinimumSize( new Dimension( ( int )display.x, ( int )display.y ) ) ;
		frame.validate() ;
		frame.setVisible( true ) ;

		draw() ;
	}

	@Override
	public void init( GLAutoDrawable _drawable )
	{
		gl = _drawable.getGL().getGL2() ;

		gl.glEnable( GL.GL_TEXTURE_2D ) ;
		gl.setSwapInterval( 0 ) ; // V-Sync 1 = Enabled, 0 = Disabled

		gl.glEnable( GL.GL_BLEND ) ;
		gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA ) ;

		resize() ;

		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY ) ;
		gl.glEnableClientState( GL2.GL_NORMAL_ARRAY ) ;
		gl.glEnableClientState( GL2.GL_TEXTURE_COORD_ARRAY ) ;

		if( gl.isExtensionAvailable( "GL_EXT_abgr" ) == true )
		{
			textures.setImageFormat( GL2.GL_ABGR_EXT  ) ;
		}
	}

	private void resize()
	{
		renderDimensions = renderInfo.getRenderDimensions() ;
		displayDimensions = renderInfo.getDisplayDimensions() ;

		gl.glMatrixMode( GL2.GL_PROJECTION );
		gl.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		glu.gluOrtho2D( 0.0f, renderDimensions.x, renderDimensions.y, 0.0f ) ;

		gl.glMatrixMode( GL2.GL_MODELVIEW ) ;
		gl.glLoadIdentity() ;

		gl.glViewport( 0, 0, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;
	}
	
	@Override
	public void reshape( GLAutoDrawable _drawable, int _x, int _y, int _width, int _height )
	{
		renderInfo.setDisplayDimensions( new Vector2( _width, _height ) ) ;
		resize() ;
	}

	@Override
	public void dispose( GLAutoDrawable _drawable ) {}

	public void draw()
	{
		cameraPosition = renderInfo.getCameraPosition() ;
		renderDimensions = renderInfo.getRenderDimensions() ;
		displayDimensions = renderInfo.getDisplayDimensions() ;

		if( cameraPosition == null )
		{
			System.out.println( "Camera Not Set" ) ;
			return ;
		}

		updateEvents() ;
		canvas.display() ;
	}

	@Override
	public void display( GLAutoDrawable _drawable )
	{
		gl = _drawable.getGL().getGL2() ;

		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT ) ;
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		gl.glLoadIdentity() ;

		gl.glPushMatrix() ;
			gl.glTranslatef( cameraPosition.x, cameraPosition.y, 0.0f ) ;
			render() ;
		gl.glPopMatrix() ;

		gl.glFlush() ;
		canvas.swapBuffers() ;

		//timer.getElapsedTimeInNanoSeconds() ;
	}

	protected void render()
	{
		final int length = content.size() ;
		RenderData data = null ;

		for( int i = 0; i < length; ++i )
		{
			data = content.get( i ) ;
			pos.setXY( data.position.x, data.position.y ) ;
			data.drawCall.draw( data.drawData, pos ) ;
		}
	}

	@Override
	protected void createTexture( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", Vector3.class, null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.TEXTURE, _draw, position, layer ) ;
			passIDToCallback( data.id, _draw.getObject( "CALLBACK", IDInterface.class, null ) ) ;
			data.drawCall = drawTexture ;
			insert( data ) ;
		}
	}

	@Override
	protected void createGeometry( final Settings _draw ) {}

	@Override
	protected void createText( final Settings _draw )
	{
		final Vector3 position = _draw.getObject( "POSITION", Vector3.class, null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.TEXT, _draw, position, layer ) ;
			passIDToCallback( data.id, _draw.getObject( "CALLBACK", IDInterface.class, null ) ) ;
			data.drawCall = drawText ;
			insert( data ) ;
		}
	}

	public void sort() {}

	public static GLCanvas getCanvas()
	{
		return canvas ;
	}

	private Texture loadTexture( final Settings _draw )
	{
		final Texture texture = ( Texture )textures.get( _draw.getString( "FILE", null ) ) ;
		if( texture == null ) { return null ; }
		
		Vector2 fillDim = _draw.getObject( "FILL", Vector2.class, null ) ;
		Vector2 dimension = _draw.getObject( "DIM", Vector2.class, null ) ;
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
}