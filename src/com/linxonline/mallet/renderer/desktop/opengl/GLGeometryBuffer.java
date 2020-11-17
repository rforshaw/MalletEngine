package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.Arrays ;
import java.nio.* ;

import com.linxonline.mallet.util.buffers.IntegerBuffer ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLGeometryBuffer extends GLBuffer
{
	private final static int INDEX_BYTE_SIZE = 5 * 1024 ;
	private final static int VERTEX_BYTE_SIZE = 5 * 1024 ;

	private final int indexByteSize ;
	private final int vertexByteSize ;

	private final IntBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	private int[] indexID = new int[1] ;
	private int[] vboID = new int[1] ;
	private int[] indexLength = new int[1] ;

	private int order ;
	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

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

	private boolean stable = false ;

	public GLGeometryBuffer( final GeometryBuffer _buffer )
	{
		this( _buffer, INDEX_BYTE_SIZE, VERTEX_BYTE_SIZE ) ;
	}

	public GLGeometryBuffer( final GeometryBuffer _buffer, final int _indexByteSize, final int _vertexByteSize )
	{
		super( _buffer.isUI() ) ;

		indexByteSize = _indexByteSize ;
		vertexByteSize = _vertexByteSize ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( _indexByteSize ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( _vertexByteSize ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;

		MGL.glGenBuffers( 1, indexID, 0 ) ;
		MGL.glGenBuffers( 1, vboID, 0 ) ;
	}

	public boolean update( final GeometryBuffer _buffer )
	{
		final int swivelSize = calculateVertexSize( _buffer.getSwivel() ) ;
		vertexStride = swivelSize ;
		vertexStrideBytes = swivelSize * VBO_VAR_BYTE_SIZE ;

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

		for( final Draw draw : _buffer.getDraws() )
		{
			if( draw.isHidden() == true )
			{
				continue ;
			}

			final Shape shape = draw.getShape() ;
			final int shapeIndexByteSize = shape.getIndexSize() * IBO_VAR_BYTE_SIZE ;
			final int shapeVertexByteSize = shape.getVertexSize() * vertexStrideBytes ;

			usedIndexByteSize += shapeIndexByteSize ;
			usedVertexByteSize  += shapeVertexByteSize ;
			if( usedIndexByteSize > indexByteSize || usedVertexByteSize > vertexByteSize )
			{
				upload( bufferIndex ) ;

				// If the draw object would exceed our limits
				// then either create/jump to our next buffer.
				bufferIndex = ( bufferIndex + 1 == indexID.length ) ? genNewBuffers() : bufferIndex + 1 ;
				//System.out.println( "Buffer Index: " + bufferIndex ) ;
				usedIndexByteSize = shapeIndexByteSize ;
				usedVertexByteSize = shapeVertexByteSize ;
			}
		
			draw.getPosition( position ) ;
			draw.getOffset( offset ) ;
			draw.getRotation( rotation ) ;
			draw.getScale( scale ) ;

			apply( matrix, matrixTemp, position, offset, rotation, scale ) ;

			uploadIndexToRAM( draw ) ;
			uploadVBOToRAM( draw, matrix ) ;
		}

		upload( bufferIndex ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	public void draw( final VertexAttrib[] _attributes )
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
			MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[i] ) ;
			MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[i] ) ;

			
			GLGeometryBuffer.prepareVertexAttributes( _attributes, vertexStrideBytes ) ;

			
			MGL.glDrawElements( style, indexLength[i], MGL.GL_UNSIGNED_INT, 0 ) ;
			
			
		}
		GLGeometryBuffer.disableVertexAttributes( _attributes ) ;
	}

	@Override
	public void shutdown()
	{
		MGL.glDeleteBuffers( indexID.length, indexID, 0 ) ;
		MGL.glDeleteBuffers( vboID.length, vboID, 0 ) ;
	}

	private int genNewBuffers()
	{
		final int offset = indexID.length ;

		indexID = IntegerBuffer.expand( indexID, 1 ) ;
		vboID = IntegerBuffer.expand( vboID, 1 ) ;
		indexLength = IntegerBuffer.expand( indexLength, 1 ) ;

		MGL.glGenBuffers( 1, indexID, offset ) ;
		MGL.glGenBuffers( 1, vboID, offset ) ;

		//System.out.println( "Create New Buffer: " + offset ) ;
		return offset ;
	}

	/**
		Upload the data to the specified vbo and ibo
		specified by the _index location.
	*/
	private void upload( final int _index )
	{
		//System.out.println( "Upload to Buffer Index: " + _index ) ;
		indexLength[_index] = indexBuffer.position() ;
		final int indiciesLengthBytes = indexLength[_index] * IBO_VAR_BYTE_SIZE ;
		final int verticiesLengthBytes = vertexBuffer.position() * VBO_VAR_BYTE_SIZE ;

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[_index] ) ;
		MGL.glBufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, indiciesLengthBytes, indexBuffer, MGL.GL_DYNAMIC_DRAW ) ;

		MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[_index] ) ;
		MGL.glBufferData( MGL.GL_ARRAY_BUFFER, verticiesLengthBytes, vertexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
	}
	
	private void uploadIndexToRAM( final Draw _draw )
	{
		final Shape shape = _draw.getShape() ;
		final int indexOffset = vertexBuffer.position() / vertexStride ;

		final int[] inds = shape.getRawIndicies() ;
		final int size = inds.length ;
		for( int i = 0; i < size; i++ )
		{
			indexBuffer.put( indexOffset + inds[i] ) ;
		}

		indexBuffer.put( PRIMITIVE_RESTART_INDEX ) ;
	}

	private void uploadVBOToRAM( final Draw _draw, final Matrix4 _matrix )
	{
		final Shape shape = _draw.getShape() ;
		final Shape.Swivel[] swivel = shape.getSwivel() ;
		final int verticiesSize = shape.getVertexSize() ;

		for( int i = 0; i < verticiesSize; i++ )
		{
			for( int j = 0; j < swivel.length; j++ )
			{
				switch( swivel[j] )
				{
					case NORMAL :
					case POINT  :
					{
						shape.getVector3( i, j, point ) ;
						Matrix4.multiply( point, _matrix, temp ) ;
						vertexBuffer.put( temp.x ) ;
						vertexBuffer.put( temp.y ) ;
						vertexBuffer.put( temp.z ) ;
						break ;
					}
					case COLOUR :
					{
						shape.getColour( i, j, shapeColour ) ;
						vertexBuffer.put( getABGR( shapeColour ) ) ;
						break ;
					}
					case UV     :
					{
						shape.getVector2( i, j, uv ) ;
						vertexBuffer.put( uv.x ) ;
						vertexBuffer.put( uv.y ) ;
						break ;
					}
				}
			}
		}
	}
}
