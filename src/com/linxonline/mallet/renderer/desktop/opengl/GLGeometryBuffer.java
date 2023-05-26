package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.Arrays ;
import java.util.List ;
import java.util.Map ;
import java.nio.* ;

import com.linxonline.mallet.util.buffers.IntegerBuffer ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.IShape ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.DrawBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.IOcclude ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.IntVector2 ;

public final class GLGeometryBuffer extends GLBuffer
{
	private static final IUniform[] EMPTY_UNIFORMS = new IUniform[0] ;

	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private int indexMapSize = 0 ;
	private IndexMap[] indexMaps ;
	private IntBuffer indexBuffer ;
	private FloatBuffer vertexBuffer ;

	private int[] indexID = new int[1] ;
	private int[] vboID = new int[1] ;
	private int[] indexLength = new int[1] ;

	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private final Matrix4 matrix = new Matrix4() ;
	private final Matrix4 matrixTemp = Matrix4.createTempIdentity() ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 rotation = new Vector3() ;
	private final Vector3 scale = new Vector3() ;

	private boolean stable = false ;

	public GLGeometryBuffer( final GeometryBuffer _buffer )
	{
		this( _buffer, GLUtils.MIN_INDEX_BYTE_SIZE,
					   GLUtils.MIN_VERTEX_BYTE_SIZE,
					   GLUtils.MAX_INDEX_BYTE_SIZE,
					   GLUtils.MAX_VERTEX_BYTE_SIZE ) ;
	}

	public GLGeometryBuffer( final GeometryBuffer _buffer,
							 final int _indexByteSize,
							 final int _vertexByteSize,
							 final int _maxIndexByteSize,
							 final int _maxVertexByteSize )
	{
		super( false ) ;

		indexByteSize = _indexByteSize ;
		vertexByteSize = _vertexByteSize ;

		maxIndexByteSize = _maxIndexByteSize ;
		maxVertexByteSize = _maxVertexByteSize ;

		indexMapSize = 0 ;
		indexMaps = new IndexMap[0] ;

		// We'll construct our buffers for the minimum size
		// but if we need more space we'll expand the size 
		// until we reach our maximum capacity.
		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( indexByteSize ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( vertexByteSize ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;

		MGL.glGenBuffers( 1, indexID, 0 ) ;
		MGL.glGenBuffers( 1, vboID, 0 ) ;
	}

	public boolean update( final GeometryBuffer _buffer )
	{
		if( vertexStride <= 0 )
		{
			// GeometryBuffer swivel is not expected to change once it is 
			// set, so we'll only calculate the swivel once.
			vertexStride = calculateVertexSize( _buffer.getAttribute() ) ;
			vertexStrideBytes = vertexStride * VBO_VAR_BYTE_SIZE ;
		}

		switch( _buffer.getStyle() )
		{
			case LINES      : style = MGL.GL_LINES ;      break ;
			case LINE_STRIP : style = MGL.GL_LINE_STRIP ; break ;
			case FILL       : style = MGL.GL_TRIANGLES ;  break ;
			default         : style = MGL.GL_LINES ;      break ;
		}

		int bufferIndex = 0 ;
		int usedIndexByteSize = 0 ;
		int usedVertexByteSize = 0 ;

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;

		final List<Draw> draws = _buffer.getDraws() ;
		final int size = draws.size() ;
		if( indexMaps.length < size )
		{
			final IndexMap[] oldMaps = indexMaps ;
			indexMaps = new IndexMap[size] ;
			for( int i = 0; i < size; ++i )
			{
				indexMaps[i] = ( i < oldMaps.length ) ? oldMaps[i] : new IndexMap() ;
			}
		}

		indexMapSize = 0 ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = draws.get( i ) ;
			final IShape[] shapes = draw.getShapes() ;
			for( final IShape shape : shapes )
			{
				if( shape == null )
				{
					continue ;
				}

				final int shapeIndexByteSize = shape.getIndicesSize() * IBO_VAR_BYTE_SIZE ;
				final int shapeVertexByteSize = shape.getVerticesSize() * vertexStrideBytes ;

				usedIndexByteSize += shapeIndexByteSize ;
				if(usedIndexByteSize > indexByteSize)
				{
					expandIndexBuffer() ;
				}

				usedVertexByteSize += shapeVertexByteSize ;
				if( usedVertexByteSize > vertexByteSize )
				{
					expandVertexBuffer() ;
				}

				if( usedIndexByteSize > indexByteSize || usedVertexByteSize > vertexByteSize )
				{
					upload( bufferIndex ) ;

					// If the draw object would exceed our limits
					// then either create/jump to our next buffer.
					bufferIndex = ( bufferIndex + 1 == indexID.length ) ? genNewBuffers() : bufferIndex + 1 ;
					usedIndexByteSize = shapeIndexByteSize ;
					usedVertexByteSize = shapeVertexByteSize ;
				}

				uploadIndexToRAM( draw, shape, indexMaps[indexMapSize++] ) ;
				vertexBuffer.put( shape.getRawVertices() ) ;
			}
		}

		upload( bufferIndex ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void draw( final VertexAttrib[] _attributes, final GLProgram _program, final IOcclude _occluder )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		GLGeometryBuffer.enableVertexAttributes( _attributes ) ;
		for( int i = 0; i < indexLength.length; ++i )
		{
			final int length = indexLength[i] ;
			if( length <= 0 )
			{
				// We may have indices and vertices but if they 
				// current have no data uploaded to them there 
				// is no point drawing them.
				continue ;
			}

			MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[i] ) ;
			MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[i] ) ;

			GLGeometryBuffer.prepareVertexAttributes( _attributes, vertexStrideBytes ) ;

			for( int j = 0; j < indexMapSize; ++j )
			{
				final IndexMap map = indexMaps[j] ;
				final Draw draw = map.draw ;
				if( draw.isHidden() || _occluder.occlude( draw ) )
				{
					continue ;
				}

				if( loadDrawUniforms( _program, draw ) == false )
				{
					System.out.println( "Failed to load uniforms for draw object." ) ;
					continue ;
				}

				draw.getPosition( position ) ;
				draw.getOffset( offset ) ;
				draw.getRotation( rotation ) ;
				draw.getScale( scale ) ;

				apply( matrix, matrixTemp, position, offset, rotation, scale ) ;

				MGL.glUniformMatrix4fv( _program.inModelMatrix, 1, true, matrix.matrix, 0 ) ;
				MGL.glDrawElements( style, map.count, MGL.GL_UNSIGNED_INT, map.start * IBO_VAR_BYTE_SIZE ) ;
			}
		}
		GLGeometryBuffer.disableVertexAttributes( _attributes ) ;
	}

	@Override
	public void shutdown()
	{
		MGL.glDeleteBuffers( indexID.length, indexID, 0 ) ;
		MGL.glDeleteBuffers( vboID.length, vboID, 0 ) ;
	}

	private void expandIndexBuffer()
	{
		if( indexByteSize == maxIndexByteSize )
		{
			// We can't expand the buffer any more.
			return ;
		}
	
		final int doubleCapacity = indexByteSize * 2 ;
		indexByteSize = (doubleCapacity > maxIndexByteSize) ? maxIndexByteSize : doubleCapacity ;
	
		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( indexByteSize ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;

		final IntBuffer old = indexBuffer ;
		// We need to take the position of the original buffer
		// when we make a copy it will set the position of the 
		// new buffer to the size of the original, however our 
		// data may not have maxed out the full buffer.
		final int position = old.position() ;
		old.position( 0 ) ;

		indexBuffer = indexByteBuffer.asIntBuffer() ;
		indexBuffer.put( old ) ;
		indexBuffer.position( position ) ;
	}

	private void expandVertexBuffer()
	{
		if( vertexByteSize == maxVertexByteSize )
		{
			// We can't expand the buffer any more.
			return ;
		}
	
		final int doubleCapacity = vertexByteSize * 2 ;
		vertexByteSize = (doubleCapacity > maxVertexByteSize) ? maxVertexByteSize : doubleCapacity ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( vertexByteSize ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;

		final FloatBuffer old = vertexBuffer ;
		// We need to take the position of the original buffer
		// when we make a copy it will set the position of the 
		// new buffer to the size of the original, however our 
		// data may not have maxed out the full buffer.
		final int position = old.position() ;
		old.position( 0 ) ;

		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;
		vertexBuffer.put( old ) ;
		vertexBuffer.position( position ) ;
	}

	private int genNewBuffers()
	{
		final int offset = indexID.length ;

		indexID = IntegerBuffer.expand( indexID, 1 ) ;
		vboID = IntegerBuffer.expand( vboID, 1 ) ;
		indexLength = IntegerBuffer.expand( indexLength, 1 ) ;

		MGL.glGenBuffers( 1, indexID, offset ) ;
		MGL.glGenBuffers( 1, vboID, offset ) ;

		return offset ;
	}

	/**
		Upload the data to the specified vbo and ibo
		specified by the _index location.
	*/
	private void upload( final int _index )
	{
		final int length = indexBuffer.position() ;
		indexLength[_index] = length ;
		if( length <= 0 )
		{
			return ;
		}

		final int indiciesLengthBytes = length * IBO_VAR_BYTE_SIZE ;
		final int verticiesLengthBytes = vertexBuffer.position() * VBO_VAR_BYTE_SIZE ;

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[_index] ) ;
		MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[_index] ) ;

		MGL.glBufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, indiciesLengthBytes, indexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
		MGL.glBufferData( MGL.GL_ARRAY_BUFFER, verticiesLengthBytes, vertexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
	}
	
	private void uploadIndexToRAM( final Draw _draw, final IShape _shape, final IndexMap _map )
	{
		final int indexStart = indexBuffer.position() ;
		final int indexOffset = vertexBuffer.position() / vertexStride ;

		final int[] inds = _shape.getRawIndices() ;
		final int size = inds.length ;
		for( int i = 0; i < size; i++ )
		{
			indexBuffer.put( indexOffset + inds[i] ) ;
		}

		_map.set( indexStart, size, _draw ) ;
	}

	private static class IndexMap
	{
		public int start = 0 ;
		public int count = 0 ;
		public Draw draw ;

		public void set( final int _start, final int _count, final Draw _draw )
		{
			start = _start ;
			count = _count ;
			draw = _draw ;
		}
	}
}

