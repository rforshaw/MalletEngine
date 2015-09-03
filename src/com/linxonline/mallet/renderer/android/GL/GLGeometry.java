package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;
import java.nio.* ;

import android.opengl.GLES11 ;

import com.linxonline.mallet.resources.model.* ;
import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLGeometry implements GeometryInterface
{
	private static final Vector3 DEFAULT_NORMAL = new Vector3() ;
	private static final Vector2 DEFAULT_TEXCOORD = new Vector2() ;
	private static final byte[] BYTE_DEFAULT_COLOUR = { ( byte )255, ( byte )255, ( byte )255, ( byte )255 } ;
	private static final float DEFAULT_COLOUR = ConvertBytes.toFloat( BYTE_DEFAULT_COLOUR, 0, 4 ) ;

	public static final int VERTEX_SIZE = 3 + 1 + 2 + 3 ;
	public static final int STRIDE = VERTEX_SIZE * 4 ; // 4 represents byte size
	public static final int POSITION_OFFSET = 0 ;
	public static final int COLOUR_OFFSET = 3 * 4 ;
	public static final int TEXCOORD_OFFSET = 4 * 4 ;
	public static final int NORMAL_OFFSET = 6 * 4 ;

	private int vertexInc = 0 ; 
	private int indexInc = 0 ;

	public int style = GLES11.GL_LINE_STRIP ;
	public int vboID = 0 ;
	public int indexID = 0 ;
	public short[] index = null ;
	public float[] vertex = null ;
	private final FloatBuffer vertexBuffer ;
	private final ShortBuffer indexBuffer ;

	public GLGeometry( int _indexSize, int _vertexSize )
	{
		index = new short[_indexSize] ;
		vertex = new float[_vertexSize * VERTEX_SIZE] ;

		final int vertexBufferLength = vertex.length * 4 ;			// * 4 represents the bytes for each float
		final ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect( vertexBufferLength ) ;
		vertexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		vertexBuffer = vertexByteBuffer.asFloatBuffer() ;

		final int indexBufferLength = index.length * 2 ; 			// * 2 represents the bytes for each index
		final ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect( indexBufferLength ) ;
		indexByteBuffer.order( ByteOrder.nativeOrder() ) ;
		indexBuffer = indexByteBuffer.asShortBuffer() ;
	}

	public void setStyle( final int _style )
	{
		style = _style ;
	}

	public void addIndices( final int _index )
	{
		if( indexInc >= index.length )
		{
			Logger.println( "Index access out of bounds - GLGeometry.", Logger.Verbosity.MAJOR ) ;
			return ;
		}

		index[indexInc++] = ( short )_index ;
	}

	public void addVertex( final Vector3 _position )
	{
		addVertex( _position, DEFAULT_COLOUR, DEFAULT_NORMAL, DEFAULT_TEXCOORD ) ;
	}

	public void addVertex( final Vector3 _position, final float _colour )
	{
		addVertex( _position, _colour, DEFAULT_NORMAL, DEFAULT_TEXCOORD ) ;
	}

	public void addVertex( final Vector3 _position, final float _colour, final Vector2 _texCoord )
	{
		addVertex( _position, _colour, DEFAULT_NORMAL, _texCoord ) ;
	}

	public void addVertex( final Vector3 _position, final Vector2 _texCoord )
	{
		addVertex( _position, DEFAULT_COLOUR, DEFAULT_NORMAL, _texCoord ) ;
	}

	public void addVertex( final Vector3 _position, final Vector3 _normal, final Vector2 _texCoord )
	{
		addVertex( _position, DEFAULT_COLOUR, _normal, _texCoord ) ;
	}

	public void addVertex( final Vector3 _position, final float _colour, final Vector3 _normal, final Vector2 _texCoord )
	{
		vertex[vertexInc] = _position.x ;
		vertex[vertexInc + 1] = _position.y ;
		vertex[vertexInc + 2] = _position.z ;

		vertex[vertexInc + 3] = ( float )_colour ;

		vertex[vertexInc + 4] = _texCoord.x ;
		vertex[vertexInc + 5] = _texCoord.y ;

		vertex[vertexInc + 6]  = _normal.x ;
		vertex[vertexInc + 7]  = _normal.y ;
		vertex[vertexInc + 8] = _normal.z ;
		vertexInc += VERTEX_SIZE ;
	}

	public void updateVertex( final int _index, final Vector3 _position, final float _colour, final Vector3 _normal, final Vector2 _texCoord )
	{
		updatePosition( _index, _position.x, _position.y, _position.z ) ;
		updateColour( _index, _colour ) ;
		updateTexCoord( _index, _texCoord.x, _texCoord.y ) ;
		updateNormal( _index, _normal.x, _normal.y, _normal.z ) ;
	}

	public void updatePosition( final int _index, final float _x, final float _y, final float _z )
	{
		final int i = _index * VERTEX_SIZE ;
		vertex[i] = _x ;
		vertex[i + 1] = _y ;
		vertex[i + 2] = _z ;
	}

	public void updateColour( final int _index, final float _colour )
	{
		final int i = _index * VERTEX_SIZE ;
		vertex[i + 3] = ( float )_colour ;
	}

	public void updateTexCoord( final int _index, final float _x, final float _y )
	{
		final int i = _index * VERTEX_SIZE ;
		vertex[i + 4] = _x ;
		vertex[i + 5] = _y ;
	}

	public void updateNormal( final int _index, final float _x, final float _y, final float _z )
	{
		final int i = _index * VERTEX_SIZE ;
		vertex[i + 6]  = _x ;
		vertex[i + 7]  = _y ;
		vertex[i + 8] = _z ;
	}

	public int getIndexSize()
	{
		return index.length ;
	}

	public int getVertexSize()
	{
		return vertex.length / VERTEX_SIZE ;
	}

	public FloatBuffer getVertexBuffer()
	{
		vertexBuffer.put( vertex ) ;
		vertexBuffer.position( 0 ) ;
		return vertexBuffer ;
	}

	public ShortBuffer getIndexBuffer()
	{
		indexBuffer.put( index ) ;
		indexBuffer.position( 0 ) ;
		return indexBuffer ;
	}

	@Override
	public void destroy()
	{
		//System.out.println( "Removing Geometry.." ) ;
		GLModelManager.unbind( this ) ;
	}
}