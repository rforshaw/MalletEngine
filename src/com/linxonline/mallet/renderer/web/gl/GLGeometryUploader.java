package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;
import java.nio.* ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;
import org.teavm.jso.typedarrays.Uint8Array ;

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
	private final static int INDEX_BYTE_SIZE = 2 ;
	private final static int FLOAT_BYTE_SIZE = 4 ;

	private final Int16Array indicies ;
	private final Float32Array verticies ;

	public GLGeometryUploader( final int _indexSize, final int _vboSize )
	{
		indicies = Int16Array.create( _indexSize ) ;
		verticies = Float32Array.create( _vboSize ) ;
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
					attributes[i] = new VertexAttrib( GLProgramManager.VERTEX_ARRAY, 3, GL3.FLOAT, false, offset ) ;
					offset += 3 * FLOAT_BYTE_SIZE ;
					break ;
				}
				case COLOUR :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.COLOUR_ARRAY, 4, GL3.UNSIGNED_BYTE, true, offset ) ;
					offset += 1 * FLOAT_BYTE_SIZE ;
					break ;
				}
				case UV     :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.TEXTURE_COORD_ARRAY0, 2, GL3.FLOAT, false, offset ) ;
					offset += 2 * FLOAT_BYTE_SIZE ;
					break ;
				}
				case NORMAL  :
				{
					attributes[i] = new VertexAttrib( GLProgramManager.NORMAL_ARRAY, 3, GL3.FLOAT, false, offset ) ;
					offset += 3 * FLOAT_BYTE_SIZE ;
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

	public synchronized void uploadIndex( final WebGLRenderingContext _gl, final GLGeometry _handler, final Shape _shape )
	{
		final int[] index = _shape.indicies ;
		final int indiciesLength = indicies.getLength() ;

		int j = 0 ;
		int offset = 0 ;

		for( int i = 0; i < index.length; i++ )
		{
			indicies.set( j++, ( short )index[i] ) ;
			if( j >= indiciesLength )
			{
				final int indexBufferLengthInBytes = indiciesLength * INDEX_BYTE_SIZE ;
				_gl.bufferSubData( GL3.ELEMENT_ARRAY_BUFFER, offset, indicies ) ;

				j = 0 ;
				offset += indexBufferLengthInBytes ;
			}
		}

		if( j > 0 )
		{
			final int indexBufferLengthInBytes = j * INDEX_BYTE_SIZE ;
			_gl.bufferSubData( GL3.ELEMENT_ARRAY_BUFFER, offset, Int16Array.create( indicies.getBuffer(), 0, j ) ) ;
		}
	}

	public synchronized void uploadVBO( final WebGLRenderingContext _gl, final GLGeometry _handler, final Shape _shape )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = calculateVertexSize( swivel ) ;
		final int verticiesSize = _shape.getVertexSize() ;

		final Uint8Array byteVersion = Uint8Array.create( verticies.getBuffer() ) ;

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
						verticies.set( inc++, point.x ) ;
						verticies.set( inc++, point.y ) ;
						verticies.set( inc++, point.z ) ;
						break ;
					}
					case COLOUR :
					{
						final MalletColour colour = _shape.getColour( i, j ) ;
						setColour( inc++, colour, byteVersion ) ;
						//verticies.set( inc++, getABGR( colour ) ) ;
						break ;
					}
					case UV     :
					{
						final Vector2 uv = _shape.getUV( i, j ) ;
						verticies.set( inc++, uv.x ) ;
						verticies.set( inc++, uv.y ) ;
						break ;
					}
				}
			}

			if( ( inc + vertexSize ) >= verticies.getLength() )
			{
				final int vertexLengthInBytes = inc * FLOAT_BYTE_SIZE ;
				_gl.bufferSubData( GL3.ARRAY_BUFFER, offset, verticies ) ;

				inc = 0 ;
				offset += vertexLengthInBytes ;
			}
		}
		
		if( inc > 0 )
		{
			final int vertexLengthInBytes = inc * FLOAT_BYTE_SIZE ;
			_gl.bufferSubData( GL3.ARRAY_BUFFER, offset, Float32Array.create( verticies.getBuffer(), 0, inc ) ) ;
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

		public final WebGLBuffer[] indexID = new WebGLBuffer[1] ;
		public final WebGLBuffer[] vboID = new WebGLBuffer[1] ;

		public GLGeometry( final VertexAttrib[] _attributes,
						   final Shape.Style _style,
						   final int _indexLength,
						   final int _vertexLength,
						   final int _vertexStride )
		{
			indexLength  = _indexLength ;
			vertexLength = _vertexLength ;
			vertexStride = _vertexStride ;

			final WebGLRenderingContext gl = GLRenderer.getContext() ;

			indexID[0] = GLModelManager.genIndexID( gl ) ;
			vboID[0] = GLModelManager.genVBOID( gl ) ;

			final int indexBufferLengthInBytes = indexLength * INDEX_BYTE_SIZE ;
			final int vertexLengthInBytes = vertexLength * FLOAT_BYTE_SIZE ;

			gl.bindBuffer( GL3.ELEMENT_ARRAY_BUFFER, indexID[0] ) ;
			gl.bufferData( GL3.ELEMENT_ARRAY_BUFFER, indexBufferLengthInBytes, GL3.STATIC_DRAW ) ;

			gl.bindBuffer( GL3.ARRAY_BUFFER, vboID[0] ) ;
			gl.bufferData( GL3.ARRAY_BUFFER, vertexLengthInBytes, GL3.DYNAMIC_DRAW ) ;

			attributes = _attributes ;
			stride = _vertexStride ;

			switch( _style )
			{
				case LINES      : style = GL3.LINES ;      break ;
				case LINE_STRIP : style = GL3.LINE_STRIP ; break ;
				case FILL       : style = GL3.TRIANGLES ;  break ;
				default         : style = GL3.LINES ;      break ;
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

	private static void setColour( final int _fIndex, final MalletColour _colour, final Uint8Array _byteStream )
	{
		int byteIndex = _fIndex * 4 ;
		_byteStream.set( byteIndex++, ( short )_colour.colours[MalletColour.RED] ) ;
		_byteStream.set( byteIndex++, ( short )_colour.colours[MalletColour.GREEN] ) ;
		_byteStream.set( byteIndex++, ( short )_colour.colours[MalletColour.BLUE] ) ;
		_byteStream.set( byteIndex, ( short )_colour.colours[MalletColour.ALPHA] ) ;
	}
}