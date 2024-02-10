package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;
import java.util.ArrayList ;
import java.nio.* ;

import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;

import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.DrawInstancedBuffer ;
import com.linxonline.mallet.renderer.GeometryBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.IShape ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.IUniform ;

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

	private final ArrayList<Draw> draws = new ArrayList<Draw>() ;
	private GLProgram glProgram ;
	private final List<IUniform> uniforms = new ArrayList<IUniform>() ;
	private final List<GLStorage> storages = new ArrayList<GLStorage>() ;

	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private Int16Array indexBuffer ;
	private Float32Array vertexBuffer ;

	private WebGLBuffer[] indexID = new WebGLBuffer[1] ;
	private WebGLBuffer[] vboID = new WebGLBuffer[1] ;

	private int indexCount = 0 ;
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
		indexBuffer = Int16Array.create( indexByteSize / IBO_VAR_BYTE_SIZE ) ;
		vertexBuffer = Float32Array.create( vertexByteSize / VBO_VAR_BYTE_SIZE ) ;

		indexID[0] = MGL.createBuffer() ;
		vboID[0] = MGL.createBuffer() ;
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

		if( GLDrawInstancedBuffer.generateProgramUniforms( glProgram, program, uniforms ) == false )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
			stable = false ;
			return stable ;
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

		draws.clear() ;
		for( final GeometryBuffer buffer : _buffer.getBuffers() )
		{
			draws.addAll( buffer.getDraws() ) ;
		}

		// Draw objects may contain their own shape, however,
		// this is ignored. DrawInstancedBuffer defines a shape,
		// which is loaded instead, this shape is loaded only once.
		if( instanceLoaded == false )
		{
			incrementIndex = 0 ;
			incrementVertex = 0 ;

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

		MGL.useProgram( glProgram.id[0] ) ;

		final Matrix4 projection = ( isUI() ) ? _camera.getUIProjection() : _camera.getWorldProjection() ;
		final float[] projMatrix = projection.matrix ;

		MGL.uniformMatrix4fv( glProgram.inMVPMatrix, true, projMatrix ) ;
		if( loadProgramUniforms( glProgram, uniforms ) == false )
		{
			System.out.println( "Failed to load uniforms." ) ;
		}

		GLGeometryBuffer.enableVertexAttributes( attributes ) ;
		
		MGL.bindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
		MGL.bindBuffer( MGL.GL_ARRAY_BUFFER, vboID[0] ) ;

		GLGeometryBuffer.prepareVertexAttributes( attributes, vertexStrideBytes ) ;

		for( final Draw draw : draws )
		{
			if( draw.isHidden() /*|| occluder.occlude( draw )*/ )
			{
				continue ;
			}

			draw.getPosition( position ) ;
			draw.getOffset( offset ) ;
			draw.getRotation( rotation ) ;
			draw.getScale( scale ) ;

			apply( matrix, matrixTemp, position, offset, rotation, scale ) ;

			MGL.uniformMatrix4fv( glProgram.inModelMatrix, true, matrix.matrix ) ;
			MGL.drawElements( style, indexCount, MGL.GL_UNSIGNED_SHORT, 0 ) ;
		}

		GLGeometryBuffer.disableVertexAttributes( attributes ) ;
	}

	@Override
	public void shutdown()
	{
		MGL.deleteBuffer( indexID[0] ) ;
		MGL.deleteBuffer( vboID[0] ) ;
	}

	private void uploadInstanceToVRAM()
	{
		final int indiciesLengthBytes = incrementIndex * IBO_VAR_BYTE_SIZE ;
		final int verticiesLengthBytes = incrementVertex * VBO_VAR_BYTE_SIZE ;

		MGL.bindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[INSTANCE_INDEX] ) ;
		MGL.bindBuffer( MGL.GL_ARRAY_BUFFER, vboID[INSTANCE_INDEX] ) ;

		MGL.bufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
		MGL.bufferData( MGL.GL_ARRAY_BUFFER, vertexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
	}

	private int uploadInstanceToRAM( final IShape _shape )
	{
		final int indexStart = incrementIndex ;
		final int indexOffset = incrementVertex / vertexStride ;

		final int[] inds = _shape.getRawIndices() ;
		final int size = inds.length ;
		for( int i = 0; i < size; i++ )
		{
			indexBuffer.set( i, ( short )( indexOffset + inds[i] ) ) ;
		}

		final float[] vertices = _shape.getRawVertices() ;
		for( int j = 0; j < vertices.length; ++j )
		{
			vertexBuffer.set( incrementVertex++, vertices[j] ) ;
		}

		return size ;
	}
}
