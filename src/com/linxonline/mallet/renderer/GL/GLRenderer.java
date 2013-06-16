package com.linxonline.mallet.renderer ;

import javax.swing.JFrame ;
import java.util.ArrayList ;
import java.awt.* ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;

import javax.media.opengl.* ;
import javax.media.opengl.awt.GLCanvas ;
import javax.media.opengl.glu.GLU ;

import com.linxonline.mallet.resources.gl.* ;
import com.linxonline.mallet.physics.AABB ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.resources.gl.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;

public class GLRenderer extends Basic2DRender implements GLEventListener
{
	private static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;
	protected final static GLTextureManager textures = new GLTextureManager() ;

	private int numID = 0 ;
	private static GLCanvas canvas = null ;
	private JFrame frame = null ;

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
	
	public GLRenderer()
	{
		initGraphics() ;
		initDrawCalls() ;
	}

	private void initGraphics()
	{
		GLProfile glProfile = GLProfile.getDefault() ;
		GLCapabilities capabilities = new GLCapabilities( glProfile ) ;
		capabilities.setDoubleBuffered( true ) ;
		
		canvas = new GLCanvas( capabilities ) ;
		canvas.setAutoSwapBufferMode( false ) ;
		canvas.addGLEventListener( this ) ;
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
								gl.glVertex2f( point.x, point.y ) ;
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
				if( texture != null )
				{
					final GLImage image = texture.getImage( GLImage.class ) ;
					if( image.textureID != textureID )
					{
						textureID = image.textureID ;
						gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;
					}
				}
				else
				{
					final String file = _settings.getString( "FILE", null ) ;
					if( ( texture = loadTexture( file ) ) != null )
					{
						final String dim = _settings.getString( "DIM", null ) ;
						final Vector2 dimension = _settings.getObject( "DIM", Vector2.class, null ) ;
						_settings.addObject( "MODEL", GLModelGenerator.genPlaneModel( dim, ( int )dimension.x, ( int )dimension.y ) ) ;
					}
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
					gl.glTranslatef( _position.x + offset.x, _position.y + offset.y, 0.0f ) ;
					gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;

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
				System.out.println( "Draw Text" ) ;
			}
		} ;
	}
	
	public void hookToWindow( final JFrame _frame )
	{
		frame = _frame ;
		frame.add( canvas ) ;
		frame.pack() ;
	}

	@Override
	public void init( GLAutoDrawable _drawable )
	{
		GL2 gl = _drawable.getGL().getGL2() ;
		renderDimensions = renderInfo.getRenderDimensions() ;
		displayDimensions = renderInfo.getDisplayDimensions() ;

		gl.glEnable( GL.GL_TEXTURE_2D ) ;
		gl.setSwapInterval( 0 ) ; // V-Sync 1 = Enabled, 0 = Disabled

		gl.glEnable( GL.GL_BLEND ) ;
		gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA ) ;

		gl.glMatrixMode( GL2.GL_PROJECTION );
		gl.glLoadIdentity();

		// coordinate system origin at lower left with width and height same as the window
		GLU glu = new GLU();
		glu.gluOrtho2D( 0.0f, renderDimensions.x, renderDimensions.y, 0.0f ) ;

		gl.glMatrixMode( GL2.GL_MODELVIEW ) ;
		gl.glLoadIdentity() ;

		gl.glViewport( 0, 0, ( int )displayDimensions.x, ( int )displayDimensions.y ) ;

		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY ) ;
		gl.glEnableClientState( GL2.GL_NORMAL_ARRAY ) ;
		gl.glEnableClientState( GL2.GL_TEXTURE_COORD_ARRAY ) ;
	}

	@Override
	public void reshape( GLAutoDrawable _drawable, int _x, int _y, int _width, int _height ) {}

	@Override
	public void dispose( GLAutoDrawable _drawable ) {}

	public void draw()
	{
		cameraPosition = renderInfo.getCameraPosition() ;
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

	private void drawShapes( GL2 _gl, RenderContainer _container )
	{
		final Vector3 position = _container.position ;
		final Settings settings = _container.settings ;
		final float rotation = ( float )Math.toDegrees( settings.getFloat( "ROTATE", 0.0f ) ) ;
		final Vector2 offset = settings.getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ) ;

		_gl.glPushMatrix() ;
			_gl.glTranslatef( position.x, position.y, 0.0f ) ;
			_gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;
			_gl.glTranslatef( offset.x, offset.y, 0.0f ) ;

			try
			{
				final Line line = ( Line )settings.getObject( "DRAWLINE" ) ;
				_gl.glBegin( GL2.GL_LINES ) ;
					_gl.glVertex2f( line.start.x, line.start.y ) ;
					_gl.glVertex2f( line.end.x, line.end.y ) ;
				_gl.glEnd() ;
			}
			catch( NullPointerException _ex ) {}

			try
			{
				final Shape shape = ( Shape )settings.getObject( "DRAWLINES" ) ;
				//_graphics.setColor( Color.WHITE ) ;
				
				final int size = shape.indicies.size() ;
				for( int i = 0; i < size; i += 2 )
				{
					final Vector2 start = shape.points.get( shape.indicies.get( i ) ) ;
					final Vector2 end = shape.points.get( shape.indicies.get( i + 1 ) ) ;

					_gl.glBegin( GL2.GL_LINES ) ;
						_gl.glColor3f( 1.0f, 1.0f, 1.0f ) ;
						_gl.glVertex2f( start.x, start.y ) ;
						_gl.glVertex2f( end.x, end.y ) ;
					_gl.glEnd() ;
				}
			}
			catch( NullPointerException _ex ) {}

			try
			{
				final Shape shape = ( Shape )settings.getObject( "POINTS" ) ;
				//_graphics.setColor( Color.WHITE ) ;

				final int size = shape.indicies.size() ;
				for( int i = 0; i < size; ++i )
				{
					final Vector2 point = shape.points.get( shape.indicies.get( i ) ) ;
					_gl.glBegin( GL2.GL_POINT ) ;
						_gl.glColor3f( 1.0f, 0.0f, 0.0f ) ;
						_gl.glVertex2f( point.x, point.y ) ;
					_gl.glEnd() ;
				}
			}
			catch( NullPointerException _ex ) {}
		_gl.glPopMatrix() ;
	}

	@Override
	protected void createTexture( final Settings _draw )
	{
		System.out.println( "Creating Texture" ) ;
		final Vector3 position = _draw.getObject( "POSITION", Vector3.class, null ) ;
		final int layer = _draw.getInteger( "LAYER", -1 ) ;

		if( position != null )
		{
			final RenderData data = new RenderData( numID++, DrawRequestType.TEXTURE, _draw, position, layer ) ;
			data.drawCall = drawTexture ;
			insert( data ) ;
		}
	}

	@Override
	protected void createGeometry( final Settings _draw ) {}

	@Override
	protected void createText( final Settings _draw ) {}

	public void sort() {}

	public static GLCanvas getCanvas()
	{
		return canvas ;
	}

	private Texture loadTexture( final String _file )
	{
		return _file != null ? ( Texture )textures.get( _file ) : null ;
	}
}