package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;
import java.nio.* ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.DrawInstancedBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.IShape ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.IntVector2 ;

import com.linxonline.mallet.io.serialisation.Serialise ;

public final class GLDrawInstancedBuffer extends GLBuffer
{
	private static final int INSTANCE_INDEX = 0 ;

	private String storageName = null ;
	private int order ;
	private VertexAttrib[] attributes = null ;

	private GLProgram glProgram ;
	private Program mapProgram = new Program() ;

	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private IntBuffer indexBuffer ;
	private FloatBuffer vertexBuffer ;

	private int[] indexID = new int[1] ;
	private int[] vboID = new int[1] ;

	private int indexCount = 0 ;
	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private final Transformations transformations = new Transformations() ;
	private final Storage transStorage = new Storage( transformations ) ;
	private final GLStorage glTransStorage = new GLStorage( transStorage ) ;

	private AssetLookup<Storage, GLStorage> storages ;
	private boolean instanceLoaded = false ;
	private boolean stable = false ;

	public GLDrawInstancedBuffer( final DrawInstancedBuffer _buffer )
	{
		this( _buffer, GLUtils.MIN_INDEX_BYTE_SIZE,
					   GLUtils.MIN_VERTEX_BYTE_SIZE,
					   GLUtils.MAX_INDEX_BYTE_SIZE,
					   GLUtils.MAX_VERTEX_BYTE_SIZE ) ;
	}

	public GLDrawInstancedBuffer( final DrawInstancedBuffer _buffer,
								  final int _indexByteSize,
								  final int _vertexByteSize,
								  final int _maxIndexByteSize,
								  final int _maxVertexByteSize )
	{
		super( _buffer.isUI() ) ;

		indexByteSize = _indexByteSize ;
		vertexByteSize = _vertexByteSize ;

		maxIndexByteSize = _maxIndexByteSize ;
		maxVertexByteSize = _maxVertexByteSize ;

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

	public boolean update( final DrawInstancedBuffer _buffer,
						   final AssetLookup<Program, GLProgram> _programs,
						   final AssetLookup<?, GLBuffer> _buffers,
						   final AssetLookup<Storage, GLStorage> _storages )
	{
		final Program program = _buffer.getProgram() ;
		glProgram = _programs.getRHS( program.index() ) ;
		if( glProgram == null )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
			stable = false ;
			return stable ;
		}

		if( glProgram.remap( program, mapProgram ) == false )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
			stable = false ;
			return stable ;
		}

		// For instanced rendering to work we require a storage buffer 
		// that contains all matrices of each of the draw objects.
		// The user defined program must define a shared storage buffer 
		// that aligns with the storageName.
		if( storageName == null )
		{
			storageName = _buffer.getStorageName() ;
			mapProgram.removeStorage( storageName ) ;
			mapProgram.mapStorage( storageName, transStorage ) ;
		}

		if( attributes == null )
		{
			// We only want to build the attributes once, 
			// we know a DrawBuffers program can't be fully 
			// replaced, they'd have to create a new GeometryBuffer 
			// to do that.
			attributes = constructVertexAttrib( _buffer.getAttribute(), glProgram ) ;
		}

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

		// Draw objects may contain their own shape, however,
		// this is ignored. DrawInstancedBuffer defines a shape,
		// which is loaded instead, this shape is loaded only once.
		if( instanceLoaded == false )
		{
			indexBuffer.position( 0 ) ;
			vertexBuffer.position( 0 ) ;

			int usedIndexByteSize = 0 ;
			int usedVertexByteSize = 0 ;

			final IShape shape = _buffer.getShape() ;
			final int shapeIndexByteSize = shape.getIndicesSize() * IBO_VAR_BYTE_SIZE ;
			final int shapeVertexByteSize = shape.getVerticesSize() * vertexStrideBytes ;

			usedIndexByteSize += shapeIndexByteSize ;
			usedVertexByteSize += shapeVertexByteSize ;
			if( usedIndexByteSize > indexByteSize || usedVertexByteSize > vertexByteSize )
			{
				System.out.println( "Failed to expand buffer to fit instanced shape." ) ;
				stable = false ;
				return stable ;
			}

			indexCount = uploadInstanceToRAM( shape ) ;
			uploadInstanceToVRAM() ;
			instanceLoaded = true ;
		}

		// Update the Storage buffer with the new transformations 
		// from the draw objects.
		transformations.clear() ;
		transformations.add( _buffer.getBuffers() ) ;
		glTransStorage.update( transStorage ) ;

		storages = _storages ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	@Override
	public void draw( final Matrix4 _projection )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		MGL.glUseProgram( glProgram.id[0] ) ;

		final float[] matrix = _projection.matrix ;

		MGL.glUniformMatrix4fv( glProgram.inMVPMatrix, 1, true, matrix, 0 ) ;
		if( glProgram.loadUniforms( mapProgram ) == false )
		{
			System.out.println( "Failed to load uniforms." ) ;
		}

		{
			boolean foundInstanceBuffer = false ;

			final List<String> buffers = glProgram.program.getBuffers() ;
			final int size = buffers.size() ;
			for( int i = 0; i < size; ++i )
			{
				final String name = buffers.get( i ) ;
				if( name.equals( storageName ) == true ) 
				{
					// Map the transformation storage to the specified 
					// name within the program.
					foundInstanceBuffer = true ;
					MGL.glBindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, glTransStorage.id[0] ) ;
				}
				else
				{
					final Storage storage = mapProgram.getStorage( name ) ;
					if( storage == null )
					{
						System.out.println( "Failed to find storage buffer, skipping..." ) ;
						continue ;
					}

					final GLStorage glStorage = storages.getRHS( storage.index() ) ;
					MGL.glBindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, glStorage.id[0] ) ;
				}
			}

			if( foundInstanceBuffer == false )
			{
				System.out.println( "Instances buffer not specified in program." ) ;
				return ;
			}
		}

		GLGeometryBuffer.enableVertexAttributes( attributes ) ;

		MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[INSTANCE_INDEX] ) ;
		MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[INSTANCE_INDEX] ) ;

		GLGeometryBuffer.prepareVertexAttributes( attributes, vertexStrideBytes ) ;

		MGL.glDrawElementsInstanced( style, indexCount, MGL.GL_UNSIGNED_INT, 0L, transformations.drawCount ) ;

		GLGeometryBuffer.disableVertexAttributes( attributes ) ;
	}

	@Override
	public void shutdown()
	{
		glTransStorage.shutdown() ;
		MGL.glDeleteBuffers( indexID.length, indexID, 0 ) ;
		MGL.glDeleteBuffers( vboID.length, vboID, 0 ) ;
	}

	private void uploadInstanceToVRAM()
	{
		final int indiciesLengthBytes = indexBuffer.position() * IBO_VAR_BYTE_SIZE ;
		final int verticiesLengthBytes = vertexBuffer.position() * VBO_VAR_BYTE_SIZE ;

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[INSTANCE_INDEX] ) ;
		MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[INSTANCE_INDEX] ) ;

		MGL.glBufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, indiciesLengthBytes, indexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
		MGL.glBufferData( MGL.GL_ARRAY_BUFFER, verticiesLengthBytes, vertexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
	}

	private int uploadInstanceToRAM( final IShape _shape )
	{
		final int indexStart = indexBuffer.position() ;
		final int indexOffset = vertexBuffer.position() / vertexStride ;

		final int[] inds = _shape.getRawIndices() ;
		final int size = inds.length ;
		for( int i = 0; i < size; i++ )
		{
			indexBuffer.put( indexOffset + inds[i] ) ;
		}

		vertexBuffer.put( _shape.getRawVertices() ) ;

		return size ;
	}

	private static class Transformations implements Storage.IData
	{
		private final Matrix4 matrix = new Matrix4() ;
		private final Matrix4 matrixTemp = Matrix4.createTempIdentity() ;

		private final Vector3 position = new Vector3() ;
		private final Vector3 offset = new Vector3() ;
		private final Vector3 rotation = new Vector3() ;
		private final Vector3 scale = new Vector3() ;
	
		private final List<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;
		public int drawCount = 0 ;

		private Transformations() {}

		public void add( final List<GeometryBuffer> _buffers )
		{
			buffers.addAll( _buffers ) ;
		}

		public void clear()
		{
			buffers.clear() ;
			drawCount = 0 ;
		}

		@Override
		public int getLength()
		{
			drawCount = 0 ;
			for( final GeometryBuffer buffer : buffers )
			{
				final List<Draw> draws = buffer.getDraws() ;
				drawCount += draws.size() ;
			}
			return drawCount * 16 * 4 ;
		}

		@Override
		public void serialise( Serialise.Out _out )
		{
			for( final GeometryBuffer buffer : buffers )
			{
				final List<Draw> draws = buffer.getDraws() ;
				for( final Draw draw : draws )
				{
					draw.getPosition( position ) ;
					draw.getOffset( offset ) ;
					draw.getRotation( rotation ) ;
					draw.getScale( scale ) ;

					apply( matrix, matrixTemp, position, offset, rotation, scale ) ;
					matrix.transpose() ;

					_out.writeFloats( matrix.matrix ) ;
				}
			}
		}
	}
}
