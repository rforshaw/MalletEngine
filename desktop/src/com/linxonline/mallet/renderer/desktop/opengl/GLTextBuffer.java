package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.ArrayList ;
import java.nio.* ;

import com.linxonline.mallet.util.buffers.IntegerBuffer ;

import com.linxonline.mallet.renderer.TextDraw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.Attribute ;
import com.linxonline.mallet.renderer.TextBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Colour ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.IUniform ;

import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class GLTextBuffer extends GLBuffer
{
	private final int maxIndexByteSize ;
	private final int maxVertexByteSize ;

	private int indexByteSize ;
	private int vertexByteSize ;

	private IntBuffer indexBuffer ;
	private FloatBuffer vertexBuffer ;

	private int[] indexID = new int[1] ;
	private int[] vboID = new int[1] ;
	private int[] indexLength = new int[1] ;

	private VertexAttrib[] attributes = null ;
	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,

	private GLProgram glProgram ;
	private final GLProgram.UniformState uniformState = new GLProgram.UniformState() ;
	private final List<GLProgram.ILoadUniform> uniforms = new ArrayList<GLProgram.ILoadUniform>() ;
	private final List<GLStorage> storages = new ArrayList<GLStorage>() ;

	private final Colour shapeColour = new Colour() ;
	private final Vector2 vec2 = new Vector2() ;
	private final Vector3 point = new Vector3() ;
	private final Vector3 temp = new Vector3() ;

	private final Matrix4 matrix = new Matrix4() ;
	private final Matrix4 matrixTemp = Matrix4.createIdentity() ;

	private final AABB boundary = AABB.create() ;
	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 rotation = new Vector3() ;
	private final Vector3 scale = new Vector3( 1, 1, 1 ) ;

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

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( indexByteSize ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( vertexByteSize ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;

		MGL.glGenBuffers( 1, indexID, 0 ) ;
		MGL.glGenBuffers( 1, vboID, 0 ) ;
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

		uniforms.clear() ;
		if( glProgram.buildProgramUniforms( program, uniforms ) == false )
		{
			stable = false ;
			return stable ;
		}

		GLTextBuffer.generateStorages( glProgram, program, _storages, storages ) ;

		final IUniform uFont = program.getUniform( "inTex0" ) ;
		if( !( uFont instanceof Font ) )
		{
			// The font passed in needs to be a Font.
			stable = false ;
			return stable ;
		}

		final Font font = ( Font )uFont ;
		final Font.Metrics metrics = font.getMetrics() ;
		final GLFont glFont = GLRenderer.getFont( font ) ;

		final float fScale = font.getPointSize() ;
		final float fAscent = metrics.getAscent() ;
		
		{
			final Shape shape = glFont.getShapeWithChar( '\0' ) ;
			if( attributes == null )
			{
				// We only want to build the attributes once, 
				// we know a TextBuffers program can't be fully 
				// replaced, they'd have to create a new TextBuffer 
				// to do that.
				attributes = constructVertexAttrib( program, glProgram ) ;
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

		indexBuffer.position( 0 ) ;
		vertexBuffer.position( 0 ) ;

		final List<TextDraw> draws = _buffer.getTextDraws() ;
		final int drawSize = draws.size() ;
		for( int l = 0; l < drawSize; ++l )
		{
			final TextDraw draw = draws.get( l ) ;
			if( draw.isHidden() == true )
			{
				continue ;
			}

			draw.getBoundary( boundary, vec2 ) ;

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

			final float metricHeight = metrics.getHeight() ;
			final float baseX = position.x + offset.x ;
			final float baseY = position.y + offset.y ;

			int newline = 0 ;
			float currentX = baseX ;
			float currentY = baseY ;

			final int length = end - start ;
			for( int i = 0; i < length; i++ )
			{
				final char c = text.charAt( start + i ) ;
				switch( c )
				{
					case '\n' :
					{
						currentX = position.x + offset.x ;
						currentY += metricHeight ;

						apply( matrix, matrixTemp, position, offset, rotation, scale ) ;
						matrix.translate( 0.0f, currentY - baseY, 0.0f ) ;
						break ;
					}
				}

				float endWordX = currentX ;
				if( c != ' ' )
				{
					// Determine if we have to jump to the next line
					// if the word the character resides within cannot
					// fit on the current line.
					for( int j = ( start + i + 1 ); j < length; ++j )
					{
						final char t = text.charAt( j ) ;
						if( t == ' ' )
						{
							// Consider a space to be the end of a word.
							break ;
						}

						final Glyph g = metrics.getGlyphWithChar( t ) ;
						endWordX += g.getWidth() ;
					}

					if( boundary.intersectPoint( endWordX, currentY ) == false )
					{
						currentX = position.x + offset.x ;
						currentY += metricHeight ;

						apply( matrix, matrixTemp, position, offset, rotation, scale ) ;
						matrix.translate( 0.0f, currentY - baseY, 0.0f ) ;
					}
				}

				if( boundary.intersectPoint( currentX, currentY ) == false )
				{
					continue ;
				}

				final Glyph glyph = metrics.getGlyphWithChar( c ) ;
				final Shape shape = glFont.getShapeWithChar( c ) ;
				if( shape == null )
				{
					// If a shape does not exist then the GLFont 
					// needs to be recreated as the Font has a 
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

				final int indexOffset = vertexBuffer.position() / vertexStride ;

				final int size = shape.getIndicesSize() ; 
				for( int j = 0; j < size; j++ )
				{
					indexBuffer.put( indexOffset + shape.getIndex( j ) ) ;
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
								point.multiply( fScale ) ;
								point.add( 0, fAscent, 0 ) ;

								Matrix4.multiply( point, matrix, temp ) ;
								vertexBuffer.put( temp.x ) ;
								vertexBuffer.put( temp.y ) ;
								vertexBuffer.put( temp.z ) ;
								break ;
							}
							case FLOAT :
							{
								// GLDraw colour overrides Shapes colour.
								final Colour colour = draw.getColour() ;
								final Colour col = ( colour != null ) ? colour : shape.getColour( j, k, shapeColour ) ;
								vertexBuffer.put( getABGR( col ) ) ;
								break ;
							}
							case VEC2     :
							{
								shape.getVector2( j, k, vec2 ) ;
								vertexBuffer.put( vec2.x ) ;
								vertexBuffer.put( vec2.y ) ;
								break ;
							}
						}
					}
				}

				matrix.translate( glyph.getWidth(), 0.0f, 0.0f ) ;
				currentX += glyph.getWidth() ;
			}

			indexBuffer.put( PRIMITIVE_RESTART_INDEX ) ;
		}

		upload( bufferIndex ) ;

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

		GLTextBuffer.enableVertexAttributes( attributes ) ;
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

		MGL.glEnable( MGL.GL_MULTISAMPLE ) ;

		GLTextBuffer.bindBuffers( storages ) ;

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

			GLTextBuffer.prepareVertexAttributes( attributes, vertexStrideBytes ) ;
			MGL.glDrawElements( style, length, MGL.GL_UNSIGNED_INT, 0 ) ;
		}
		GLTextBuffer.disableVertexAttributes( attributes ) ;

		MGL.glDisable( MGL.GL_MULTISAMPLE ) ;
	}

	@Override
	public void shutdown()
	{
		MGL.glDeleteBuffers( 1, indexID, 0 ) ;
		MGL.glDeleteBuffers( 1, vboID, 0 ) ;
	}

	private void expandIndexBuffer()
	{
		if( indexByteSize == maxIndexByteSize )
		{
			// We can't expand the buffer any more.
			return ;
		}
	
		final int doubleCapacity = indexByteSize * 2 ;
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

	private void expandVertexBuffer()
	{
		if( vertexByteSize == maxVertexByteSize )
		{
			// We can't expand the buffer any more.
			return ;
		}

		final int doubleCapacity = vertexByteSize * 2 ;
		vertexByteSize = ( doubleCapacity > maxVertexByteSize ) ? maxVertexByteSize : doubleCapacity ;

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

	private static void apply( final Matrix4 _mat4,
							   final Matrix4 _temp,
							   final Vector3 _position,
							   final Vector3 _offset,
							   final Vector3 _rotation,
							   final Vector3 _scale )
	{
		_mat4.setIdentity() ;
		_mat4.setPosition( _position.x, _position.y, 0.0f ) ;

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
		_temp.setPosition( _offset.x, _offset.y, _offset.z ) ;
		_mat4.multiply( _temp ) ;
		_temp.setIdentity() ;
	}
}
