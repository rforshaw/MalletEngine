package com.linxonline.mallet.renderer ;

import javax.swing.JFrame ;
import java.util.ArrayList ;
import java.awt.* ;
import java.awt.image.BufferStrategy ;
import java.awt.geom.AffineTransform ;

import javax.media.opengl.* ;
import javax.media.opengl.awt.GLCanvas ;
import javax.media.opengl.glu.GLU ;

import com.linxonline.mallet.physics.AABB ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.resources.gl.* ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.resources.texture.* ;

public class GLRenderer extends Basic2DRender implements GLEventListener
{
	private static final Vector2 DEFAULT_OFFSET = new Vector2( 0, 0 ) ;

	public RenderInfo renderInfo = new RenderInfo( new Vector2( 800, 600 ), 
												   new Vector2( 800, 600 ), 
												   new Vector3( 0, 0, 0 ) ) ; 

	private ArrayList<RenderContainer> content = new ArrayList<RenderContainer>() ;
	private static GLCanvas canvas = null ;
	private JFrame frame = null ;

	//private AABB viewArea = new AABB() ;
	private Vector2 halfRenderDimensions = new Vector2( 0, 0 ) ;
	private Vector2 containerPos = new Vector2( 0, 0 ) ;

	private Vector3 cameraPosition = null ;
	private Vector2 renderDimensions = null ;
	private Vector2 displayDimensions = null ;
	
	private int textureID = 0 ;
	private int indexID = 0 ;
	
	public GLRenderer()
	{
		initGraphics() ;
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
	
	public void hookToWindow( final JFrame _frame )
	{
		frame = _frame ;
		frame.add( canvas ) ;
		frame.pack() ;
	}
	
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

	public void setRenderDimensions( final int _width, final int _height )
	{
		halfRenderDimensions.x = _width / 2 ;
		halfRenderDimensions.y = _height / 2 ;

		//viewArea = new AABB( -( halfRenderDimensions.x + 100 ), -( halfRenderDimensions.y + 100 ),
		//					  halfRenderDimensions.x + 100, halfRenderDimensions.y + 100 ) ;

		renderInfo.setRenderDimensions( new Vector2( _width, _height ) ) ;
		renderDimensions = renderInfo.getRenderDimensions() ;
	}

	public void setDisplayDimensions( final int _width, final int _height )
	{
		renderInfo.setDisplayDimensions( new Vector2( _width, _height ) ) ;
		canvas.setSize( _width, _height ) ;
	}

	public void setCameraPosition( final Vector3 _position )
	{
		renderInfo.setCameraPosition( _position ) ;
	}

	public void addRenderContainer( final RenderContainer _container )
	{
		if( exists( _container ) == true )
		{
			return ;
		}

		insert( _container ) ;
	}

	public void removeRenderContainer( final RenderContainer _container )
	{
		if( exists( _container ) == true )
		{
			content.remove( _container ) ;
		}
	}

	// GLEventListener - Not specifically used
	public void reshape( GLAutoDrawable _drawable, int _x, int _y, int _width, int _height ) {}

	// GLEventListener - Not specifically used
	public void displayChanged( GLAutoDrawable _drawable, boolean _modeChanged, boolean _deviceChanged ) {}

	// GLEventListener - Not specifically used
	public void dispose( GLAutoDrawable _drawable ) {}

	public void draw()
	{
		cameraPosition = renderInfo.getCameraPosition() ;
		if( cameraPosition == null )
		{
			System.out.println( "Camera Not Set" ) ;
			return ;
		}

		//viewArea.moveAABB( cameraPosition.x - halfRenderDimensions.x, 
		//				   cameraPosition.y - halfRenderDimensions.y ) ;

		canvas.display() ;
	}

	// GLEventListener Starts the rendering
	public void display( GLAutoDrawable _drawable )
	{
		GL2 gl = _drawable.getGL().getGL2() ;

		gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT ) ;
		gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f ) ;

		gl.glLoadIdentity() ;

		gl.glPushMatrix() ;
			gl.glTranslatef( cameraPosition.x, cameraPosition.y, 0.0f ) ;
			render( gl ) ;
		gl.glPopMatrix() ;

		gl.glFlush() ;
		canvas.swapBuffers() ;
	}

	private void render( GL2 _gl )
	{
		for( RenderContainer container : content )
		{
			containerPos.setXY( container.position.x, container.position.y ) ;
			//if( isWithinBounds( containerPos ) == false )
			//{
			//	continue ;
			//}

			for( Integer type : container.enabledTypes )
			{
				if( type == RenderContainer.MODEL_TYPE )
				{
					drawModel( _gl, container ) ;
				}
				else if( type == RenderContainer.TEXT_TYPE )
				{
					//drawString( _gl, container ) ;
				}
				else if( type == RenderContainer.GEOMETRIC_TYPE )
				{
					drawShapes( _gl, container ) ;
				}
			}
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

	private void drawModel( GL2 _gl, RenderContainer _container )
	{
		final Settings settings = _container.settings ;
		final Model model = settings.getObject( "MODEL", Model.class, null ) ;
		if( model == null )
		{
			// Model Doesn't exist, no need to render.
			return ;
		}

		final Texture texture = settings.getObject( "TEXTURE", Texture.class, null ) ;
		if( texture != null )
		{
			final GLImage image = texture.getImage( GLImage.class ) ;
			if( image.textureID != textureID )
			{
				textureID = image.textureID ;
				_gl.glBindTexture( GL.GL_TEXTURE_2D, textureID ) ;
			}
		}

		final Vector3 position = _container.position ;
		final float rotation = ( float )Math.toDegrees( settings.getFloat( "ROTATE", 0.0f ) ) ;
		final Vector2 offset = settings.getObject( "OFFSET", Vector2.class, DEFAULT_OFFSET ) ;
		final GLGeometry geometry = model.getGeometry( GLGeometry.class ) ;

		_gl.glPushMatrix() ;
			_gl.glTranslatef( position.x, position.y, 0.0f ) ;
			_gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f ) ;
			_gl.glTranslatef( offset.x, offset.y, 0.0f ) ;

			if( geometry.indexID != indexID )
			{
				indexID = geometry.indexID ;
				_gl.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
				_gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, geometry.vboID ) ;
			}

			_gl.glVertexPointer( 3, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.POSITION_OFFSET ) ;
			_gl.glTexCoordPointer( 2, GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.TEXCOORD_OFFSET ) ;
			_gl.glNormalPointer( GL2.GL_FLOAT, GLGeometry.STRIDE, GLGeometry.NORMAL_OFFSET ) ;

			_gl.glDrawElements( GL2.GL_TRIANGLES, geometry.index.length, GL2.GL_UNSIGNED_INT, 0 ) ;
		_gl.glPopMatrix() ;
	}

	public void sort()
	{
		content = quicksort( content ) ;
	}

	public void clear()
	{
		content.clear() ;
	}

	public static GLCanvas getCanvas()
	{
		return canvas ;
	}
	
	/*private boolean isWithinBounds( final Vector2 _position )
	{
		return viewArea.intersectByPosition( _position ) ;
	}*/
	
	private void insert( final RenderContainer _container )
	{
		try
		{
			final int layer1 = _container.settings.getInteger( "LAYER" ) ;
			int layer2 = 0 ;

			for( RenderContainer container : content )
			{
				layer2 = container.settings.getInteger( "LAYER" ) ;

				if( layer1 <= layer2 )
				{
					int index = content.indexOf( container ) ;
					content.add( index, _container ) ;
					return ;
				}
			}
		}
		catch( NullPointerException _ex ) {}

		content.add( _container ) ;
	}

	private ArrayList<RenderContainer> quicksort( ArrayList<RenderContainer> _contents )
	{
		final int size = _contents.size() ;
		if( size <= 1 )
		{
			return _contents ;
		}
		
		int layer = 0 ;
		final int pivot = _contents.get( size / 2 ).settings.getInteger( "LAYER" ) ;
		RenderContainer pivotContainer = _contents.get( size / 2 ) ;
		_contents.remove( pivot ) ;
		
		ArrayList<RenderContainer> less = new ArrayList<RenderContainer>() ;
		ArrayList<RenderContainer> greater = new ArrayList<RenderContainer>() ;
		
		for( RenderContainer container : _contents )
		{
			layer = container.settings.getInteger( "LAYER" ) ;
			if( layer <= pivot )
			{
				less.add( container ) ;
			}
			else
			{
				greater.add( container ) ;
			}
		}
		
		less = quicksort( less ) ;
		greater = quicksort( greater ) ;

		less.add( pivotContainer ) ;
		less.addAll( greater ) ;
		return less ;
	}

	private final boolean exists( final RenderContainer _container )
	{
		return content.contains( _container ) ;
	}

	protected void createTexture( final Settings _draw ) {}
	protected void createGeometry( final Settings _draw ) {}
	protected void createText( final Settings _draw ) {}
	
}