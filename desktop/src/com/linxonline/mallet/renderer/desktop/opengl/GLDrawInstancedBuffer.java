package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;
import java.nio.* ;

import com.linxonline.mallet.util.Parallel ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.DrawInstancedBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.IShape ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLDrawInstancedBuffer extends GLBuffer
{
	private static final int INSTANCE_INDEX = 0 ;

	private String storageName = null ;
	private VertexAttrib[] attributes = null ;

	private GLProgram glProgram ;
	private final GLProgram.UniformState uniformState = new GLProgram.UniformState() ;
	private final List<GLProgram.ILoadUniform> uniforms = new ArrayList<GLProgram.ILoadUniform>() ;
	private final List<GLStorage> storages = new ArrayList<GLStorage>() ;

	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private IntBuffer indexBuffer ;
	private FloatBuffer vertexBuffer ;

	private final GLIndexWrite indexWrite ;
	private final GLVertWrite vertWrite ;

	private int[] indexID = new int[1] ;
	private int[] vboID = new int[1] ;

	private int indexCount = 0 ;
	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private final Transformations transformations = new Transformations() ;
	private final Storage transStorage = new Storage( transformations ) ;
	private final GLStorage glTransStorage = new GLStorage( transStorage ) ;

	private boolean isStatic = false ;
	private boolean denyTransUpdate = false ;

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

		indexWrite = new GLIndexWrite( indexBuffer ) ;
		vertWrite = new GLVertWrite( vertexBuffer ) ;

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

		uniforms.clear() ;
		if( glProgram.buildProgramUniforms( program, uniforms ) == false )
		{
			stable = false ;
			return stable ;
		}

		isStatic = _buffer.isStatic() ;
		denyTransUpdate = false ;

		GLDrawInstancedBuffer.generateStorages( glProgram, program, _storages, storages ) ;

		// For instanced rendering to work we require a storage buffer 
		// that contains all matrices of each of the draw objects.
		// The user defined program must define a shared storage buffer 
		// that aligns with the storageName.
		if( storageName == null )
		{
			storageName = _buffer.getStorageName() ;
		}

		{
			final GLProgram.SSBuffer ssb = glProgram.getSSBuffer( storageName ) ;
			if( ssb == null )
			{
				System.out.println( storageName + " buffer not specified in program." ) ;
				stable = false ;
				return stable ;
			}

			storages.set( ssb.getIndex(), glTransStorage ) ;
		}

		if( attributes == null )
		{
			// We only want to build the attributes once, 
			// we know a DrawBuffers program can't be fully 
			// replaced, they'd have to create a new GeometryBuffer 
			// to do that.
			attributes = constructVertexAttrib( program, glProgram ) ;
		}

		switch( program.getStyle() )
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

			// GeometryBuffer swivel is not expected to change once it is 
			// set, so we'll only calculate the swivel once.
			vertexStride = calculateVertexSize( shape.getAttribute() ) ;
			vertexStrideBytes = vertexStride * VBO_VAR_BYTE_SIZE ;

			indexCount = uploadInstanceToRAM( shape ) ;
			uploadInstanceToVRAM() ;
			instanceLoaded = true ;
		}

		// Update the Storage buffer with the new transformations 
		// from the draw objects.
		transformations.clear() ;
		transformations.add( _buffer.getBuffers() ) ;

		// We successfully updated the buffer, nothing more is need 
		// but to inform the trigger.
		stable = true ;
		return stable ;
	}

	@Override
	public void draw( final GLCamera _camera )
	{
		if( stable == false )
		{
			// We only want to draw if we've successfully 
			// updated the buffer.
			return ;
		}

		if( denyTransUpdate == false )
		{
			glTransStorage.update( transStorage ) ;
			if( glTransStorage.hasValidUpload() == false)
			{
				System.out.println( "Does not contain valid state" ) ;
				return ;
			}

			// If the buffer is intended to be static
			// we only want to load the data once and then
			// never touch it again.
			denyTransUpdate = ( isStatic ) ? true : false ;
		}

		MGL.glUseProgram( glProgram.id[0] ) ;

		final Matrix4 view = ( isUI() ) ? IDENTITY : _camera.getView() ;
		final Matrix4 projection = ( isUI() ) ? _camera.getUIProjection() : _camera.getProjection() ;

		MGL.glUniformMatrix4fv( glProgram.inViewMatrix, 1, false, view.matrix, 0 ) ;
		MGL.glUniformMatrix4fv( glProgram.inProjectionMatrix, 1, false, projection.matrix, 0 ) ;
		MGL.glUniform2f( glProgram.inResolution, _camera.getWidth(), _camera.getHeight() ) ;

		{
			uniformState.reset() ;
			final int size = uniforms.size() ;
			for( int i = 0; i < size; ++i )
			{
				if( uniforms.get( i ).load( uniformState ) == false )
				{
					System.out.println( "Failed to load uniforms." ) ;
					return ;
				}
			}
		}

		{
			final int size = storages.size() ;
			for( int i = 0; i < size; ++i )
			{
				final GLStorage storage = storages.get( i ) ;
				MGL.glBindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, storage.getID() ) ;
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
		final int indexOffset = vertexBuffer.position() / vertexStride ;

		_shape.writeIndices( indexOffset, indexWrite ) ;
		_shape.writeVertices( vertWrite ) ;

		return _shape.getIndicesSize() ;
	}

	private static class TransformationUpdater implements Parallel.IListRun<Draw>
	{
		private Storage.ISerialise out ;

		public void set( final Storage.ISerialise _out )
		{
			out = _out ;
		}

		@Override
		public void run( final int _start, final int _end, final List<Draw> _draws )
		{
			final Matrix4 mat = new Matrix4() ;

			for( int i = _start; i < _end; ++i )
			{
				_draws.get( i ).getTransformation( mat ) ;
				out.writeFloats( i * 16 * 4, mat.matrix ) ;
			}
		}
	}
	
	private static class Transformations implements Storage.IData
	{
		private final List<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;
		private final TransformationUpdater updater = new TransformationUpdater() ;

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
			final int size = buffers.size() ;
			for( int i = 0; i < size; ++i )
			{
				final GeometryBuffer buffer = buffers.get( i ) ;
				final List<Draw> draws = buffer.getDraws() ;
				drawCount += draws.size() ;
			}
			return drawCount * ( 16 * 4 ) ;
		}

		@Override
		public void serialise( Storage.ISerialise _out )
		{
			final int bufferSize = buffers.size() ;
			for( int i = 0; i < bufferSize; ++i )
			{
				final GeometryBuffer buffer = buffers.get( i ) ;

				//final long startTime = System.currentTimeMillis() ;

				updater.set( _out ) ;
				Parallel.forBatch( buffer.getDraws(), 1000, updater ) ;
				updater.set( null ) ;

				//final long endTime = System.currentTimeMillis() ;
				//System.out.println( "GL Time Taken: " + ( endTime - startTime ) ) ;
			}
		}
	}
}
