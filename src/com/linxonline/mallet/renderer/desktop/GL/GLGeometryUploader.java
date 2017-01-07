package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.Iterator ;
import java.util.List ;
import java.util.Arrays ;
import java.nio.* ;

import javax.media.opengl.* ;

import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Shape.Swivel ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.texture.* ;
import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.util.worker.* ;
import com.linxonline.mallet.util.caches.ObjectCache ;
import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.util.sort.OrderedInsert ;
import com.linxonline.mallet.util.sort.SortInterface ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class GLGeometryUploader
{
	protected final static ObjectCache<Location> locationCache = new ObjectCache<Location>( Location.class ) ;

	public final static int PRIMITIVE_RESTART_INDEX = 0xFFFFFF ;
	private final static int PRIMITIVE_EXPANSION = 1 ;

	private final static int VBO_VAR_BYTE_SIZE = 4 ;
	private final static int IBO_VAR_BYTE_SIZE = 4 ;

	private final int[] indicies ;
	private final float[] verticies ;
	private int programID = -1 ;

	private final IntBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	private final List<GLBuffer> buffers = MalletList.<GLBuffer>newList()  ;							// Available GLBuffers

	private final MalletColour shapeColour = new MalletColour() ;
	private final Vector2 uv = new Vector2() ;
	private final Vector3 point = new Vector3() ;
	private final Vector3 temp = new Vector3() ;
	private final byte[] abgrTemp = new byte[4] ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize )
	{
		indicies = new int[_indexSize] ;
		verticies = new float[_vboSize] ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( _vboSize * VBO_VAR_BYTE_SIZE ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( _indexSize * IBO_VAR_BYTE_SIZE ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;
	}

	/**
		Draw the uploaded geometry.
	*/
	public void draw( final GL3 _gl, final Matrix4 _worldProjection, final Matrix4 _uiProjection )
	{
		for( final GLBuffer buffer : buffers )
		{
			buffer.draw( _gl, _worldProjection, _uiProjection ) ;
		}
	}

	/**
		Upload geometry and have it prepared for drawing.
	*/
	public void upload( final GL3 _gl, final GLDrawData _data )
	{
		GLBuffer buffer = _data.getGLBuffer() ;
		if( buffer != null )
		{
			if( buffer.isSupported( _data ) == true )
			{
				// If the data is still supported in the buffer 
				// it was previously loaded into then update it.
				buffer.upload( _gl, _data ) ;
				return ;
			}
			else
			{
				remove( _gl, _data ) ;
			}
		}

		// If the draw data no longer fits with the previously 
		// used buffer find or create a new buffer that matches.
		buffer = getSupportedBuffer( _data ) ;
		_data.setGLBuffer( buffer ) ;

		buffer.upload( _gl, _data ) ;
	}

	/**
		Find the GLBuffer/GLGeometry that the GLRenderData resides in.
		Remove it from the buffers, and pack the index buffer.
	*/
	public void remove( final GL3 _gl, final GLDrawData _data )
	{
		GLBuffer buffer = _data.getGLBuffer() ;
		if( buffer != null )
		{
			buffer.remove( _gl, _data ) ;
			_data.setGLBuffer( null ) ;
			_data.setLocation( null ) ;
		}
	}

	/**
		Destroy all buffers allocated in OpenGL.
	*/
	public void shutdown()
	{
		for( final GLBuffer buffer : buffers )
		{
			buffer.destroy() ;
		}
		buffers.clear() ;
	}

	/**
		Remove any GLBuffers that do not contain any geometry.
	*/
	public void clean()
	{
		final Iterator<GLBuffer> i = buffers.iterator() ;
		while( i.hasNext() == true )
		{
			final GLBuffer buffer = i.next() ;
			buffer.clean() ;
			if( buffer.containsGeometry() == false )
			{
				buffer.destroy() ;
				i.remove() ;
			}
		}
	}

	protected void uploadIndex( final GL3 _gl, final Location _handler, final Shape _shape )
	{
		final GLGeometry geometry = _handler.getGeometry() ;

		final int indexOffset = _handler.getVertexStart() / geometry.vertexStrideBytes ;

		int increment = 0 ;
		int indexStartBytes = _handler.getIndexStart() ;

		final int size = _shape.getIndexSize() ;
		for( int i = 0; i < size; i++ )
		{
			//System.out.println( "Index: " + _shape.getIndex( i ) + " With Offset: " + ( indexOffset + _shape.getIndex( i ) ) ) ;
			indicies[increment++] = indexOffset + _shape.getIndex( i ) ;

			if( increment >= indicies.length )
			{
				// Buffer is full needs to be passed to GPU now
				indexBuffer.put( indicies ) ;
				indexBuffer.position( 0 ) ;

				final int lengthBytes = indicies.length * IBO_VAR_BYTE_SIZE ;
				_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexStartBytes, lengthBytes, indexBuffer ) ;

				indexStartBytes += lengthBytes ;
				increment = 0 ;
			}
		}

		indicies[increment++] = PRIMITIVE_RESTART_INDEX ;

		indexBuffer.put( indicies ) ;
		indexBuffer.position( 0 ) ;

		_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexStartBytes, increment * IBO_VAR_BYTE_SIZE, indexBuffer ) ;
		//GLRenderer.handleError( "Index Buffer Sub Data: ", _gl ) ;
	}

	protected void uploadVBO( final GL3 _gl, final Location _handler, final Shape _shape, final Matrix4 _matrix )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = calculateVertexSize( swivel ) ;
		final int verticiesSize = _shape.getVertexSize() ;

		int increment = 0 ;
		int vertexStartBytes = _handler.getVertexStart() ;

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

			// If verticies does not have enough space to store 
			// another vertex, upload it to the GPU before continuing.
			if( ( increment + vertexSize ) >= verticies.length )
			{
				vertexBuffer.put( verticies ) ;
				vertexBuffer.position( 0 ) ;

				final int lengthBytes = increment * VBO_VAR_BYTE_SIZE ;
				_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, vertexStartBytes, lengthBytes, vertexBuffer ) ;
				//GLRenderer.handleError( "Vertex Buffer Sub Data: ", _gl ) ;
				
				vertexStartBytes += lengthBytes ;
				increment = 0 ;
			}
		}

		if( increment > 0 )
		{
			vertexBuffer.put( verticies ) ;
			vertexBuffer.position( 0 ) ;

			_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, vertexStartBytes, increment * VBO_VAR_BYTE_SIZE, vertexBuffer ) ;
			//GLRenderer.handleError( "Vertex Buffer Sub Data: ", _gl ) ;
		}
	}

	/**
		Find a GLBuffer that supports the texture, swivel, and layer
		of the datat passed in.
		If a GLBuffer doesn't exist create one.
	*/
	private GLBuffer getSupportedBuffer( final GLDrawData _data )
	{
		for( final GLBuffer buffer : buffers )
		{
			if( buffer.isSupported( _data ) == true )
			{
				return buffer ;
			}
		}

		GLBuffer buffer = null ;
		if( _data.getText() != null )
		{
			// Use _data as initial parameters for this buffer.
			buffer = new GLTextBuffer( _data, indicies.length * IBO_VAR_BYTE_SIZE, verticies.length * VBO_VAR_BYTE_SIZE ) ;
		}
		else
		{
			// Use _data as initial parameters for this buffer.
			buffer = new GLGeometryBuffer( _data, indicies.length * IBO_VAR_BYTE_SIZE, verticies.length * VBO_VAR_BYTE_SIZE ) ;
		}
		
		OrderedInsert.insert( buffer, buffers ) ;
		return buffer ;
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
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, GL3.GL_FLOAT, false, offset ) ;
					offset += 3 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 4, GL3.GL_UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 2, GL3.GL_FLOAT, false, offset ) ;
					offset += 2 * VBO_VAR_BYTE_SIZE ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( _program.inAttributes[i], 3, GL3.GL_FLOAT, false, offset ) ;
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
	} ;

	/**
		Handles the geometry and index buffers for 
		a particular set of vertex attributes and style.
		GLBuffer will generate more GLGeometry buffers 
		when the existing buffers are full.
	*/
	public abstract class GLBuffer implements SortInterface
	{
		protected Shape.Swivel[] shapeSwivel ;
		protected Shape.Style shapeStyle ;

		protected VertexAttrib[] attributes ;
		protected final int style ;						// OpenGL GL_TRIANGLES, GL_LINES, 
		protected final int indexLengthBytes ;
		protected final int vertexLengthBytes ;
		protected final int vertexStrideBytes ;			// Specifies the byte offset between verticies

		protected ProgramMap<GLProgram> program ;		// What shader should be used
		protected final int layer ;						// Defines the 2D layer the geometry resides on
		protected final boolean ui ;					// Is the buffer used for UI or world space?

		protected GLProgram stencilProgram         = null ;	// Stencil is applied to all geometry located in buffers
		protected Shape stencilShape               = null ;
		protected VertexAttrib[] stencilAttributes = null ;
		protected Matrix4 stencilMatrix            = null ;

		protected Location stencilLocation = null ;

		protected final List<GLGeometry> buffers = MalletList.<GLGeometry>newList()  ;

		public GLBuffer( final GLDrawData _data,
						 final int _indexLengthBytes,
						 final int _vertexLengthBytes )
		{
			final Shape shape = _data.getDrawShape() ;
			final Shape.Swivel[] swivel = shape.getSwivel() ;

			layer   = _data.getOrder() ;
			program = ( ProgramMap<GLProgram> )_data.getProgram() ;
			ui      = _data.isUI() ;

			shapeSwivel = Arrays.copyOf( swivel, swivel.length ) ;
			attributes = constructVertexAttrib( shapeSwivel, program.getProgram() ) ;

			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;
			vertexStrideBytes = calculateVertexSize( shapeSwivel ) * VBO_VAR_BYTE_SIZE ;

			shapeStyle = shape.getStyle() ;
			switch( shapeStyle )
			{
				case LINES      : style = GL3.GL_LINES ;      break ;
				case LINE_STRIP : style = GL3.GL_LINE_STRIP ; break ;
				case FILL       : style = GL3.GL_TRIANGLES ;  break ;
				default         : style = GL3.GL_LINES ;      break ;
			}

			setupStencil( _data ) ;
		}

		public void draw( final GL3 _gl, final Matrix4 _worldProjection, final Matrix4 _uiProjection )
		{
			if( program == null )
			{
				System.out.println( "No program specified..." ) ;
				return ;
			}

			final GLProgram glProgram = program.getProgram() ;
			if( glProgram == null )
			{
				System.out.println( "No OpenGL program specified..." ) ;
				return ;
			}

			final float[] matrix = ( ui == false ) ? _worldProjection.matrix : _uiProjection.matrix ;
			if( stencilLocation != null )
			{
				drawStencil( _gl, matrix ) ;
			}

			if( glProgram.id[0] != programID )
			{
				// Only call glUseProgram if the last program used 
				// doesn't match what we want.
				// Ordering draws by program could improve rendering performance.
				programID = glProgram.id[0] ;
				_gl.glUseProgram( programID ) ;										//GLRenderer.handleError( "Use Program", _gl ) ;
			}

			_gl.glUniformMatrix4fv( glProgram.inMVPMatrix, 1, true, matrix, 0 ) ;		//GLRenderer.handleError( "Load Matrix", _gl ) ;
			if( glProgram.loadUniforms( _gl, program ) == false )
			{
				// We failed to load all uniforms required for 
				// this buffer.
				return ;
			}

			GLGeometryUploader.enableVertexAttributes( _gl, attributes ) ;
			for( final GLGeometry geometry : buffers )
			{
				_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;	//GLRenderer.handleError( "Draw Bind Index: ", _gl ) ;
				_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Draw Bind Vertex: ", _gl ) ;

				GLGeometryUploader.prepareVertexAttributes( _gl, attributes, vertexStrideBytes ) ;
				_gl.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.GL_UNSIGNED_INT, 0 ) ;
				//GLRenderer.handleError( "Draw Elements: ", _gl ) ;
			}
			GLGeometryUploader.disableVertexAttributes( _gl, attributes ) ;

			if( stencilLocation != null )
			{
				_gl.glDisable( GL3.GL_STENCIL_TEST ) ;		//GLRenderer.handleError( "Disable Stencil", _gl ) ;
			}
		}

		public abstract void upload( final GL3 _gl, final GLDrawData _data ) ;
		protected abstract void expand( final int _indexLengthBytes, final int _vertexLengthBytes ) ;

		public void remove( final GL3 _gl, final GLDrawData _data )
		{
			final Location location = _data.getLocation() ;
			if( location != null )
			{
				location.getGeometry().remove( _gl, location ) ;
			}
		}

		public boolean containsGeometry()
		{
			return buffers.isEmpty() == false ;
		}

		/**
			Determine whether or not this GLBuffer supports
			the requirements of the GLRenderData.

			GLBuffers will batch together similar content to 
			improve rendering performance.

			They will use layer, texture, shape swivel and style
			to determine if the buffer can support the data.
		*/
		public boolean isSupported( final GLDrawData _data )
		{
			if( layer != _data.getOrder() )
			{
				return false ;
			}

			if( ui != _data.isUI() )
			{
				return false ;
			}

			if( stencilShape != _data.getClipShape() )
			{
				return false ;
			}

			if( isProgram( ( ProgramMap<GLProgram> )_data.getProgram() ) == false )
			{
				return false ;
			}

			if( isShape( _data.getDrawShape() ) == false )
			{
				return false ;
			}

			return true ;
		}

		private boolean isShape( final Shape _shape )
		{
			if( shapeStyle != _shape.getStyle() )
			{
				return false ;
			}

			final Shape.Swivel[] sw = _shape.getSwivel() ;
			if( shapeSwivel.length != sw.length )
			{
				return false ;
			}

			for( int i = 0; i < sw.length; i++ )
			{
				if( shapeSwivel[i] != sw[i] )
				{
					return false ;
				}
			}

			return true ;
		}

		private boolean isProgram( final ProgramMap<GLProgram> _program )
		{
			return program.equals( _program ) ;
		}

		private void drawStencil( final GL3 _gl, final float[] _projectionMatrix )
		{
			if( stencilProgram.id[0] != programID )
			{
				// Only call UseProgram if the last program used 
				// doesn't match what we want.
				programID = stencilProgram.id[0] ;
				_gl.glUseProgram( programID ) ;										//GLRenderer.handleError( "Use Program", _gl ) ;
			}

			_gl.glUniformMatrix4fv( stencilProgram.inMVPMatrix, 1, true, _projectionMatrix, 0 ) ;					//GLRenderer.handleError( "Load Matrix", _gl ) ;

			// Don't render the element to the colour buffer
			_gl.glColorMask( false, false, false, false ) ;
			_gl.glEnable( GL3.GL_STENCIL_TEST ) ;

			_gl.glStencilMask( 0xFF ) ;
			_gl.glClear( GL3.GL_STENCIL_BUFFER_BIT ) ;

			_gl.glStencilFunc( GL3.GL_NEVER, 1, 0xFF ) ;
			_gl.glStencilOp( GL3.GL_REPLACE, GL3.GL_KEEP, GL3.GL_KEEP ) ;

			GLGeometryUploader.enableVertexAttributes( _gl, stencilAttributes ) ;

			final GLGeometry geometry = stencilLocation.getGeometry() ;
			_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;		//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;					//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			GLGeometryUploader.this.uploadIndex( _gl, stencilLocation, stencilShape ) ;
			GLGeometryUploader.this.uploadVBO( _gl, stencilLocation, stencilShape, stencilMatrix ) ;

			GLGeometryUploader.prepareVertexAttributes( _gl, stencilAttributes, geometry.getStride() ) ;
			_gl.glDrawElements( geometry.getStyle(), geometry.getIndexLength(), GL3.GL_UNSIGNED_INT, 0 ) ;
			//GLRenderer.handleError( "Draw Elements: ", _gl ) ;
			GLGeometryUploader.disableVertexAttributes( _gl, stencilAttributes ) ;

			_gl.glColorMask( true, true, true, true ) ;		// Re-enable colour buffer
			_gl.glStencilFunc( GL3.GL_EQUAL, 1, 1 ) ;
			// continue rendering scene...
		}

		/**
			A buffer can support one stencil.
		*/
		private void setupStencil( final GLDrawData _data )
		{
			stencilShape = _data.getClipShape() ;
			if( stencilShape != null )
			{
				final Shape.Swivel[] swivel = stencilShape.getSwivel() ;
				stencilAttributes = constructVertexAttrib( swivel, stencilProgram ) ;

				final int vertexStrideBytes = calculateVertexSize( swivel ) * VBO_VAR_BYTE_SIZE ;
				final int vertexBytes = stencilShape.getVertexSize() * vertexStrideBytes ;
				final int indexBytes  = ( stencilShape.getIndexSize() + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;

				final GLGeometry geometry = new GLGeometry( GL3.GL_TRIANGLES, indexBytes, vertexBytes, vertexStrideBytes )
				{
					@Override
					protected Location findLocation( final GLDrawData _data )
					{
						final Shape shape = _data.getClipShape() ;
						return findLocation( shape.getIndexSize(), shape.getVertexSize() ) ;
					}
				} ;

				stencilLocation = geometry.findLocation( _data ) ;
				stencilProgram = _data.getClipProgram() ;
				stencilMatrix = _data.getClipMatrix() ;
			}
		}

		/**
			Blank out geometry stored by this buffer.
			GLGeometry objects initialised still exist
			but are reset.
		*/
		private void clear()
		{
			for( final GLGeometry geometry : buffers )
			{
				geometry.clear() ;
			}
		}

		public void clean()
		{
			final Iterator<GLGeometry> i = buffers.iterator() ;
			while( i.hasNext() == true )
			{
				final GLGeometry geometry = i.next() ;
				if( geometry.containsGeometry() == false )
				{
					geometry.destroy() ;
					i.remove() ;
				}
			}
		}

		@Override
		public int sortValue()
		{
			return layer ;
		}

		/**
			Clear allocations and unbind the GLGeometry 
			objects in OpenGL.
			Nothing should be left, purge it all.
		*/
		public void destroy()
		{
			shapeSwivel       = null ;
			shapeStyle        = null ;
			attributes        = null ;
			program           = null ;
			stencilProgram    = null ;
			stencilShape      = null ;
			stencilAttributes = null ;
			stencilMatrix     = null ;
			stencilLocation   = null ;

			for( final GLGeometry geometry : buffers )
			{
				geometry.clear() ;
				geometry.destroy() ;
			}
			buffers.clear() ;
		}
	}

	public class GLGeometryBuffer extends GLBuffer
	{
		public GLGeometryBuffer( final GLDrawData _data,
								 final int _indexLengthBytes,
								 final int _vertexLengthBytes )
		{
			super( _data, _indexLengthBytes, _vertexLengthBytes ) ;
		}

		@Override
		public void upload( final GL3 _gl, final GLDrawData _data )
		{
			final Location location = findLocationGeometry( _data ) ;
			uploadGeometry( _gl, location, _data ) ;
		}

		private void uploadGeometry( final GL3 _gl, final Location _location, final GLDrawData _data )
		{
			final GLGeometry geometry = _location.getGeometry() ;
			final Shape shape = _data.getDrawShape() ;

			_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;	//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			GLGeometryUploader.this.uploadIndex( _gl, _location, shape ) ;
			GLGeometryUploader.this.uploadVBO( _gl, _location, shape, _data.getDrawMatrix() ) ;
		}

		private Location findLocationGeometry( final GLDrawData _data )
		{
			// If _data has already been added we return the location 
			// in which it resides.
			{
				final Location location = _data.getLocation() ;
				if( location != null )
				{
					return location ;
				}
			}

			// If it hasn't been added find a space for it within 
			// an existing geometry buffer.
			for( final GLGeometry geometry : buffers )
			{
				final Location location = geometry.findLocation( _data ) ;
				if( location != null )
				{
					location.setData( _data ) ;
					_data.setLocation( location ) ;
					return location ;
				}
			}

			// If no space exists create a new geometry buffer 
			// and repeat the finding process.
			// Increase the buffer size if the geometry is too large.
			final Shape shape = _data.getDrawShape() ;
			final int shapeIndexBytes = ( shape.getIndexSize() + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
			final int indexBytes = ( indexLengthBytes > shapeIndexBytes ) ? indexLengthBytes : shapeIndexBytes ;

			final int shapeVertexBytes = shape.getVertexSize() * vertexStrideBytes ;
			final int vertexBytes =  ( vertexLengthBytes > shapeVertexBytes ) ? vertexLengthBytes : shapeVertexBytes ;

			expand( indexBytes, vertexBytes ) ;
			return findLocationGeometry( _data ) ;
		}

		@Override
		protected void expand( final int _indexLengthBytes, final int _vertexLengthBytes )
		{
			buffers.add( new GLGeometry( style, _indexLengthBytes, _vertexLengthBytes, vertexStrideBytes )
			{
				@Override
				protected Location findLocation( final GLDrawData _data )
				{
					final Shape shape = _data.getDrawShape() ;
					return findLocation( shape.getIndexSize(), shape.getVertexSize() ) ;
				}
			} ) ;
		}
	}
	
	public class GLTextBuffer extends GLBuffer
	{
		public GLTextBuffer( final GLDrawData _data,
							 final int _indexLengthBytes,
							 final int _vertexLengthBytes )
		{
			super( _data, _indexLengthBytes, _vertexLengthBytes ) ;
		}

		@Override
		public void upload( final GL3 _gl, final GLDrawData _data )
		{
			Location location = findLocationText( _gl, _data ) ;
			uploadText( _gl, location, _data ) ;
		}

		/**
			Determine whether or not the Location is large 
			enough to contain the text specified by the draw object.
			A developer can change the text on a whim, so the location 
			must be updated to take this into account.
		*/
		private boolean isCorrectSize( final GLDrawData _data, final Location _location )
		{
			final StringBuilder txt = _data.getText() ;
			final int shapeIndexBytes = ( ( txt.length() * 6 ) + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
			return shapeIndexBytes == _location.getIndexLength() ;
		}

		private void uploadText( final GL3 _gl, final Location _location, final GLDrawData _data )
		{
			final Matrix4 positionMatrix = _data.getDrawMatrix() ;

			final MalletColour colour = _data.getColour() ;
			final MalletFont font = _data.getFont() ;
			final GLFontMap fm = ( GLFontMap )font.font.getFont() ;

			final Shape shape = _data.getDrawShape() ;
			final Shape.Swivel[] swivel = shape.getSwivel() ;
			final int vertexSize = calculateVertexSize( swivel ) ;
			final int verticiesSize = shape.getVertexSize() ;

			final GLGeometry geometry = _location.getGeometry() ;
			_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, geometry.getIndexID() ) ;	//GLRenderer.handleError( "Upload Bind Index: ", _gl ) ;
			_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, geometry.getVBOID() ) ;				//GLRenderer.handleError( "Upload Bind Vertex: ", _gl ) ;

			final StringBuilder text = _data.getText() ;
			final int length = text.length() ;
			final int initialIndexOffset = _location.getVertexStart() / geometry.vertexStrideBytes ;

			int indexInc = 0 ;
			int vertexInc = 0 ;

			int indexStartBytes = _location.getIndexStart() ;
			int vertexStartBytes = _location.getVertexStart() ;

			for( int i = 0; i < length; i++ )
			{
				final GLGlyph glyph = fm.getGlyphWithChar( text.charAt( i ) ) ;
				final int indexOffset = initialIndexOffset + ( i * 4 ) ;

				final int size = glyph.shape.getIndexSize() ; 
				for( int j = 0; j < size; j++ )
				{
					indicies[indexInc++] = indexOffset + glyph.shape.getIndex( j ) ;
					if( indexInc >= indicies.length )
					{
						indexBuffer.put( indicies ) ;
						indexBuffer.position( 0 ) ;

						final int lengthBytes = indicies.length * IBO_VAR_BYTE_SIZE ;
						_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexStartBytes, lengthBytes, indexBuffer ) ;

						indexStartBytes += lengthBytes ;
						indexInc = 0 ;
					}
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
								glyph.shape.getVector3( j, k, point ) ;
								Matrix4.multiply( point, positionMatrix, temp ) ;
								verticies[vertexInc++] = temp.x ;
								verticies[vertexInc++] = temp.y ;
								verticies[vertexInc++] = temp.z ;
								break ;
							}
							case COLOUR :
							{
								// GLDrawData colour overrides Shapes colour.
								final MalletColour c = ( colour != null ) ? colour : glyph.shape.getColour( j, k, shapeColour ) ;
								verticies[vertexInc++] = getABGR( c ) ;
								break ;
							}
							case UV     :
							{
								glyph.shape.getVector2( j, k, uv ) ;
								verticies[vertexInc++] = uv.x ;
								verticies[vertexInc++] = uv.y ;
								break ;
							}
						}
					}

					if( ( vertexInc + vertexSize ) >= verticies.length )
					{
						vertexBuffer.put( verticies ) ;
						vertexBuffer.position( 0 ) ;

						final int lengthBytes = vertexInc * VBO_VAR_BYTE_SIZE ;
						_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, vertexStartBytes, lengthBytes, vertexBuffer ) ;

						vertexStartBytes += lengthBytes ;
						vertexInc = 0 ;
					}
				}

				positionMatrix.translate( glyph.advance, 0.0f, 0.0f ) ;
			}

			indicies[indexInc++] = PRIMITIVE_RESTART_INDEX ;

			indexBuffer.put( indicies ) ;
			indexBuffer.position( 0 ) ;

			_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexStartBytes, indexInc * IBO_VAR_BYTE_SIZE, indexBuffer ) ;

			if( vertexInc > 0 )
			{
				vertexBuffer.put( verticies ) ;
				vertexBuffer.position( 0 ) ;

				_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, vertexStartBytes, vertexInc * VBO_VAR_BYTE_SIZE, vertexBuffer ) ;
			}
		}

		private Location findLocationText( final GL3 _gl, final GLDrawData _data )
		{
			// If _data has already been added we return the location 
			// in which it resides.
			{
				final Location location = _data.getLocation() ;
				if( location != null )
				{
					// Ensure the location previously assigned is still good
					// for our purposes.
					if( isCorrectSize( _data, location ) == true )
					{
						return location ;
					}

					remove( _gl, _data ) ;
				}
			}

			// If it hasn't been added find a space for it within 
			// an existing geometry buffer.
			for( final GLGeometry geometry : buffers )
			{
				final Location location = geometry.findLocation( _data ) ;
				if( location != null )
				{
					location.setData( _data ) ;
					_data.setLocation( location ) ;
					return location ;
				}
			}

			// If no space exists create a new geometry buffer 
			// and repeat the finding process.
			// Increase the buffer size if the geometry is too large.
			final Shape shape = _data.getDrawShape() ;
			final StringBuilder text = _data.getText() ;
			final int shapeIndexBytes = ( ( shape.getIndexSize() + PRIMITIVE_EXPANSION ) * text.length() ) * IBO_VAR_BYTE_SIZE ;
			final int indexBytes = ( indexLengthBytes > shapeIndexBytes ) ? indexLengthBytes : shapeIndexBytes ;

			final int shapeVertexBytes = shape.getVertexSize() * vertexStrideBytes  * text.length() ;
			final int vertexBytes =  ( vertexLengthBytes > shapeVertexBytes ) ? vertexLengthBytes : shapeVertexBytes ;

			expand( indexBytes, vertexBytes ) ;
			return findLocationText( _gl, _data ) ;
		}

		@Override
		protected void expand( final int _indexLengthBytes, final int _vertexLengthBytes )
		{
			buffers.add( new GLGeometry( style, _indexLengthBytes, _vertexLengthBytes, vertexStrideBytes )
			{
				@Override
				protected Location findLocation( final GLDrawData _data )
				{
					final StringBuilder text = _data.getText() ;
					return findLocation( ( 6 * text.length() ), ( 4 * text.length() ) ) ;
				}
			} ) ;
		}
	}

	public abstract class GLGeometry
	{
		private final int style ;
		private final int indexLengthBytes ;
		private final int vertexLengthBytes ;
		private final int vertexStrideBytes ;		// Specifies the byte offset between verticies

		public final int[] indexID ;
		public final int[] vboID ;

		public int amountIndexUsedBytes  = 0 ;		// How much of buffer has been used
		public int amountVertexUsedBytes = 0 ;		// How much of buffer has been used

		private final List<Location> allocated = MalletList.<Location>newList()  ;
		private final List<Location.Range> indexRanges = MalletList.<Location.Range>newList()  ;
		private final List<Location.Range> vertexRanges = MalletList.<Location.Range>newList()  ;

		public GLGeometry( final int _style,
						   final int _indexLengthBytes,
						   final int _vertexLengthBytes,
						   final int _vertexStrideBytes )
		{
			style = _style ;
			indexLengthBytes  = _indexLengthBytes ;
			vertexLengthBytes = _vertexLengthBytes ;
			vertexStrideBytes = _vertexStrideBytes ;

			final GL3 gl = GLRenderer.getGL() ;
			indexID = GLModelManager.genIndexID( gl ) ;
			vboID = GLModelManager.genVBOID( gl ) ;

			gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
			gl.glBufferData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexLengthBytes, null, GL3.GL_DYNAMIC_DRAW ) ;

			gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vboID[0] ) ;
			gl.glBufferData( GL3.GL_ARRAY_BUFFER, vertexLengthBytes, null, GL3.GL_DYNAMIC_DRAW ) ;
		}

		/**
			Using the data stored in _data calculate index and 
			vertex size that needs to be found.
			Call findLocation( int _indexSize, int _vertexSize )
			once you've calculated your space requirements.
		*/
		protected abstract Location findLocation( final GLDrawData _data ) ;

		protected Location findLocation( final int _indexSize, final int _vertexSize )
		{
			final int availableIndex = indexLengthBytes - amountIndexUsedBytes ;
			final int shapeIndexBytes = ( _indexSize + PRIMITIVE_EXPANSION ) * IBO_VAR_BYTE_SIZE ;
			if( shapeIndexBytes > availableIndex )
			{
				//System.out.println( "Not enough Index space..." ) ;
				return null ;
			}

			final int availableVertex = vertexLengthBytes - amountVertexUsedBytes ;
			final int shapeVertexBytes = _vertexSize * vertexStrideBytes ;
			if( shapeVertexBytes > availableVertex )
			{
				//System.out.println( "Not enough Vertex space..." ) ;
				return null ;
			}

			if( allocated.isEmpty() )
			{
				//System.out.println( "Index Length: " + shapeIndexBytes + " Vertex Length: " + shapeVertexBytes ) ;
				return createLocation( 0, shapeIndexBytes, 0, shapeVertexBytes ) ;
			}

			boolean foundIndex = false ;
			boolean foundVertex = false ;
			int locationIndexStart = 0 ;
			int locationVertexStart = 0 ;

			//System.out.println( "Searching for location.. " ) ;
			for( final Location.Range nextIndex : indexRanges )
			{
				final int indexLen = nextIndex.getStart() - locationIndexStart ;
				if( indexLen < shapeIndexBytes )
				{
					locationIndexStart = nextIndex.getEnd() ;
				}
				else
				{
					foundIndex = true ;
					break ;
				}
			}

			for( final Location.Range nextVertex : vertexRanges )
			{
				final int vertexLen = nextVertex.getStart() - locationVertexStart ;
				if( vertexLen < shapeVertexBytes )
				{
					locationVertexStart = nextVertex.getEnd() ;
				}
				else
				{
					foundVertex = true ;
					break ;
				}
			}

			if( foundIndex == true && foundVertex == true )
			{
				//System.out.println( "Found location... " ) ;
				return createLocation( locationIndexStart, shapeIndexBytes, locationVertexStart, shapeVertexBytes ) ;
			}

			final int indexLen = indexLengthBytes - locationIndexStart ;
			final int vertexLen = vertexLengthBytes - locationVertexStart ;
			if( indexLen > shapeIndexBytes && vertexLen > shapeVertexBytes )
			{
				//System.out.println( "Found location at end... " ) ;
				return createLocation( locationIndexStart, shapeIndexBytes, locationVertexStart, shapeVertexBytes ) ;
			}

			return null ;		// If no space is available
		}

		private Location createLocation( final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			amountIndexUsedBytes += _indexLength ;
			amountVertexUsedBytes += _vertexLength ;

			final Location location = locationCache.get() ;
			location.set( this, _indexStart, _indexLength, _vertexStart, _vertexLength ) ;
			allocated.add( location ) ;

			updateRanges() ;
			return location ; 
		}

		public void remove( final GL3 _gl, final Location _location )
		{
			if( allocated.remove( _location ) == true )
			{
				amountIndexUsedBytes -= _location.getIndexLength() ;
				amountVertexUsedBytes -= _location.getVertexLength() ;
				locationCache.reclaim( _location ) ;

				updateRanges() ;
				packGeometryData( _gl ) ;
			}
		}

		public int getIndexID()
		{
			return indexID[0] ;
		}

		public int getVBOID()
		{
			return vboID[0] ;
		}

		public int getIndexLength()
		{
			return amountIndexUsedBytes / IBO_VAR_BYTE_SIZE ;
		}

		public int getUsedIndexBytes()
		{
			return amountIndexUsedBytes ;
		}

		public int getUsedVertexBytes()
		{
			return amountVertexUsedBytes ;
		}

		public int getStyle()
		{
			return style ;
		}

		public int getStride()
		{
			return vertexStrideBytes ;
		}

		public int getLocationSize()
		{
			return allocated.size() ;
		}
		
		/**
			Returns true if the GLGeometry contains any 
			vertex data, false otherwise.
			Can be used to determine whether the buffer can 
			be destroyed.
		*/
		public boolean containsGeometry()
		{
			return amountVertexUsedBytes > 0 ;
		}

		public void destroy()
		{
			GLModelManager.unbind( this ) ;
		}

		/**
			Update the index and vertex range arrays.
			Ranges must be in order so findLocation 
			can find a correct free space.
		*/
		private void updateRanges()
		{
			indexRanges.clear() ;
			orderedIndexes( allocated, indexRanges ) ;

			vertexRanges.clear() ;
			orderedVertexes( allocated, vertexRanges ) ;
		}

		private List<Location.Range> orderedIndexes( final List<Location> _locations, final List<Location.Range> _ordered )
		{
			_ordered.clear() ;
			if( _locations.isEmpty() == true )
			{
				return _ordered ;
			}

			_ordered.add( _locations.get( 0 ).index ) ;
			final int size = _locations.size() ;

			for( int i = 1; i < size; i++ )
			{
				OrderedInsert.insert( _locations.get( i ).index, _ordered ) ;
			}

			return _ordered ;
		}

		private List<Location.Range> orderedVertexes( final List<Location> _locations, final List<Location.Range> _ordered )
		{
			_ordered.clear() ;
			if( _locations.isEmpty() == true )
			{
				return _ordered ;
			}

			_ordered.add( _locations.get( 0 ).vertex ) ;
			final int size = _locations.size() ;

			for( int i = 1; i < size; i++ )
			{
				OrderedInsert.insert( _locations.get( i ).vertex, _ordered ) ;
			}

			return _ordered ;
		}

		private void packGeometryData( final GL3 _gl )
		{
			int start = 0 ;
			for( final Location.Range range : indexRanges )
			{
				final int indexLength = range.getLength() ;
				if( start != range.getStart() )
				{
					final Location location = range.getParent() ;
					location.set( this, start, indexLength, location.getVertexStart(), location.getVertexLength() ) ;

					// The data being shifted may be is a state of flux, 
					// there is no guarantee that it will still 
					// reside within this buffer.
					// We'll flag the data to be updated during its
					// render cycle.
					DrawAssist.forceUpdate( location.getData() ) ;
				}

				start += indexLength ;
			}
		}

		private void clear()
		{
			amountIndexUsedBytes = 0 ;
			amountVertexUsedBytes = 0 ;
			for( final Location location : allocated )
			{
				locationCache.reclaim( location ) ;
			}
			allocated.clear() ;
		}
	}

	public static class Location implements Cacheable
	{
		private GLDrawData data ;
		private GLGeometry geometry = null ;
		
		private Range index = new Range() ;
		private Range vertex = new Range() ;

		public Location() {}

		private Location( final GLGeometry _geometry, final int _indexStart, final int _indexLength, final int _vertexStart, final int _vertexLength )
		{
			set( _geometry, _indexStart, _indexLength, _vertexStart, _vertexLength ) ;
		}

		public void set( final GLGeometry _geometry,
						 final int _indexStart,
						 final int _indexLength,
						 final int _vertexStart,
						 final int _vertexLength )
		{
			geometry = _geometry ;
			index.set( _indexStart, _indexLength ) ;
			vertex.set( _vertexStart, _vertexLength ) ;
		}

		public void setData( final GLDrawData _data )
		{
			data = _data ;
		}

		public int getIndexStart()
		{
			return index.getStart() ;
		}

		public int getIndexEnd()
		{
			return index.getEnd() ;
		}

		public int getIndexLength()
		{
			return index.getLength() ;
		}

		public int getVertexStart()
		{
			return vertex.getStart() ;
		}

		public int getVertexEnd()
		{
			return vertex.getEnd() ;
		}

		public int getVertexLength()
		{
			return vertex.getLength() ;
		}

		@Override
		public void reset()
		{
			set( null, 0, 0, 0, 0 ) ;
			setData( null ) ;
		}

		public GLDrawData getData()
		{
			return data ;
		}

		public GLGeometry getGeometry()
		{
			return geometry ;
		}

		public String toString()
		{
			return "Index: " + index.toString() + " Vertex: " + vertex.toString() ;
		}

		private class Range implements SortInterface
		{
			private int start = 0 ;
			private int length = 0 ;

			public Range() {}

			public void set( final int _start, final int _length )
			{
				start = _start ;
				length = _length ;
			}

			public int sortValue()
			{
				return start ;
			}

			public Location getParent()
			{
				return Location.this ;
			}

			public int getStart()
			{
				return start ;
			}

			public int getEnd()
			{
				return start + length ;
			}

			public int getLength()
			{
				return length ;
			}

			public String toString()
			{
				return "Start: " + start + " Length: " + length ;
			}
		}
	}

	protected static void enableVertexAttributes( final GL3 _gl, final VertexAttrib[] _atts )
	{
		for( final VertexAttrib att : _atts )
		{
			_gl.glEnableVertexAttribArray( att.index ) ;
		}
	}

	protected static void prepareVertexAttributes( final GL3 _gl, final VertexAttrib[] _atts, final int _stride )
	{
		for( final VertexAttrib att : _atts )
		{
			_gl.glVertexAttribPointer( att.index, att.size, att.type, att.normalised, _stride, att.offset ) ;
		}
	}

	protected static void disableVertexAttributes( final GL3 _gl, final VertexAttrib[] _atts )
	{
		for( final VertexAttrib att : _atts )
		{
			_gl.glDisableVertexAttribArray( att.index ) ;
		}
	}

	private float getABGR( final MalletColour _colour )
	{
		abgrTemp[0] = _colour.colours[MalletColour.ALPHA] ;
		abgrTemp[1] = _colour.colours[MalletColour.BLUE] ;
		abgrTemp[2] = _colour.colours[MalletColour.GREEN] ;
		abgrTemp[3] = _colour.colours[MalletColour.RED] ;

		return ConvertBytes.toFloat( abgrTemp, 0, 4 ) ;
	}
}
