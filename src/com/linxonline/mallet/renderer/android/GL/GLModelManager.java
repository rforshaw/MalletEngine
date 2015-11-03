package com.linxonline.mallet.renderer.android.GL ;

import java.util.HashMap ;
import android.opengl.GLES20 ;

import java.nio.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.Model ;

public class GLModelManager
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
		GLES20.glDeleteBuffers( 1, id, 0 ) ;

		id[0] = _geometry.indexID ;
		GLES20.glDeleteBuffers( 1, id, 0 ) ;
	}

	public static int glGenBuffers()
	{
		final int[] id = new int[1] ;
		GLES20.glGenBuffers( 1, id, 0 ) ;
		return id[0] ;
	}

	public static void bindVBO( final GLGeometry _geometry )
	{
		final int vboID = GLModelManager.glGenBuffers() ;
		GLES20.glBindBuffer( GLES20.GL_ARRAY_BUFFER, vboID ) ;
		_geometry.vboID = vboID ;

		GLES20.glBufferData( GLES20.GL_ARRAY_BUFFER,
							 _geometry.getVertexLengthInBytes(),
							 _geometry.getVertexBuffer(),
							 GLES20.GL_DYNAMIC_DRAW ) ;
	}

	public static void updateVBO( final GLGeometry _geometry )
	{
		GLES20.glBufferSubData( GLES20.GL_ARRAY_BUFFER,
								0,
								_geometry.getVertexLengthInBytes(),
								_geometry.getVertexBuffer() ) ;
	}

	public static void bindIndex(final GLGeometry _geometry )
	{
		final int indexID = GLModelManager.glGenBuffers() ;
		GLES20.glBindBuffer( GLES20.GL_ELEMENT_ARRAY_BUFFER, indexID ) ;
		_geometry.indexID = indexID ;

		final int indexBufferLength = _geometry.getIndexLength() * 2 ;		// * 2 represents the bytes for each short
		GLES20.glBufferData( GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferLength, _geometry.getIndexBuffer(), GLES20.GL_STATIC_DRAW ) ;
	}
}