package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.nio.* ;

import com.linxonline.mallet.util.buffers.IntegerBuffer ;

import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.IShape ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.IOcclude ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLGeometryBuffer extends GLBuffer
{
	private static final IUniform[] EMPTY_UNIFORMS = new IUniform[0] ;
	private static final int DRAW_DIMENSIONS_PACKET_SIZE = 3 ;
	private static final int SHAPE_DIMENSIONS_PACKET_SIZE = 3 ;
	private static final int RANGE_DIMENSIONS_PACKET_SIZE = 3 ;

	private static final int MAX_INT32_SIZE = 2147483647 ;
	private static final int MAX_INDEX_BUFFER_SIZE = MAX_INT32_SIZE / 4 ;

	private int indexByteSize ;

	private int toDrawSize = 0 ;
	private Draw[] toDraw ;
	private int[] drawDimensions ;
	private int[] shapeDimensions ;

	private IntBuffer indexBuffer ;
	private FloatBuffer vertexBuffer ;

	private int[] indexID = new int[1] ;
	private int[] vboID = new int[1] ;

	private final int vertexStride ;
	private final int vertexStrideBytes ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 rotation = new Vector3() ;
	private final Vector3 scale = new Vector3() ;
	private final Matrix4 modelMatrix = new Matrix4() ;

	private boolean stable = false ;

	public GLGeometryBuffer( final GeometryBuffer _buffer )
	{
		super( false ) ;

		vertexStride = calculateVertexSize( _buffer.getAttribute() ) ;
		vertexStrideBytes = vertexStride * VBO_VAR_BYTE_SIZE ;

		final int indexSize = calculateIndexSize( _buffer.getDraws() ) ;
		final int vertexSize = indexSize * vertexStride ;

		indexByteSize = indexSize * IBO_VAR_BYTE_SIZE ;

		toDrawSize = 0 ;
		toDraw = new Draw[0] ;
		drawDimensions = new int[0] ;
		shapeDimensions = new int[0] ;

		// We'll construct our buffers to meet the
		// requirements of the current draw objects.
		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( vertexSize * VBO_VAR_BYTE_SIZE ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( indexByteSize ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;

		MGL.glGenBuffers( 1, indexID, 0 ) ;
		MGL.glGenBuffers( 1, vboID, 0 ) ;
	}

	private int calculateIndexSize( final List<Draw> _draws )
	{
		int indexSize = 0 ;
	
		final int size = _draws.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = _draws.get( i ) ;
			final IShape[] shapes = draw.getShapes() ;
			if( shapes == null )
			{
				continue ;
			}

			for( int j = 0; j < shapes.length; ++j )
			{
				final IShape shape = shapes[j] ;
				if( shape == null )
				{
					continue ;
				}

				indexSize += shape.getIndicesSize() ;
				if( indexSize >= MAX_INDEX_BUFFER_SIZE )
				{
					return MAX_INDEX_BUFFER_SIZE ;
				}
			}
		}

		return indexSize ;
	}

	private int calculateShapeSize( final List<Draw> _draws )
	{
		int shapeSize = 0 ;
	
		final int size = _draws.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = _draws.get( i ) ;
			final IShape[] shapes = draw.getShapes() ;
			if( shapes == null )
			{
				continue ;
			}

			shapeSize += shapes.length ;
		}

		return shapeSize ;
	}
	
	public boolean update( final GeometryBuffer _buffer )
	{
		int bufferIndex = 0 ;
		int usedIndexByteSize = 0 ;
		int shapeStart = 0 ;

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;

		final List<Draw> draws = _buffer.getDraws() ;
		final int totalDrawSize = draws.size() ;
		if( toDraw.length < totalDrawSize )
		{
			toDraw = new Draw[totalDrawSize] ;
			drawDimensions = new int[totalDrawSize * DRAW_DIMENSIONS_PACKET_SIZE] ;
		}

		final int totalShapeSize = calculateShapeSize( draws ) * SHAPE_DIMENSIONS_PACKET_SIZE ;
		if( totalShapeSize != shapeDimensions.length )
		{
			shapeDimensions = new int[totalShapeSize] ;
		}

		toDrawSize = 0 ;
		for( int i = 0; i < totalDrawSize; ++i )
		{
			final Draw draw = draws.get( i ) ;
			toDraw[i] = draw ;

			final IShape[] shapes = draw.getShapes() ;
			if( shapes == null )
			{
				continue ;
			}

			final int drawIndex = i ;
			final int drawOffset = toDrawSize++ * DRAW_DIMENSIONS_PACKET_SIZE ;
			int shapeCount = 0 ;

			drawDimensions[drawOffset] = drawIndex ;
			drawDimensions[drawOffset + 1] = shapeStart * SHAPE_DIMENSIONS_PACKET_SIZE ;

			for( int j = 0; j < shapes.length; ++j )
			{
				final IShape shape = shapes[j] ;
				if( shape == null )
				{
					continue ;
				}

				shapeCount += 1 ;

				final int[] rawIndices = shape.getRawIndices() ;
				final float[] rawVerts = shape.getRawVertices() ;

				final int count = shape.getIndicesSize() ;
				final int shapeIndexByteSize = count * IBO_VAR_BYTE_SIZE ;

				usedIndexByteSize += shapeIndexByteSize ;
				if( usedIndexByteSize > indexByteSize )
				{
					expandBuffers( usedIndexByteSize ) ;
				}

				if( usedIndexByteSize > indexByteSize )
				{
					// Even once expanded we still can't fit it in...
					// We can only upload the current buffers
					// and create a new set.
					upload( bufferIndex ) ;

					// If the draw object would exceed our limits
					// then either create/jump to our next buffer.
					bufferIndex = ( bufferIndex + 1 == indexID.length ) ? genNewBuffers() : bufferIndex + 1 ;
					usedIndexByteSize = shapeIndexByteSize ;
				}

				final int shapeOffset = shapeStart++ * SHAPE_DIMENSIONS_PACKET_SIZE ;

				final int indexStart = indexBuffer.position() ;
				final int indexOffset = vertexBuffer.position() / vertexStride ;

				for( int k = 0; k < count; ++k )
				{
					indexBuffer.put( indexOffset + rawIndices[k] ) ;
				}

				shapeDimensions[shapeOffset] = bufferIndex ;
				shapeDimensions[shapeOffset + 1] = indexStart ;
				shapeDimensions[shapeOffset + 2] = count ;

				vertexBuffer.put( rawVerts ) ;
			}

			drawDimensions[drawOffset + 2] = shapeCount ;
		}

		upload( bufferIndex ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void draw( final int[] _range, final int _style, final VertexAttrib[] _attributes, final GLProgram _program, final GLProgram.UniformState _state, final Camera _camera, final IOcclude _occluder )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		GLGeometryBuffer.enableVertexAttributes( _attributes ) ;

		int activeIndex = -1 ;

		final int size = ( _range == null ) ? toDrawSize : _range.length ;
		for( int i = 0; i < size; ++i )
		{
			final int rangeIndex = i * RANGE_DIMENSIONS_PACKET_SIZE ;
			final int index = ( _range == null ) ? i : _range[rangeIndex] ;

			final int drawOffset = index * DRAW_DIMENSIONS_PACKET_SIZE ;

			final int drawIndex = drawDimensions[drawOffset] ;
			final int shapeStart = ( _range == null ) ? drawDimensions[drawOffset + 1] : _range[rangeIndex + 1] ;
			final int shapeCount = ( _range == null ) ? drawDimensions[drawOffset + 2] : _range[rangeIndex + 2] ;

			final Draw draw = toDraw[drawIndex] ;
			if( draw.isHidden() || _occluder.occlude( _camera, draw ) )
			{
				continue ;
			}

			if( _state.hasDrawUniforms() )
			{
				if( _program.loadDrawUniforms( _state, draw ) == false )
				{
					System.out.println( "Failed to load uniforms for draw object." ) ;
					continue ;
				}
			}

			draw.getPosition( position ) ;
			draw.getOffset( offset ) ;
			draw.getRotation( rotation ) ;
			draw.getScale( scale ) ;

			position.add( offset ) ;
			modelMatrix.applyTransformations( position, rotation, scale ) ;
			MGL.glUniformMatrix4fv( _program.inModelMatrix, 1, false, modelMatrix.matrix, 0 ) ;

			for( int j = 0; j < shapeCount; ++j )
			{
				final int shapeOffset = shapeStart + ( j * SHAPE_DIMENSIONS_PACKET_SIZE ) ;

				final int bufferIndex = shapeDimensions[shapeOffset] ;
				final int start = shapeDimensions[shapeOffset + 1] ;
				final int count = shapeDimensions[shapeOffset + 2] ;

				if( activeIndex != bufferIndex )
				{
					activeIndex = bufferIndex ;
					MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[activeIndex] ) ;
					MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[activeIndex] ) ;
					GLGeometryBuffer.prepareVertexAttributes( _attributes, vertexStrideBytes ) ;
				}

				MGL.glDrawElements( _style, count, MGL.GL_UNSIGNED_INT, start * IBO_VAR_BYTE_SIZE ) ;
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

	private void expandBuffers( final int _neededBytes )
	{
		final int maxIndexByteSize = MAX_INDEX_BUFFER_SIZE * IBO_VAR_BYTE_SIZE ;
		if( indexByteSize >= maxIndexByteSize )
		{
			// We can't expand the buffer no more.
			return ;
		}

		{
			final int doubleCapacity = _neededBytes * 2 ;
			indexByteSize = ( doubleCapacity > maxIndexByteSize ) ? maxIndexByteSize : doubleCapacity ;

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

		{
			// The vertex-buffer only needs to be big enough to reference
			// the same number of indices, if all the indices reference a
			// unique vertex.
			final int vertexByteSize = indexByteSize * vertexStrideBytes ;

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
	}

	private int genNewBuffers()
	{
		final int offset = indexID.length ;

		indexID = IntegerBuffer.expand( indexID, 1 ) ;
		vboID = IntegerBuffer.expand( vboID, 1 ) ;

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

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;
	}
}

