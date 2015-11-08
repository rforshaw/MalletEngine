package com.linxonline.mallet.renderer.desktop.GL ;

import javax.media.opengl.* ;
import java.util.HashMap ;

import java.nio.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.Model ;

public class GLModelManager
{
	public static int[] genIndexID( final GL3 _gl )
	{
		final int[] id = new int[1] ;

		_gl.glGenBuffers( 1, id, 0 ) ;
		_gl.glBindBuffer( GL3.GL_ELEMENT_ARRAY_BUFFER, id[0] ) ;

		return id ;
	}

	public static int[] genVBOID( final GL3 _gl )
	{
		final int[] id = new int[1] ;

		_gl.glGenBuffers( 1, id, 0 ) ;
		_gl.glBindBuffer( GL3.GL_ARRAY_BUFFER, id[0] ) ;

		return id ;
	}

	public static void unbind( final GLGeometryUploader.GLGeometry _geometry )
	{
		GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;

		if( gl != null )
		{
			gl.glDeleteBuffers( 1, _geometry.vboID, 0 ) ;
			gl.glDeleteBuffers( 1, _geometry.indexID, 0 ) ;
		}

		GLRenderer.getCanvas().getContext().release() ;
	}
}