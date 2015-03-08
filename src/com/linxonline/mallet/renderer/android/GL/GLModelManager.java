package com.linxonline.mallet.renderer.android.GL ;

import java.util.HashMap ;
import android.opengl.GLES11 ;

import java.nio.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.Model ;

public class GLModelManager extends AbstractManager<Model>
{
	public static void bind( final GLGeometry _geometry )
	{
		bindVBO( _geometry ) ;
		bindIndex( _geometry ) ;
	}

	public static void unbind( final GLGeometry _geometry )
	{
		final int[] id = new int[1] ;
		id[0] = _geometry.vboID ;
		GLES11.glDeleteBuffers( 1, id, 0 ) ;

		id[0] = _geometry.indexID ;
		GLES11.glDeleteBuffers( 1, id, 0 ) ;
	}

	public static int glGenBuffers()
	{
		final int[] id = new int[1] ;
		GLES11.glGenBuffers( 1, id, 0 ) ;
		return id[0] ;
	}

	public static void bindVBO( final GLGeometry _geometry )
	{
		final int vboID = GLModelManager.glGenBuffers() ;
		GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, vboID ) ;
		_geometry.vboID = vboID ;

		GLModelManager.updateVBO( _geometry ) ;
	}

	public static void updateVBO( final GLGeometry _geometry )
	{
		final int vertexBufferLength = _geometry.vertex.length * 4 ;		// * 4 represents the bytes for each float
		GLES11.glBufferData( GLES11.GL_ARRAY_BUFFER, vertexBufferLength, _geometry.getVertexBuffer(), GLES11.GL_DYNAMIC_DRAW ) ;
	}

	public static void bindIndex(final GLGeometry _geometry )
	{
		final int indexID = GLModelManager.glGenBuffers() ;
		GLES11.glBindBuffer( GLES11.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
		_geometry.indexID = indexID ;

		final int indexBufferLength = _geometry.index.length * 2 ;		// * 2 represents the bytes for each short
		GLES11.glBufferData( GLES11.GL_ELEMENT_ARRAY_BUFFER, indexBufferLength, _geometry.getIndexBuffer(), GLES11.GL_STATIC_DRAW ) ;
	}
}