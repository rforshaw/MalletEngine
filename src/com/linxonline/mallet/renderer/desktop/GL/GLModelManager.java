package com.linxonline.mallet.renderer.desktop.GL ;

import javax.media.opengl.* ;
import java.util.HashMap ;

import java.nio.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.Model ;

public class GLModelManager
{
	public static void bind( final GLGeometry _geometry )
	{
		final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;
		if( gl == null )
		{
			System.out.println( "GL context doesn't exist" ) ;
			return ;
		}

		bindVBO( gl, _geometry ) ;
		bindIndex( gl, _geometry ) ;
	}

	public static void unbind( final GLGeometry _geometry )
	{
		GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;
		if( gl != null )
		{
			final int[] id = new int[1] ;
			id[0] = _geometry.vboID ;
			gl.glDeleteBuffers( 1, id, 0 ) ;

			id[0] = _geometry.indexID ;
			gl.glDeleteBuffers( 1, id, 0 ) ;
		}
		GLRenderer.getCanvas().getContext().release() ;
	}

	public static int glGenBuffers( GL3 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenBuffers( 1, id, 0 ) ;
		return id[0] ;
	}

	public static void bindVBO( final GL3 _gl, final GLGeometry _geometry )
	{
		final int vboID = GLModelManager.glGenBuffers( _gl ) ;
		_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, vboID ) ;
		_geometry.vboID = vboID ;

		_gl.glBufferData( GL3.GL_ARRAY_BUFFER, _geometry.getVertexLengthInBytes(), _geometry.getVertexBuffer(), GL3.GL_DYNAMIC_DRAW ) ;
	}

	public static void updateVBO( final GL3 _gl, final GLGeometry _geometry )
	{
		_gl.glBufferSubData( GL3.GL_ARRAY_BUFFER,	
							 0,
							 _geometry.getVertexLengthInBytes(),
							 _geometry.getVertexBuffer() ) ;
	}

	public static void bindIndex( final GL3 _gl, final GLGeometry _geometry )
	{
		final int indexID = GLModelManager.glGenBuffers( _gl ) ;
		_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
		_geometry.indexID = indexID ;

		final int indexBufferLength = _geometry.getIndexLength() * 4 ;		// * 4 represents the bytes for each integer
		_gl.glBufferData( GL3.GL_ELEMENT_ARRAY_BUFFER, indexBufferLength, _geometry.getIndexBuffer(), GL3.GL_STATIC_DRAW ) ;
	}
}