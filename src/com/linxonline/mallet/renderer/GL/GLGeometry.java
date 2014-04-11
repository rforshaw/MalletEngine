package com.linxonline.mallet.renderer.GL ;

import java.util.ArrayList ;

import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLGeometry implements GeometryInterface
{
	public static final int VERTEX_SIZE = 3 + 4 + 2 + 3 ;
	public static final int STRIDE = VERTEX_SIZE * 4 ; // 4 represents byte size
	public static final int POSITION_OFFSET = 0 ;
	public static final int COLOUR_OFFSET = 3 * 4 ;
	public static final int TEXCOORD_OFFSET = 7 * 4 ;
	public static final int NORMAL_OFFSET = 9 * 4 ;

	private int vertexInc = 0 ; 
	private int indexInc = 0 ;

	public int vboID = 0 ;
	public int indexID = 0 ;
	public int[] index = null ;
	public float[] vertex = null ;

	public GLGeometry( int _indexSize, int _vertexSize )
	{
		initIndexBufferSize( _indexSize ) ;
		initVertexBufferSize( _vertexSize ) ;
	}

	public void initIndexBufferSize( final int _size )
	{
		index = new int[_size] ;
	}

	public void initVertexBufferSize( final int _size )
	{
		vertex = new float[_size * VERTEX_SIZE] ;
	}

	public void addIndices( final int _index )
	{
		if( indexInc >= index.length )
		{
			System.out.println( "Index Array Overflow." ) ;
			return ;
		}

		index[indexInc++] = _index ;
	}

	public void addVertex( final Vector3 _position, 
						   final Vector3 _normal,
						   final Vector2 _texCoord )
	{
		vertex[vertexInc] = _position.x ;
		vertex[vertexInc + 1] = _position.y ;
		vertex[vertexInc + 2] = _position.z ;

		vertex[vertexInc + 3] = 1.0f ;			// red
		vertex[vertexInc + 4] = 1.0f ;			// green
		vertex[vertexInc + 5] = 1.0f ;			// blue
		vertex[vertexInc + 6] = 1.0f ;			// alpha
	
		vertex[vertexInc + 7] = _texCoord.x ;
		vertex[vertexInc + 8] = _texCoord.y ;
		
		vertex[vertexInc + 9]  = _normal.x ;
		vertex[vertexInc + 10]  = _normal.y ;
		vertex[vertexInc + 11] = _normal.z ;

		vertexInc += VERTEX_SIZE ;
	}

	public void destroy()
	{
		GLModelManager.unbind( this ) ;
	}
}