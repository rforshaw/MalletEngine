package com.linxonline.mallet.renderer.web.gl ;

import java.util.Arrays ;

import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;
import org.teavm.jso.typedarrays.Uint8Array ;

import com.linxonline.mallet.util.buffers.IntegerBuffer ;

import com.linxonline.mallet.renderer.TextDraw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.TextBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.Glyph ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLTextBuffer extends GLBuffer
{
	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private Int16Array indexBuffer ;
	private Float32Array vertexBuffer ;

	private WebGLBuffer[] indexID = new WebGLBuffer[1] ;
	private WebGLBuffer[] vboID = new WebGLBuffer[1] ;
	private int[] indexLength = new int[1] ;

	private VertexAttrib[] attributes = null ;
	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private int incrementIndex = 0 ;
	private int incrementVertex = 0 ;

	private GLProgram glProgram ;
	private Program mapProgram = new Program() ;

	private final MalletColour shapeColour = new MalletColour() ;
	private final Vector2 uv = new Vector2() ;
	private final Vector3 point = new Vector3() ;
	private final Vector3 temp = new Vector3() ;

	private final Matrix4 matrix = new Matrix4() ;
	private final Matrix4 matrixTemp = Matrix4.createTempIdentity() ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 rotation = new Vector3() ;
	private final Vector3 scale = new Vector3( 1, 1, 1 ) ;

	private AssetLookup<Storage, GLStorage> storages ;
	private boolean stable = false ;

	public GLTextBuffer( final TextBuffer _buffer )
	{
		this( _buffer, GLUtils.MIN_INDEX_BYTE_SIZE,
					   GLUtils.MIN_VERTEX_BYTE_SIZE,
					   GLUtils.MAX_INDEX_BYTE_SIZE,
					   GLUtils.MAX_VERTEX_BYTE_SIZE ) ;
	}

	public GLTextBuffer( final TextBuffer _buffer,
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

		indexBuffer = Int16Array.create( indexByteSize / IBO_VAR_BYTE_SIZE ) ;
		vertexBuffer = Float32Array.create( vertexByteSize / VBO_VAR_BYTE_SIZE ) ;

		indexID[0] = MGL.createBuffer() ;
		vboID[0] = MGL.createBuffer() ;
	}

	public boolean update( final TextBuffer _buffer,
						   final AssetLookup<Program, GLProgram> _lookup,
						   final AssetLookup<Storage, GLStorage> _storages )
	{
		final Program program = _buffer.getProgram() ;

		glProgram = _lookup.getRHS( program.index() ) ;
		if( glProgram == null )
		{
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

		final MalletFont font = program.getUniform( "inTex0", MalletFont.class ) ;
		final MalletFont.Metrics metrics = font.getMetrics() ;
		final GLFont glFont = GLRenderer.getFont( font ) ;

		{
			final Shape shape = glFont.getShapeWithChar( '\0' ) ;
			if( attributes == null )
			{
				// We only want to build the attributes once, 
				// we know a TextBuffers program can't be fully 
				// replaced, they'd have to create a new TextBuffer 
				// to do that.
				attributes = constructVertexAttrib( shape.getAttribute(), glProgram ) ;
			}

			if( vertexStride <= 0 )
			{
				// Attribute is not expected to change once it is 
				// set, so we'll only calculate the swivel once.
				vertexStride = calculateVertexSize( shape.getAttribute() ) ;
				vertexStrideBytes = vertexStride * VBO_VAR_BYTE_SIZE ;
			}

			switch( shape.getStyle() )
			{
				case LINES      : style = MGL.GL_LINES ;      break ;
				case LINE_STRIP : style = MGL.GL_LINE_STRIP ; break ;
				case FILL       : style = MGL.GL_TRIANGLES ;  break ;
				default         : style = MGL.GL_LINES ;      break ;
			}
		}

		int bufferIndex = 0 ;
		int usedIndexByteSize = 0 ;
		int usedVertexByteSize = 0 ;

		incrementIndex = 0 ;
		incrementVertex = 0 ;

		for( final TextDraw draw : _buffer.getTextDraws() )
		{
			if( draw.isHidden() == true )
			{
				continue ;
			}

			draw.getPosition( position ) ;
			draw.getOffset( offset ) ;
			draw.getRotation( rotation ) ;
			draw.getScale( scale ) ;

			apply( matrix, matrixTemp, position, offset, rotation, scale ) ;

			final StringBuilder text = draw.getText() ;
			int start = draw.getStart() ;
			int end = draw.getEnd() ;

			{
				final int length = text.length() ;
				end = ( end < length ) ? end : length ;
				end = ( end >= 0 ) ? end : 0 ;

				start = ( start < end ) ? start : end ;
				start = ( start >= 0 ) ? start : 0 ;
			}

			final int initialIndexOffset = incrementVertex / vertexStride ;

			final int length = end - start ;
			for( int i = 0; i < length; i++ )
			{
				final char c = text.charAt( start + i ) ;
				switch( c )
				{
					case '\n' :
					{
						matrix.setX( position.x + offset.x ) ;
						matrix.translate( 0.0f, metrics.getHeight(), 0.0f ) ;
						break ;
					}
				}

				final Glyph glyph = metrics.getGlyphWithChar( c ) ;
				final Shape shape = glFont.getShapeWithChar( c ) ;
				if( shape == null )
				{
					// If a shape does not exist then the GLFont 
					// needs to be recreated as the MalletFont has a 
					// new glyph that is not yet represented.
					// We need to update all text as the texture co-ordinates 
					// used by the previously uploaded text will now be wrong.
					//GLGeometryUploader.this.forceUpdate() ;
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

				final Shape.Attribute[] swivel = shape.getAttribute() ;
				final int verticiesSize = shape.getVerticesSize() ;

				final int GLYPH_POINTS = 4 ;
				final int indexOffset = initialIndexOffset + ( i * GLYPH_POINTS ) ;

				final int size = shape.getIndicesSize() ; 
				for( int j = 0; j < size; j++ )
				{
					indexBuffer.set( incrementIndex++, ( short )( indexOffset + shape.getIndex( j ) ) ) ;
				}

				for( int j = 0; j < verticiesSize; j++ )
				{
					for( int k = 0; k < swivel.length; k++ )
					{
						switch( swivel[k] )
						{
							case VEC3  :
							{
								shape.getVector3( j, k, point ) ;
								Matrix4.multiply( point, matrix, temp ) ;
								//System.out.println( "Vec3: " + temp.toString() ) ;
								vertexBuffer.set( incrementVertex++, temp.x ) ;
								vertexBuffer.set( incrementVertex++, temp.y ) ;
								vertexBuffer.set( incrementVertex++, temp.z ) ;
								break ;
							}
							case FLOAT :
							{
								// GLDraw colour overrides Shapes colour.
								final MalletColour colour = draw.getColour() ;
								final MalletColour col = ( colour != null ) ? colour : shape.getColour( j, k, shapeColour ) ;
								vertexBuffer.set( incrementVertex++, getABGR( col ) ) ;
								break ;
							}
							case VEC2     :
							{
								//System.out.println( "Vec2: " + uv.toString() ) ;
								shape.getVector2( j, k, uv ) ;
								vertexBuffer.set( incrementVertex++, uv.x ) ;
								vertexBuffer.set( incrementVertex++, uv.y ) ;
								break ;
							}
						}
					}
				}

				//System.out.println( "Glyph Width: " + glyph.getWidth() ) ;
				matrix.translate( glyph.getWidth(), 0.0f, 0.0f ) ;
				//System.out.println( "Matrix: " + matrix.toString() ) ;
			}

			//indexBuffer.put( PRIMITIVE_RESTART_INDEX ) ;
		}

		upload( bufferIndex ) ;

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

		GLTextBuffer.enableVertexAttributes( attributes ) ;
		MGL.useProgram( glProgram.id[0] ) ;

		final float[] matrix = _projection.matrix ;

		MGL.uniformMatrix4fv( glProgram.inMVPMatrix, true, matrix ) ;
		if( glProgram.loadUniforms( mapProgram ) == false )
		{
			System.out.println( "Failed to load uniforms." ) ;
		}

		glProgram.bindBuffers( mapProgram, storages ) ;
		
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

			MGL.bindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[i] ) ;
			MGL.bindBuffer( MGL.GL_ARRAY_BUFFER, vboID[i] ) ;

			GLTextBuffer.prepareVertexAttributes( attributes, vertexStrideBytes ) ;
			MGL.drawElements( style, length, MGL.GL_UNSIGNED_SHORT, 0 ) ;
		}
		GLTextBuffer.disableVertexAttributes( attributes ) ;
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

	public static WebGLBuffer[] expand( final WebGLBuffer[] _from, final int _extra )
	{
		final int length = _from.length + _extra ;
		final WebGLBuffer[] to = new WebGLBuffer[length] ;
		System.arraycopy( _from, 0, to, 0, _from.length ) ;
		return to ;
	}
}
