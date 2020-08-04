package com.linxonline.mallet.renderer.android.opengl ;

import java.util.Iterator ;
import java.util.List ;
import java.util.Arrays ;
import java.nio.* ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Shape.Swivel ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.ProgramMap ;
import com.linxonline.mallet.renderer.BasicDraw ;
import com.linxonline.mallet.renderer.TextData ;
import com.linxonline.mallet.renderer.font.Glyph ;

import com.linxonline.mallet.renderer.opengl.Buffers ;
import com.linxonline.mallet.renderer.opengl.LocationBuffer ;
import com.linxonline.mallet.renderer.opengl.Location ;

import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.util.OrderedInsert ;
import com.linxonline.mallet.util.ISort ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class GLGeometryUploader
{
	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFF ;
	private final static int PRIMITIVE_EXPANSION = 1 ;

	private final static int VBO_VAR_BYTE_SIZE = 4 ;
	private final static int IBO_VAR_BYTE_SIZE = 2 ;

	public abstract class BufferObject
	{
		protected final short[] indicies ;
		protected final float[] verticies ;

		protected final ShortBuffer indexBuffer ;
		protected final FloatBuffer vertexBuffer ;

		protected final int[] indexID ;
		protected final int[] vboID ;

		protected final GLDraw.Mode mode ;
		protected Shape.Swivel[] shapeSwivel = null ;
		protected Shape.Style shapeStyle = null ;
		protected int shapeSwivelSize = 0 ;

		protected boolean dirty = true ;
		protected VertexAttrib[] attributes = null ;
		protected int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
		protected int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,
		protected int indexLength = 0 ;

		// A Copy of the ProgramMap provided by the user.
		// Used to determine if a ProgramMap provided by a 
		// Draw is compatible with this ObjectBuffer.
		protected ProgramMap<GLProgram> userProgram = null ;
		// Removes the need for looking up the textures 
		// referenced by MalletTexture and MalletFont.
		protected ProgramMap<GLProgram> program = null ;

		protected final int order ;						// Defines the 2D order the geometry resides on
		protected final boolean ui ;					// Is the buffer used for UI or world space?

		public BufferObject( final int _indexByteSize, final int _vertexByteSize, final GLDraw _user )
		{
			mode = _user.getMode() ;
			final BasicDraw basic = _user.getBasicData() ;
			order = basic.getOrder() ;
			ui = basic.isUI() ;

			final int indexSize = _indexByteSize / IBO_VAR_BYTE_SIZE ;
			final int vboSize = _vertexByteSize / VBO_VAR_BYTE_SIZE ;

			indicies = new short[indexSize] ;
			verticies = new float[vboSize] ;

			final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( _indexByteSize ) ;
			vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
			vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

			final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( _vertexByteSize ) ;
			indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
			indexBuffer = indexByteBuffer.asShortBuffer() ;
			
			userProgram = new ProgramMap<GLProgram>( basic.getProgram() ) ;
			final GLProgram glProgram = userProgram.getProgram() ;
			program = ( glProgram != null ) ? glProgram.buildMap( userProgram ) : null ;

			indexID = GLModelManager.genIndexID() ;
			vboID = GLModelManager.genVBOID() ;

			MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
			MGL.glBufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, _indexByteSize, null, MGL.GL_DYNAMIC_DRAW ) ;

			MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[0] ) ;
			MGL.glBufferData( MGL.GL_ARRAY_BUFFER, _vertexByteSize, null, MGL.GL_DYNAMIC_DRAW ) ;
		}

		protected void initShape( final Shape _shape )
		{
			if( _shape == null )
			{
				shapeSwivel = new Shape.Swivel[0] ;
				attributes = new VertexAttrib[0] ;
				return ;
			}

			final Shape.Swivel[] swivel = _shape.getSwivel() ;
			shapeSwivel = Arrays.copyOf( swivel, swivel.length ) ;
			attributes = constructVertexAttrib( shapeSwivel, userProgram.getProgram() ) ;

			shapeSwivelSize = calculateVertexSize( shapeSwivel ) ;
			vertexStrideBytes = shapeSwivelSize * VBO_VAR_BYTE_SIZE ;

			shapeStyle = _shape.getStyle() ;
			switch( shapeStyle )
			{
				case LINES      : style = MGL.GL_LINES ;      break ;
				case LINE_STRIP : style = MGL.GL_LINE_STRIP ; break ;
				case FILL       : style = MGL.GL_TRIANGLES ;  break ;
				default         : style = MGL.GL_LINES ;      break ;
			}
		}

		/**
			Trigger the rendering of the buffer.
		*/
		public void draw( final Matrix4 _world, final Matrix4 _ui )
		{
			if( userProgram == null )
			{
				System.out.println( "No program specified..." ) ;
				return ;
			}

			final GLProgram glProgram = userProgram.getProgram() ;
			if( glProgram == null )
			{
				System.out.println( "No OpenGL program specified..." ) ;
				return ;
			}

			if( program == null )
			{
				program = glProgram.buildMap( userProgram ) ;
				if( program == null )
				{
					return ;
				}
			}

			MGL.glEnable( MGL.GL_PRIMITIVE_RESTART_FIXED_INDEX ) ;		//GLRenderer.handleError( "Enable Primitive Restart" ) ;
			MGL.glEnable( MGL.GL_BLEND ) ;										//GLRenderer.handleError( "Enable Blend", _gl ) ;
			MGL.glBlendFunc( MGL.GL_SRC_ALPHA, MGL.GL_ONE_MINUS_SRC_ALPHA ) ;

			MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
			MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[0] ) ;

			if( dirty == true )
			{
				uploadBuffers() ;
				dirty = false ;
			}

			MGL.glUseProgram( glProgram.id[0] ) ;

			final float[] matrix = ( ui == false ) ? _world.matrix : _ui.matrix ;

			MGL.glUniformMatrix4fv( glProgram.inMVPMatrix, 1, true, matrix, 0 ) ;
			glProgram.loadUniforms( program ) ;

			glProgram.bindBuffers( program ) ;

			GLGeometryUploader.enableVertexAttributes( attributes ) ;

			GLGeometryUploader.prepareVertexAttributes( attributes, vertexStrideBytes ) ;
			MGL.glDrawElements( style, indexLength, MGL.GL_UNSIGNED_SHORT, 0 ) ;
			GLGeometryUploader.disableVertexAttributes( attributes ) ;

			MGL.glDisable( MGL.GL_BLEND ) ;
			MGL.glEnable( MGL.GL_PRIMITIVE_RESTART_FIXED_INDEX ) ;
		}

		/**
			Called during a draw operation - if the buffer object is 
			considered dirty then the buffer will be reuploaded.
		*/
		private void uploadBuffers()
		{
			final int startBytes = 0 ;

			indexBuffer.put( indicies ) ;
			indexBuffer.position( 0 ) ;
			final int indiciesLengthBytes = indicies.length * IBO_VAR_BYTE_SIZE ;
			MGL.glBufferSubData( MGL.GL_ELEMENT_ARRAY_BUFFER, startBytes, indiciesLengthBytes, indexBuffer ) ;

			vertexBuffer.put( verticies ) ;
			vertexBuffer.position( 0 ) ;
			final int verticiesLengthBytes = verticies.length * VBO_VAR_BYTE_SIZE ;
			MGL.glBufferSubData( MGL.GL_ARRAY_BUFFER, startBytes, verticiesLengthBytes, vertexBuffer ) ;
		}

		public abstract void upload( final Location<BufferObject, GLDraw> _location ) ;

		public void setIndexLength( final int _indexLengthInBytes )
		{
			indexLength = _indexLengthInBytes / IBO_VAR_BYTE_SIZE ;
		}

		public void destroy()
		{
			MGL.glDeleteBuffers( 1, indexID, 0 ) ;
			MGL.glDeleteBuffers( 1, vboID, 0 ) ;
		}

		/**
			Determine whether or not this BufferObject supports
			the requirements of the GLRenderData.

			GLBuffers will batch together similar content to 
			improve rendering performance.

			They will use layer, texture, shape swivel and style
			to determine if the buffer can support the data.
		*/
		public boolean isSupported( final GLDraw _user )
		{
			if( mode != _user.getMode() )
			{
				//System.out.println( "Mode: " + mode + " " + _user.getMode() ) ;
				return false ;
			}

			final BasicDraw basic = _user.getBasicData() ;
			if( order != basic.getOrder() )
			{
				//System.out.println( "Order: " + order + " " +  _user.getOrder() ) ;
				return false ;
			}

			if( ui != basic.isUI() )
			{
				//System.out.println( "UI: " + ui + " " +  _user.isUI() ) ;
				return false ;
			}

			if( isProgram( basic.getProgram() ) == false )
			{
				//System.out.println( "Programs are different" ) ;
				return false ;
			}

			if( mode != GLDraw.Mode.TEXT )
			{
				if( isShape( _user.getShape() ) == false )
				{
					return false ;
				}
			}

			return true ;
		}

		private boolean isShape( final Shape _shape )
		{
			if( _shape == null )
			{
				//System.out.println( "Requires shape" ) ;
				return false ;
			}

			if( shapeStyle != _shape.getStyle() )
			{
				//System.out.println( "Shape Style: " + shapeStyle + " " +  _shape.getStyle() ) ;
				return false ;
			}

			final Shape.Swivel[] sw = _shape.getSwivel() ;
			if( shapeSwivel.length != sw.length )
			{
				//System.out.println( "Shape Swivel Length: " + shapeSwivel.length + " " +  sw.length ) ;
				return false ;
			}

			for( int i = 0; i < sw.length; i++ )
			{
				if( shapeSwivel[i] != sw[i] )
				{
					//System.out.println( "Shape Swivel: " + shapeSwivel[i] + " " +  sw[i] ) ;
					return false ;
				}
			}

			return true ;
		}

		private boolean isProgram( final ProgramMap<GLProgram> _program )
		{
			// Checking to see if the program matches up with 
			// the program used by the buffer is expensive.
			// We only check to see if the program is valid if it's 
			// flagged as dirty.
			// As only modified/new programs will be flagged as dirty.
			if( _program.isDirty() == true )
			{
				final boolean valid = userProgram.equals( _program ) ;
				// The program should only be flagged as not dirty 
				// once a valid buffer has been found.
				_program.setDirty( valid ? false : true ) ;
				return valid ;
			}

			return true ;
		}
	}

	private class BasicObject extends BufferObject
	{
		private final MalletColour shapeColour = new MalletColour() ;
		private final Vector2 uv = new Vector2() ;
		private final Vector3 point = new Vector3() ;
		private final Vector3 temp = new Vector3() ;

		private final Matrix4 matrix = new Matrix4() ;
		private final Matrix4 matrixTemp = Matrix4.createTempIdentity() ;

		private final Vector3 position = new Vector3() ;
		private final Vector3 offset = new Vector3() ;
		private final Vector3 rotation = new Vector3() ;
		private final Vector3 scale = new Vector3() ;

		public BasicObject( final int _indexByteSize, final int _vertexByteSize, final GLDraw _user )
		{
			super( _indexByteSize, _vertexByteSize, _user ) ;
			initShape( _user.getShape() ) ;
		}

		@Override
		public void upload( final Location<BufferObject, GLDraw> _location )
		{
			final GLDraw draw = _location.getLocationData() ;
			final BasicDraw basic = draw.getBasicData() ;
			final Shape shape = draw.getShape() ;

			basic.getPosition( position ) ;
			basic.getOffset( offset ) ;
			basic.getRotation( rotation ) ;
			basic.getScale( scale ) ;

			apply( matrix, matrixTemp, position, offset, rotation, scale ) ;

			uploadIndexToRAM( _location, shape ) ;
			uploadVBOToRAM( _location, shape, matrix ) ;

			dirty = true ;
		}

		private void uploadIndexToRAM( final Location<BufferObject, GLDraw> _handler, final Shape _shape )
		{
			final BufferObject buffer = _handler.getBufferData() ;
		
			final Location.Range indexRange = _handler.getIndex() ;
			final Location.Range vertRange = _handler.getVertex() ;

			final int indexOffset = vertRange.getStart() / buffer.vertexStrideBytes ;
			int increment = indexRange.getStart() / IBO_VAR_BYTE_SIZE ;

			final int size = _shape.getIndexSize() ;
			for( int i = 0; i < size; i++ )
			{
				indicies[increment++] = ( short )( indexOffset + _shape.getIndex( i ) ) ;
			}

			indicies[increment++] = ( short )PRIMITIVE_RESTART_INDEX ;
		}

		private void uploadVBOToRAM( final Location<BufferObject, GLDraw> _handler, final Shape _shape, final Matrix4 _matrix )
		{
			final BufferObject buffer = _handler.getBufferData() ;

			final Shape.Swivel[] swivel = _shape.getSwivel() ;
			//final int vertexSize = buffer.shapeSwivelSize ;
			final int verticiesSize = _shape.getVertexSize() ;

			final Location.Range vertRange = _handler.getVertex() ;

			int increment = vertRange.getStart() / VBO_VAR_BYTE_SIZE ;

			for( int i = 0; i < verticiesSize; i++ )
			{
				for( int j = 0; j < swivel.length; j++ )
				{
					switch( swivel[j] )
					{
						case NORMAL :
						case POINT  :
						{
							_shape.getVector3( i, j, point ) ;
							Matrix4.multiply( point, _matrix, temp ) ;
							verticies[increment++] = temp.x ;
							verticies[increment++] = temp.y ;
							verticies[increment++] = temp.z ;
							break ;
						}
						case COLOUR :
						{
							_shape.getColour( i, j, shapeColour ) ;
							verticies[increment++] = getABGR( shapeColour ) ;
							break ;
						}
						case UV     :
						{
							_shape.getVector2( i, j, uv ) ;
							verticies[increment++] = uv.x ;
							verticies[increment++] = uv.y ;
							break ;
						}
					}
				}
			}
		}
	}

	private class TextObject extends BufferObject
	{
		private final MalletColour shapeColour = new MalletColour() ;
		private final Vector2 uv = new Vector2() ;
		private final Vector3 point = new Vector3() ;
		private final Vector3 temp = new Vector3() ;

		private final MalletFont font ;
		private final MalletFont.Metrics metrics ;
		private final GLFont glFont ;

		private final Matrix4 matrix = new Matrix4() ;
		private final Matrix4 matrixTemp = Matrix4.createTempIdentity() ;

		private final Vector3 position = new Vector3() ;
		private final Vector3 offset = new Vector3() ;
		private final Vector3 rotation = new Vector3() ;
		private final Vector3 scale = new Vector3( 1, 1, 1 ) ;

		public TextObject( final int _indexByteSize, final int _vertexByteSize, final GLDraw _user )
		{
			super( _indexByteSize, _vertexByteSize, _user ) ;
			font = userProgram.getUniform( "inTex0", MalletFont.class ) ;
			metrics = font.getMetrics() ;
			glFont = GLRenderer.getFont( font ) ;

			initShape( glFont.getShapeWithChar( '\0' ) ) ;
		}
	
		@Override
		public void upload( final Location<BufferObject, GLDraw> _location )
		{
			dirty = true ;
			final GLDraw draw = _location.getLocationData() ;
			final BasicDraw basic = draw.getBasicData() ;
			final TextData txtDraw = draw.getTextData() ;
			final BufferObject buffer = _location.getBufferData() ;

			basic.getPosition( position ) ;
			basic.getOffset( offset ) ;
			basic.getRotation( rotation ) ;

			apply( matrix, matrixTemp, position, offset, rotation, scale ) ;

			final MalletColour colour = basic.getColour() ;

			final Location.Range indexRange = _location.getIndex() ;
			final Location.Range vertRange = _location.getVertex() ;

			final int initialIndexOffset = vertRange.getStart() / buffer.vertexStrideBytes ;

			int indexInc = indexRange.getStart() / IBO_VAR_BYTE_SIZE ;
			int vertexInc = vertRange.getStart() / VBO_VAR_BYTE_SIZE ;

			final StringBuilder text = txtDraw.getText() ;
			final int start = txtDraw.getTextStart() ;
			final int end = txtDraw.getTextEnd() ;

			final int length = end - start ;
			for( int i = 0; i < length; i++ )
			{
				final char c = text.charAt( start + i ) ;

				final Glyph glyph = metrics.getGlyphWithChar( c ) ;
				final Shape shape = glFont.getShapeWithChar( c ) ;
				if( shape == null )
				{
					//System.out.println( "Missing: " + c + " Glyph: " + glyph ) ;
					// If a shape does not exist then the GLFont 
					// needs to be recreated as the MalletFont has a 
					// new glyph that is not yet represented.
					// We need to update all text as the texture co-ordinates 
					// used by the previously uploaded text will now be wrong.
					//GLGeometryUploader.this.forceUpdate() ;
					continue ;
				}

				final Shape.Swivel[] swivel = shape.getSwivel() ;
				//final int vertexSize = calculateVertexSize( swivel ) ;
				final int verticiesSize = shape.getVertexSize() ;

				final int GLYPH_POINTS = 4 ;
				final int indexOffset = initialIndexOffset + ( i * GLYPH_POINTS ) ;

				final int size = shape.getIndexSize() ; 
				for( int j = 0; j < size; j++ )
				{
					indicies[indexInc++] = ( short )( indexOffset + shape.getIndex( j ) ) ;
				}

				for( int j = 0; j < verticiesSize; j++ )
				{
					for( int k = 0; k < swivel.length; k++ )
					{
						switch( swivel[k] )
						{
							case NORMAL :
							case POINT  :
							{
								shape.getVector3( j, k, point ) ;
								Matrix4.multiply( point, matrix, temp ) ;
								verticies[vertexInc++] = temp.x ;
								verticies[vertexInc++] = temp.y ;
								verticies[vertexInc++] = temp.z ;
								break ;
							}
							case COLOUR :
							{
								// GLDraw colour overrides Shapes colour.
								final MalletColour col = ( colour != null ) ? colour : shape.getColour( j, k, shapeColour ) ;
								verticies[vertexInc++] = getABGR( col ) ;
								break ;
							}
							case UV     :
							{
								shape.getVector2( j, k, uv ) ;
								verticies[vertexInc++] = uv.x ;
								verticies[vertexInc++] = uv.y ;
								break ;
							}
						}
					}
				}

				matrix.translate( glyph.getWidth(), 0.0f, 0.0f ) ;
			}

			indicies[indexInc++] = ( short )PRIMITIVE_RESTART_INDEX ;
		}
	}

	private static class LocationBufferListener implements LocationBuffer.Listener<BufferObject, GLDraw>
	{
		@Override
		public boolean isSupported( final BufferObject _buffer, final GLDraw _user )
		{
			return _buffer.isSupported( _user ) ;
		}

		@Override
		public void allocated( final Location<BufferObject, GLDraw> _location, final GLDraw _user )
		{
			_user.setNewLocation( _location ) ;
			_location.setLocationData( _user ) ;

			final BufferObject buffer = _location.getBufferData() ;
			buffer.upload( _location ) ;
			buffer.setIndexLength( _location.getIndex().getEnd() ) ;
		}

		@Override
		public void deallocated( final Location<BufferObject, GLDraw> _location )
		{
			//System.out.println( "Location deallocated" ) ;
			_location.getLocationData().setNewLocation( null ) ;

			// Reset the program so it is checked fully 
			// if the draw is readded to the renderer.
			final GLDraw draw = _location.getLocationData() ;
			final BasicDraw<GLProgram> basic = draw.getBasicData() ;
			final ProgramMap<GLProgram> program = basic.getProgram() ;
			program.dirty() ;
		}

		@Override
		public void shifted( final Location<BufferObject, GLDraw> _location )
		{
			// The location has been moved to a new location within 
			// the buffer object and needs to be reuploaded.
			final BufferObject buffer = _location.getBufferData() ;
			buffer.upload( _location ) ;

			//System.out.println( "Location has been shifted " + _location.getBufferData().mode ) ;
			//_location.getLocationData().forceUpdate() ;
		}

		@Override
		public void shiftEnded( final LocationBuffer<BufferObject, GLDraw> _buffer )
		{
			final BufferObject buffer = _buffer.getData() ;
			buffer.setIndexLength( _buffer.getByteIndexLength() ) ;
		}
	}
 
	private class BuffersListener implements Buffers.Listener<BufferObject, GLDraw>
	{
		@Override
		public void draw( final Matrix4 _world, final Matrix4 _ui, final LocationBuffer<BufferObject, GLDraw> _buffer )
		{
			final BufferObject buffer = _buffer.getData() ;
			buffer.draw( _world, _ui ) ;
		}

		@Override
		public int calculateIndexByteSize( final GLDraw _user )
		{
			return GLGeometryUploader.calculateIndexByteSize( _user ) ;
		}

		@Override
		public int calculateVertexByteSize( final GLDraw _user )
		{
			return GLGeometryUploader.calculateVertexByteSize( _user ) ;
		}

		@Override
		public void allocated( final LocationBuffer<BufferObject, GLDraw> _allocated, final GLDraw _user )
		{
			_allocated.setListener( new LocationBufferListener() ) ;

			final int indexSize = buffers.getMaximumByteIndex() ;
			final int vertexSize = buffers.getMaximumByteVertex() ;

			final BasicDraw basic = _user.getBasicData() ;
			_allocated.setOrder( basic.getOrder() ) ;

			switch( _user.getMode() )
			{
				case TEXT  : _allocated.setData( new TextObject( indexSize, vertexSize, _user ) ) ; break ;
				case BASIC : _allocated.setData( new BasicObject( indexSize, vertexSize, _user ) ) ; break ;
			}
		}

		@Override
		public void deallocated( final BufferObject _data )
		{
			_data.destroy() ;
		}
	}
 
	private final Buffers<BufferObject, GLDraw> buffers = new Buffers<BufferObject, GLDraw>( 50000, 50000, new BuffersListener() ) ;

	private final byte[] abgrTemp = new byte[4] ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize ) {}

	/**
		Draw the uploaded geometry.
	*/
	public void draw( final Matrix4 _worldProjection, final Matrix4 _uiProjection )
	{
		buffers.draw( _worldProjection, _uiProjection ) ;
	}

	/**
		Upload geometry and have it prepared for drawing.
	*/
	public void upload( final GLDraw _data )
	{
		final Location<BufferObject, GLDraw> location = _data.getNewLocation() ;
		if( location == null )
		{
			buffers.allocate( _data ) ;
			return ;
		}

		if( isLocationValid( location ) == true )
		{
			final BufferObject buffer = location.getBufferData() ;
			buffer.upload( location ) ;
		}
		else
		{
			location.deallocate() ;
			buffers.allocate( _data ) ;
		}
	}

	/**
		Find the GLBuffer/GLGeometry that the GLRenderData resides in.
		Remove it from the buffers, and pack the index buffer.
	*/
	public void remove( final GLDraw _data )
	{
		final Location<BufferObject, GLDraw> location = _data.getNewLocation() ;
		if( location != null )
		{
			location.deallocate() ;
		}
	}

	/**
		Destroy all buffers allocated in OpenGL.
	*/
	public void shutdown() {}

	/**
		Remove any LocationBuffers that do not contain any geometry.
	*/
	public void clean() {}

	private static boolean isLocationValid( final Location<BufferObject, GLDraw> _location )
	{
		final GLDraw draw = _location.getLocationData() ;
		final Location.Range indexRange = _location.getIndex() ;
		if( calculateIndexByteSize( draw ) != indexRange.size() )
		{
			//System.out.println( "Incorrect Index Range." ) ;
			return false ;
		}

		final Location.Range vertRange = _location.getVertex() ;
		if( calculateVertexByteSize( draw ) != vertRange.size() )
		{
			//System.out.println( "Incorrect Vertex Range." ) ;
			return false ;
		}

		final BufferObject buffer = _location.getBufferData() ;
		if( buffer.isSupported( draw ) == false )
		{
			//System.out.println( "Buffer no longer supported." ) ;
			return false ;
		}

		return true ;
	}

	private static int calculateIndexByteSize( final GLDraw _user )
	{
		final GLDraw.Mode mode = _user.getMode() ;
		switch( mode )
		{
			case BASIC   : return calculateBasicIndexByteSize( _user ) ;
			case TEXT    : return calculateTextIndexByteSize( _user ) ;
			case STENCIL :
			case DEPTH   :
			default      : return 0 ;
		}
	}

	private static int calculateBasicIndexByteSize( final GLDraw _user )
	{
		final Shape shape = _user.getShape() ;
		return ( shape.getIndexSize() + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
	}

	private static int calculateTextIndexByteSize( final GLDraw _user )
	{
		final TextData text = _user.getTextData() ;
		final int length = text.getTextEnd() - text.getTextStart() ;
		return ( ( length * 6 ) + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
	}

	private static int calculateVertexByteSize( final GLDraw _user )
	{
		final GLDraw.Mode mode = _user.getMode() ;
		switch( mode )
		{
			case BASIC   : return calculateBasicVertexByteSize( _user ) ;
			case TEXT    : return calculateTextVertexByteSize( _user ) ;
			case STENCIL :
			case DEPTH   :
			default      : return 0 ;
		}
	}

	private static int calculateBasicVertexByteSize( final GLDraw _user )
	{
		final Shape shape = _user.getShape() ;
		return ( shape.getVertexSize() * calculateVertexSize( shape.getSwivel() ) ) * VBO_VAR_BYTE_SIZE ;
	}

	private static int calculateTextVertexByteSize( final GLDraw _user )
	{
		final BasicDraw basic = _user.getBasicData() ;
		final TextData text = _user.getTextData() ;
	
		final ProgramMap<GLProgram> program = basic.getProgram() ;
		final MalletFont font = program.getUniform( "inTex0", MalletFont.class ) ;
		final GLFont glFont = GLRenderer.getFont( font ) ;
		final Shape shape = glFont.getShapeWithChar( '\0' ) ;

		final int length = text.getTextEnd() - text.getTextStart() ;
		return ( ( shape.getVertexSize() * length ) * calculateVertexSize( shape.getSwivel() ) ) * VBO_VAR_BYTE_SIZE ;
	}

	private static VertexAttrib[] constructVertexAttrib( final Shape.Swivel[] _swivel, final GLProgram _program )
	{
		final VertexAttrib[] attributes = new VertexAttrib[_swivel.length] ;

		int offset = 0 ;
		for( int i = 0; i < _swivel.length; i++ )
		{
			switch( _swivel[i] )
			{
				case POINT  :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, MGL.GL_FLOAT, false, offset ) ;
					offset += 3 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 4, MGL.GL_UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 2, MGL.GL_FLOAT, false, offset ) ;
					offset += 2 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, MGL.GL_FLOAT, false, offset ) ;
					offset += 3 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
			}
		}

		return attributes ;
	}

	private static int calculateVertexSize( final Shape.Swivel[] _swivel )
	{
		int size = 0 ;
		for( int j = 0; j < _swivel.length; j++ )
		{
			switch( _swivel[j] )
			{
				case POINT  : size += 3 ; break ;
				case COLOUR : size += 1 ; break ;
				case UV     : size += 2 ; break ;
				case NORMAL : size += 3 ; break ;
			}
		}

		return size ;
	}

	private static void apply( final Matrix4 _mat4,
							   final Matrix4 _temp,
							   final Vector3 _position,
							   final Vector3 _offset,
							   final Vector3 _rotation,
							   final Vector3 _scale )
	{
		_mat4.setIdentity() ;
		_mat4.setTranslate( _position.x, _position.y, 0.0f ) ;

		_temp.setRotateX( _rotation.x ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;

		_temp.setRotateY( _rotation.y ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;

		_temp.setRotateZ( _rotation.z ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;

		_temp.setScale( _scale.x, _scale.y, _scale.z ) ;
		_temp.setTranslate( _offset.x, _offset.y, _offset.z ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;
	}

	public static class VertexAttrib
	{
		public int index ;				// Specifies the index of the generic vertex attribute to be modified 
		public int size ;				// Specifies the number of components per generic vertex attribute
		public int type ;				// Specifies the data type ;
		public boolean normalised ;		// Specifies whether fixed-point data values should be normalized
		public int offset ;				// Specifies the offset for the first component

		public VertexAttrib( final int _index, final int _size, final int _type, final boolean _normalised, final int _offset )
		{
			index = _index ;
			size = _size ;
			type = _type ;
			normalised = _normalised ;
			offset = _offset ; 
		}

		public String toString()
		{
			final StringBuilder buffer = new StringBuilder() ;
			buffer.append( "Index: " ) ;
			buffer.append( index ) ;
			buffer.append( " Size: " ) ;
			buffer.append( size ) ;
			buffer.append( " Norm: " ) ;
			buffer.append( normalised ) ;
			buffer.append( " Offset: " ) ;
			buffer.append( offset ) ;

			return buffer.toString() ;
		}
	}

	private static void enableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.glEnableVertexAttribArray( att.index ) ;
		}
	}

	private static void prepareVertexAttributes( final VertexAttrib[] _atts, final int _stride )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.glVertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}

	private static void disableVertexAttributes( final VertexAttrib[] _atts )
	{
		for( int i = 0; i < _atts.length; i++ )
		{
			final VertexAttrib att = _atts[i] ;
			MGL.glDisableVertexAttribArray( att.index ) ;
		}
	}

	private float getABGR( final MalletColour _colour )
	{
		abgrTemp[0] = _colour.colours[MalletColour.ALPHA] ;
		abgrTemp[1] = _colour.colours[MalletColour.BLUE] ;
		abgrTemp[2] = _colour.colours[MalletColour.GREEN] ;
		abgrTemp[3] = _colour.colours[MalletColour.RED] ;

		return ConvertBytes.toFloat( abgrTemp, 0 ) ;
	}
}
