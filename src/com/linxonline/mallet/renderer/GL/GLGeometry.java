package com.linxonline.mallet.renderer.GL ;

import java.util.ArrayList ;

import javax.media.opengl.* ;

import com.linxonline.mallet.resources.* ;
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

	public int style = GL2.GL_LINE_STRIP ;
	public int vboID = 0 ;
	public int indexID = 0 ;
	public int[] index = null ;
	public float[] vertex = null ;

	public GLGeometry( int _indexSize, int _vertexSize )
	{
		index = new int[_indexSize] ;
		vertex = new float[_vertexSize * VERTEX_SIZE] ;
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

		index[indexInc++] = _index ;
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

	public void destroy()
	{
		System.out.println( "Removing Geometry.." ) ;
		GLModelManager.unbind( this ) ;
	}
}