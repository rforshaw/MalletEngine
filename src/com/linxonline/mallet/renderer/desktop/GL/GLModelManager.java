package com.linxonline.mallet.renderer.desktop.GL ;

import com.jogamp.opengl.* ;

import java.nio.* ;

public final class GLModelManager
{
	private GLModelManager() {}

	public static int[] genIndexID( final GL3 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenBuffers( 1, id, 0 ) ;

		return id ;
	}

	public static int[] genVBOID( final GL3 _gl )
	{
		final int[] id = new int[1] ;
		_gl.glGenBuffers( 1, id, 0 ) ;

		return id ;
	}

	public static void unbind( final GLGeometryUploader.GLGeometry _geometry )
	{
		//GLRenderer.getCanvas().getContext().makeCurrent() ;						// Get GL's Attention
		final GL3 gl = GLRenderer.getGL() ;
		if( gl != null )
		{
			gl.glDeleteBuffers( 1, _geometry.vboID, 0 ) ;		//GLRenderer.handleError( "Delete VBO", gl ) ;
			gl.glDeleteBuffers( 1, _geometry.indexID, 0 ) ;		//GLRenderer.handleError( "Delete Index", gl ) ;
		}

		//GLRenderer.getCanvas().getContext().release() ;
	}
}
