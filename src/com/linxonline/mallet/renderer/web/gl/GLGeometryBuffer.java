package com.linxonline.mallet.renderer.web.gl ;

import java.util.Arrays ;
import java.util.List ;

import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;

import com.linxonline.mallet.util.buffers.IntegerBuffer ;

import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.IShape ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.DrawBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.IOcclude ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.IntVector2 ;

public final class GLGeometryBuffer extends GLBuffer
{
	private static final IUniform[] EMPTY_UNIFORMS = new IUniform[0] ;
	private static final int DRAW_DIMENSIONS_PACKET_SIZE = 4 ;

	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private int toDrawSize = 0 ;
	private Draw[] toDraw ;
	private int[] drawDimensions ;

	private Int16Array indexBuffer ;
	private Float32Array vertexBuffer ;

	private WebGLBuffer[] indexID = new WebGLBuffer[1] ;
	private WebGLBuffer[] vboID = new WebGLBuffer[1] ;
	private int[] indexLength = new int[1] ;

	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private int incrementIndex = 0 ;
	private int incrementVertex = 0 ;

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

		toDrawSize = 0 ;
		toDraw = new Draw[0] ;
		drawDimensions = new int[0] ;

		// We'll construct our buffers for the minimum size
		// but if we need more space we'll expand the size 
		// until we reach our maximum capacity.
		indexBuffer = Int16Array.create( indexByteSize / IBO_VAR_BYTE_SIZE ) ;
		vertexBuffer = Float32Array.create( vertexByteSize / VBO_VAR_BYTE_SIZE ) ;

		indexID[0] = MGL.createBuffer() ;
		vboID[0] = MGL.createBuffer() ;
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

		incrementIndex = 0 ;
		incrementVertex = 0 ;

		final List<Draw> draws = _buffer.getDraws() ;
		final int size = draws.size() ;
		if( toDraw.length < size )
		{
			toDraw = new Draw[size] ;
			drawDimensions = new int[size * DRAW_DIMENSIONS_PACKET_SIZE] ;
		}

		toDrawSize = 0 ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = draws.get( i ) ;
			toDraw[i] = draw ;

			final IShape[] shapes = draw.getShapes() ;
			if( shapes == null )
			{
				continue ;
			}

			final int shapeSize = shapes.length ;
			for( int j = 0; j < shapeSize; ++j )
			{
				final IShape shape = shapes[j] ;
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

				final int drawIndex = i ;
				final int offset = toDrawSize++ * DRAW_DIMENSIONS_PACKET_SIZE ;
				uploadIndexToRAM( bufferIndex, drawIndex, shape, offset ) ;

				final float[] vertices = shape.getRawVertices() ;
				for( int k = 0; k < vertices.length; ++k )
				{
					vertexBuffer.set( incrementVertex++, vertices[k] ) ;
				}
			}
		}

		upload( bufferIndex ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void draw( final VertexAttrib[] _attributes, final GLProgram _program, final Camera _camera, final IOcclude _occluder )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		GLGeometryBuffer.enableVertexAttributes( _attributes ) ;

		int activeIndex = -1 ;
		for( int i = 0; i < toDrawSize; ++i )
		{
			final int drawOffset = i * DRAW_DIMENSIONS_PACKET_SIZE ;
			final int bufferIndex = drawDimensions[drawOffset] ;
			final int drawIndex = drawDimensions[drawOffset + 1] ;
			final int start = drawDimensions[drawOffset + 2] ;
			final int count = drawDimensions[drawOffset + 3] ;

			final Draw draw = toDraw[drawIndex] ;
			if( draw.isHidden() || _occluder.occlude( _camera, draw ) )
			{
				continue ;
			}

			if( activeIndex != bufferIndex )
			{
				activeIndex = bufferIndex ;
				MGL.bindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[activeIndex] ) ;
				MGL.bindBuffer( MGL.GL_ARRAY_BUFFER, vboID[activeIndex] ) ;
				GLGeometryBuffer.prepareVertexAttributes( _attributes, vertexStrideBytes ) ;
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

			MGL.uniformMatrix4fv( _program.inModelMatrix, true, matrix.matrix ) ;
			MGL.drawElements( style, count, MGL.GL_UNSIGNED_SHORT, start * IBO_VAR_BYTE_SIZE ) ;
		}
		GLGeometryBuffer.disableVertexAttributes( _attributes ) ;
	}

	@Override
	public void shutdown()
	{
		MGL.deleteBuffer( indexID[0] ) ;
		MGL.deleteBuffer( vboID[0] ) ;
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

		final Int16Array newIndexBuffer = Int16Array.create( indexByteSize / IBO_VAR_BYTE_SIZE ) ;
		for( int i = 0; i < incrementIndex; ++i )
		{
			newIndexBuffer.set( i, indexBuffer.get( i ) ) ;
		}

		indexBuffer = newIndexBuffer ;
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

		final Float32Array newVertexBuffer = Float32Array.create( vertexByteSize / VBO_VAR_BYTE_SIZE ) ;
		for( int i = 0; i < incrementVertex; ++i )
		{
			newVertexBuffer.set( i, vertexBuffer.get( i ) ) ;
		}

		vertexBuffer = newVertexBuffer ;
	}

	private int genNewBuffers()
	{
		final int offset = indexID.length ;

		indexID = expand( indexID, 1 ) ;
		vboID = expand( vboID, 1 ) ;
		indexLength = IntegerBuffer.expand( indexLength, 1 ) ;

		indexID[offset] = MGL.createBuffer() ;
		vboID[offset] = MGL.createBuffer() ;

		return offset ;
	}

	/**
		Upload the data to the specified vbo and ibo
		specified by the _index location.
	*/
	private void upload( final int _index )
	{
		final int length = incrementIndex ;
		indexLength[_index] = length ;
		if( length <= 0 )
		{
			return ;
		}

		incrementIndex = 0 ;
		incrementVertex = 0 ;

		MGL.bindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[_index] ) ;
		MGL.bindBuffer( MGL.GL_ARRAY_BUFFER, vboID[_index] ) ;

		MGL.bufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
		MGL.bufferData( MGL.GL_ARRAY_BUFFER, vertexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
	}
	
	private void uploadIndexToRAM( final int _bufferIndex, final int _drawIndex, final IShape _shape, final int _dimOffset )
	{
		final int indexStart = incrementIndex ;
		final int indexOffset = incrementVertex / vertexStride ;

		final int[] inds = _shape.getRawIndices() ;
		final int size = inds.length ;
		for( int i = 0; i < size; i++ )
		{
			indexBuffer.set( incrementIndex++, ( short )( indexOffset + inds[i] ) ) ;
		}

		drawDimensions[_dimOffset] = _bufferIndex ;
		drawDimensions[_dimOffset + 1] = _drawIndex ;
		drawDimensions[_dimOffset + 2] = indexStart ;
		drawDimensions[_dimOffset + 3] = size ;
	}

	public static WebGLBuffer[] expand( final WebGLBuffer[] _from, final int _extra )
	{
		final int length = _from.length + _extra ;
		final WebGLBuffer[] to = new WebGLBuffer[length] ;
		System.arraycopy( _from, 0, to, 0, _from.length ) ;
		return to ;
	}
}

