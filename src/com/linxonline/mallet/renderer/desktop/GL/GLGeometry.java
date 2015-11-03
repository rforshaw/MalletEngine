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

public class GLGeometry implements GeometryInterface
{
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
			return "Index: " + index + " Size: " + size + " Norm: " + normalised + " Offset: " + offset ;
		}
	} ;

	private final int stride ;			// Specifies the byte offset between verticies
	private final VertexAttrib[] attributes ;
	private final int[] index ;
	private final float[] vertex ;

	private final IntBuffer indexBuffer ;
	private final FloatBuffer vertexBuffer ;

	private final int style ; 
	private int vertexInc = 0 ;

	public int indexID ;
	public int vboID ;

	private GLGeometry( final VertexAttrib[] _attributes,
						final Shape.Style _style,
						final int _indexLength,
						final int _vertexLength,
						final int _vertexStride )
	{
		attributes = _attributes ;
		index = new int[_indexLength] ;
		vertex = new float[_vertexLength] ;
		stride = _vertexStride ;
		
		switch( _style )
		{
			case LINES      : style = GL2.GL_LINES ;      break ;
			case LINE_STRIP : style = GL2.GL_LINE_STRIP ; break ;
			case FILL       : style = GL2.GL_TRIANGLES ;  break ;
			default         : style = GL2.GL_LINES ;      break ;
		}
		
		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( getVertexLengthInBytes() ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( getIndexLength() * 4 ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asIntBuffer() ;
	}
	
	private void addIndices( final int[] _indicies )
	{
		for( int i = 0; i < _indicies.length; i++ )
		{
			index[i] = _indicies[i] ;
		}
	}
	
	private void addPoint( final Vector3 _point )
	{
		vertex[vertexInc++] = _point.x ;
		vertex[vertexInc++] = _point.y ;
		vertex[vertexInc++] = _point.z ;
	}

	private void addColour( final MalletColour _colour )
	{
		vertex[vertexInc++] = getABGR( _colour ) ;
	}

	private void addUV( final Vector2 _uv )
	{
		vertex[vertexInc++] = _uv.x ;
		vertex[vertexInc++] = _uv.y ;
	}

	private void addNormal( final Vector3 _normal )
	{
		vertex[vertexInc++] = _normal.x ;
		vertex[vertexInc++] = _normal.y ;
		vertex[vertexInc++] = _normal.z ;
	}

	public FloatBuffer getVertexBuffer()
	{
		vertexBuffer.put( vertex ) ;
		vertexBuffer.position( 0 ) ;
		return vertexBuffer ;
	}

	public IntBuffer getIndexBuffer()
	{
		indexBuffer.put( index ) ;
		indexBuffer.position( 0 ) ;
		return indexBuffer ;
	}

	public VertexAttrib[] getAttributes()
	{
		return attributes ;
	}

	public int getIndexLength()
	{
		return index.length ;
	}

	public int getVertexLengthInBytes()
	{
		return vertex.length * 4 ;
	}

	public int getStyle()
	{
		return style ;
	}

	public int getStride()
	{
		return stride ;
	}

	@Override
	public void destroy()
	{
		//System.out.println( "Removing Geometry.." ) ;
		GLModelManager.unbind( this ) ;
	}

	public static GLGeometry update( final Shape _shape, final GLGeometry _geometry )
	{
		final Shape.Swivel[] swivel = _shape.getSwivel() ;
		final int vertexSize = _shape.getVertexSize() ;

		_geometry.vertexInc = 0 ;

		for( int i = 0; i < vertexSize; i++ )
		{
			for( int j = 0; j < swivel.length; j++ )
			{
				switch( swivel[j] )
				{
					case POINT  : _geometry.addPoint( _shape.getPoint( i, j ) ) ;   break ;
					case COLOUR : _geometry.addColour( _shape.getColour( i, j ) ) ; break ;
					case UV     : _geometry.addUV( _shape.getUV( i, j ) ) ;         break ;
					case NORMAL : _geometry.addNormal( _shape.getNormal( i, j ) ) ; break ;
				}
			}
		}

		return _geometry ;
	}

	public static GLGeometry constructIndex( final int[] _index )
	{
		final GLGeometry geometry = new GLGeometry( null, Shape.Style.FILL, _index.length, 0, 0 ) ;
		geometry.addIndices( _index ) ;

		return geometry ;
	}
	
	public static GLGeometry construct( final Shape _shape )
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

		geometry.addIndices( _shape.indicies ) ;

		final int vertexSize = _shape.getVertexSize() ;
		for( int i = 0; i < vertexSize; i++ )
		{
			for( int j = 0; j < swivel.length; j++ )
			{
				switch( swivel[j] )
				{
					case POINT  : geometry.addPoint( _shape.getPoint( i, j ) ) ;   break ;
					case COLOUR : geometry.addColour( _shape.getColour( i, j ) ) ; break ;
					case UV     : geometry.addUV( _shape.getUV( i, j ) ) ;         break ;
					case NORMAL : geometry.addNormal( _shape.getNormal( i, j ) ) ; break ;
				}
			}
		}

		return geometry ;
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