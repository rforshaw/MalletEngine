package com.linxonline.mallet.renderer.android.opengl ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.nio.* ;

import com.linxonline.mallet.renderer.TextDraw ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.TextBuffer ;
import com.linxonline.mallet.renderer.AssetLookup ;
import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Colour ;
import com.linxonline.mallet.renderer.Glyph ;
import com.linxonline.mallet.renderer.IUniform ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLTextBuffer extends GLBuffer
{
	private final static int INDEX_BYTE_SIZE = 10000 ;
	private final static int VERTEX_BYTE_SIZE = 10000 ;

	private final short[] indicies ;
	private final float[] verticies ;

	private final ShortBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	private final int[] indexID = new int[1] ;
	private final int[] vboID = new int[1] ;

	private VertexAttrib[] attributes = null ;
	private int vertexStride = -1 ;
	private int vertexStrideBytes = -1 ;			// The size in bytes of a vertex
	private int style = -1 ;						// OpenGL GL_TRIANGLES, GL_LINES,
	private int indexLength = 0 ;

	private GLProgram glProgram ;
	private final List<IUniform> uniforms = new ArrayList<IUniform>() ;
	private final List<GLStorage> storages = new ArrayList<GLStorage>() ;

	private final Colour shapeColour = new Colour() ;
	private final Vector2 uv = new Vector2() ;
	private final Vector3 point = new Vector3() ;
	private final Vector3 temp = new Vector3() ;

	private final Matrix4 matrix = new Matrix4() ;
	private final Matrix4 matrixTemp = Matrix4.createTempIdentity() ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 rotation = new Vector3() ;
	private final Vector3 scale = new Vector3( 1, 1, 1 ) ;

	private boolean stable = false ;

	public GLTextBuffer( final TextBuffer _buffer )
	{
		super( _buffer.isUI() ) ;
		final int indexSize = INDEX_BYTE_SIZE / IBO_VAR_BYTE_SIZE ;
		final int vboSize = VERTEX_BYTE_SIZE / VBO_VAR_BYTE_SIZE ;

		indicies = new short[indexSize] ;
		verticies = new float[vboSize] ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( INDEX_BYTE_SIZE ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( VERTEX_BYTE_SIZE ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asShortBuffer() ;

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

		if( GLTextBuffer.generateProgramUniforms( glProgram, program, uniforms ) == false )
		{
			// We've failed to update the buffer something in
			// the program map is wrong or has yet to be loaded.
			stable = false ;
			return stable ;
		}

		GLTextBuffer.generateStorages( glProgram, program, _storages, storages ) ;

		final IUniform uFont = program.getUniform( "inTex0" ) ;
		if( uFont.getType() != IUniform.Type.FONT )
		{
			// The font passed in needs to be a Font.
			stable = false ;
			return stable ;
		}

		final Font font = ( Font )uFont ;
		final Font.Metrics metrics = font.getMetrics() ;
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

		int indexIncrement = 0 ;
		int vertexIncrement = 0 ;

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
			final int start = draw.getStart() ;
			final int end = draw.getEnd() ;

			if( end > text.length() )
			{
				System.out.println( text ) ;
				continue ;
			}

			final int initialIndexOffset = vertexIncrement / vertexStride ;

			final int length = end - start ;
			for( int i = 0; i < length; i++ )
			{
				final char c = text.charAt( start + i ) ;
				switch( c )
				{
					case '\n' :
					{
						apply( matrix, matrixTemp, position, offset, rotation, scale ) ;
						matrix.translate( 0.0f, metrics.getHeight(), 0.0f ) ;
						break ;
					}
				}

				final Glyph glyph = metrics.getGlyphWithChar( c ) ;
				final Shape shape = glFont.getShapeWithChar( c ) ;
				if( shape == null )
				{
					//System.out.println( "Missing: " + c + " Glyph: " + glyph ) ;
					// If a shape does not exist then the GLFont 
					// needs to be recreated as the Font has a 
					// new glyph that is not yet represented.
					// We need to update all text as the texture co-ordinates 
					// used by the previously uploaded text will now be wrong.
					//GLGeometryUploader.this.forceUpdate() ;
					continue ;
				}

				final Shape.Attribute[] swivel = shape.getAttribute() ;
				final int verticiesSize = shape.getVerticesSize() ;

				final int GLYPH_POINTS = 4 ;
				final int indexOffset = initialIndexOffset + ( i * GLYPH_POINTS ) ;

				final int size = shape.getIndicesSize() ; 
				for( int j = 0; j < size; j++ )
				{
					indicies[indexIncrement++] = ( short )( indexOffset + shape.getIndex( j ) ) ;
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
								verticies[vertexIncrement++] = temp.x ;
								verticies[vertexIncrement++] = temp.y ;
								verticies[vertexIncrement++] = temp.z ;
								break ;
							}
							case FLOAT :
							{
								// GLDraw colour overrides Shapes colour.
								final Colour colour = draw.getColour() ;
								final Colour col = ( colour != null ) ? colour : shape.getColour( j, k, shapeColour ) ;
								verticies[vertexIncrement++] = getABGR( col ) ;
								break ;
							}
							case VEC2     :
							{
								shape.getVector2( j, k, uv ) ;
								verticies[vertexIncrement++] = uv.x ;
								verticies[vertexIncrement++] = uv.y ;
								break ;
							}
						}
					}
				}

				matrix.translate( glyph.getWidth(), 0.0f, 0.0f ) ;
			}

			indicies[indexIncrement++] = ( short )PRIMITIVE_RESTART_INDEX ;
		}

		indexLength = indexIncrement ;
		final int indiciesLengthBytes = indexIncrement * IBO_VAR_BYTE_SIZE ;
		final int verticiesLengthBytes = vertexIncrement * VBO_VAR_BYTE_SIZE ;

		indexBuffer.put( indicies ) ;
		indexBuffer.position( 0 ) ;

		vertexBuffer.put( verticies ) ;
		vertexBuffer.position( 0 ) ;

		MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
		MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[0] ) ;

		MGL.glBufferData( MGL.GL_ELEMENT_ARRAY_BUFFER, indiciesLengthBytes, indexBuffer, MGL.GL_DYNAMIC_DRAW ) ;
		MGL.glBufferData( MGL.GL_ARRAY_BUFFER, verticiesLengthBytes, vertexBuffer, MGL.GL_DYNAMIC_DRAW ) ;

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

		MGL.glBindBuffer( MGL.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
		MGL.glBindBuffer( MGL.GL_ARRAY_BUFFER, vboID[0] ) ;

		MGL.glUseProgram( glProgram.id[0] ) ;

		final Matrix4 projection = ( isUI() ) ? _camera.getUIProjection() : _camera.getWorldProjection() ;
		final float[] matrix = projection.matrix ;

		MGL.glUniformMatrix4fv( glProgram.inMVPMatrix, 1, true, matrix, 0 ) ;
		if( loadProgramUniforms( glProgram, uniforms ) == false )
		{
			System.out.println( "Failed to load uniforms." ) ;
		}

		GLTextBuffer.bindBuffers( storages ) ;

		GLGeometryBuffer.enableVertexAttributes( attributes ) ;

		GLGeometryBuffer.prepareVertexAttributes( attributes, vertexStrideBytes ) ;
		MGL.glDrawElements( style, indexLength, MGL.GL_UNSIGNED_SHORT, 0 ) ;
		GLGeometryBuffer.disableVertexAttributes( attributes ) ;
	}

	@Override
	public void shutdown()
	{
		MGL.glDeleteBuffers( 1, indexID, 0 ) ;
		MGL.glDeleteBuffers( 1, vboID, 0 ) ;
	}
}
