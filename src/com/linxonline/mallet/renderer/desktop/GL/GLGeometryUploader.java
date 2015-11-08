package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import java.nio.* ;

import javax.media.opengl.* ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Shape.Swivel ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLGeometryUploader
{
	private final int[] indicies ;
	private final float[] verticies ;

	private final IntBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize )
	{
		indicies = new int[_indexSize] ;
		verticies = new float[_vboSize] ;

		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( _vboSize * 4 ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( _indexSize * 4 ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;
	}

	public static synchronized GLGeometry construct( final Shape _shape )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final VertexAttrib[] attributes = new VertexAttrib[swivel.length] ;

		int offset = 0 ;
		for( int i = 0; i < swivel.length; i++ )
		{
			switch( swivel[i] )
			{
				case POINT  :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.VERTEX_ARRAY, 3, GL3.GL_FLOAT, false, offset ) ;
					offset += 3 * 4 ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.COLOUR_ARRAY, 4, GL3.GL_UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * 4 ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.TEXTURE_COORD_ARRAY0, 2, GL3.GL_FLOAT, false, offset ) ;
					offset += 2 * 4 ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.NORMAL_ARRAY, 3, GL3.GL_FLOAT, false, offset ) ;
					offset += 3 * 4 ;
					break ;
				}
			}
		}

		final int vertexStride = offset ;
		final int vertexLength = _shape.getVertexSize() * ( vertexStride / 4 ) ;
		final GLGeometry geometry = new GLGeometry( attributes,
													_shape.style,
													_shape.indicies.length,
													vertexLength,
													vertexStride ) ;

		return geometry ;
	}

	public synchronized void uploadIndex( final GL3 _gl, final GLGeometry _handler, final Shape _shape )
	{
		final int[] index = _shape.indicies ;

		int j = 0 ;
		int offset = 0 ;
		for( int i = 0; i < index.length; i++ )
		{
			indicies[j++] = index[i] ;
			if( j >= indicies.length )
			{
				indexBuffer.put( indicies ) ;
				indexBuffer.position( 0 ) ;

				final int indexBufferLengthInBytes = indicies.length * 4 ;
				_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, offset, indexBufferLengthInBytes, indexBuffer ) ;

				j = 0 ;
				offset += indexBufferLengthInBytes ;
			}
		}

		if( j > 0 )
		{
			indexBuffer.put( indicies ) ;
			indexBuffer.position( 0 ) ;

			final int indexBufferLengthInBytes = j * 4 ;
			_gl.glBufferSubData( GL3.GL_ELEMENT_ARRAY_BUFFER, offset, indexBufferLengthInBytes, indexBuffer ) ;
		}
	}

	public synchronized void uploadVBO( final GL3 _gl, final GLGeometry _handler, final Shape _shape )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = calculateVertexSize( swivel ) ;
		final int verticiesSize = _shape.getVertexSize() ;

		int inc = 0 ;
		int offset = 0 ;

		for( int i = 0; i < verticiesSize; i++ )
		{
			for( int j = 0; j < swivel.length; j++ )
			{
				switch( swivel[j] )
				{
					case NORMAL :
					case POINT  :
					{
						final Vector3 point = _shape.getPoint( i, j ) ;
						verticies[inc++] = point.x ;
						verticies[inc++] = point.y ;
						verticies[inc++] = point.z ;
						break ;
					}
					case COLOUR :
					{
						final MalletColour colour = _shape.getColour( i, j ) ;
						verticies[inc++] = getABGR( colour ) ;
						break ;
					}
					case UV     :
					{
						final Vector2 uv = _shape.getUV( i, j ) ;
						verticies[inc++] = uv.x ;
						verticies[inc++] = uv.y ;
						break ;
					}
				}
			}

			if( ( inc + vertexSize ) >= verticies.length )
			{
				vertexBuffer.put( verticies ) ;
				vertexBuffer.position( 0 ) ;

				final int vertexLengthInBytes = inc * 4 ;
				_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, offset, vertexLengthInBytes, vertexBuffer ) ;

				inc = 0 ;
				offset += vertexLengthInBytes ;
			}
		}
		
		if( inc > 0 )
		{
			vertexBuffer.put( verticies ) ;
			vertexBuffer.position( 0 ) ;

			final int vertexLengthInBytes = inc * 4 ;
			_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER, offset, vertexLengthInBytes, vertexBuffer ) ;
		}
	}

	private int calculateVertexSize( final Shape.Swivel[] _swivel )
	{
		int size = 0 ;
		for( int j = 0; j < _swivel.length; j++ )
		{
			switch( _swivel[j] )
			{
				case POINT  : size += 3 ; break ;
				case COLOUR : size += 1 ; break ;
				case UV     : size += 2 ; break ;
				case NORMAL : size += 3 ; break ;
			}
		}

		return size ;
	}
	
	public static class VertexAttrib
	{
		public int index ;				// Specifies the index of the generic vertex attribute to be modified 
		public int size ;				// Specifies the number of components per generic vertex attribute
		public int type ;				// Specifies the data type ;
		public boolean normalised ;		// Specifies whether fixed-point data values should be normalized
		public int offset ;				// Specifies the offset for the first component

		public VertexAttrib( final int _index, final int _size, final int _type, final boolean _normalised, final int _offset )
		{
			index = _index ;
			size = _size ;
			type = _type ;
			normalised = _normalised ;
			offset = _offset ; 
		}

		public String toString()
		{
			final StringBuilder buffer = new StringBuilder() ;
			buffer.append( "Index: " ) ;
			buffer.append( index ) ;
			buffer.append( " Size: " ) ;
			buffer.append( size ) ;
			buffer.append( " Norm: " ) ;
			buffer.append( normalised ) ;
			buffer.append( " Offset: " ) ;
			buffer.append( offset ) ;

			return buffer.toString() ;
		}
	} ;

	public static class GLGeometry implements GeometryInterface
	{
		private final VertexAttrib[] attributes ;
		private final int stride ;			// Specifies the byte offset between verticies
		private final int style ;

		private final int indexLength ;
		private final int vertexLength ;
		private final int vertexStride ;

		public final int[] indexID ;
		public final int[] vboID ;

		public GLGeometry( final VertexAttrib[] _attributes,
						   final Shape.Style _style,
						   final int _indexLength,
						   final int _vertexLength,
						   final int _vertexStride )
		{
			indexLength  = _indexLength ;
			vertexLength = _vertexLength ;
			vertexStride = _vertexStride ;

			GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
			final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;

			indexID = GLModelManager.genIndexID( gl ) ;
			vboID = GLModelManager.genVBOID( gl ) ;

			final int indexBufferLengthInBytes = indexLength * 4 ;
			final int vertexLengthInBytes = vertexLength * 4 ;

			gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
			gl.glBufferData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferLengthInBytes, null, GL3.GL_STATIC_DRAW ) ;

			gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vboID[0] ) ;
			gl.glBufferData( GL3.GL_ARRAY_BUFFER, vertexLengthInBytes, null, GL3.GL_DYNAMIC_DRAW ) ;

			GLRenderer.getCanvas().getContext().release() ;

			attributes = _attributes ;
			stride = _vertexStride ;
			switch( _style )
			{
				case LINES      : style = GL2.GL_LINES ;      break ;
				case LINE_STRIP : style = GL2.GL_LINE_STRIP ; break ;
				case FILL       : style = GL2.GL_TRIANGLES ;  break ;
				default         : style = GL2.GL_LINES ;      break ;
			}
		}

		public int getIndexLength()
		{
			return indexLength ;
		}

		public int getStyle()
		{
			return style ;
		}

		public int getStride()
		{
			return stride ;
		}

		public VertexAttrib[] getAttributes()
		{
			return attributes ;
		}

		public void destroy()
		{
			GLModelManager.unbind( this ) ;
		}
	}

	private static float getABGR( final MalletColour _colour )
	{
		final byte[] colour = new byte[4] ;
		colour[0] = _colour.colours[MalletColour.ALPHA] ;
		colour[1] = _colour.colours[MalletColour.BLUE] ;
		colour[2] = _colour.colours[MalletColour.GREEN] ;
		colour[3] = _colour.colours[MalletColour.RED] ;

		return ConvertBytes.toFloat( colour, 0, 4 ) ;
	}
}