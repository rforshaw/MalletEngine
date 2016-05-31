package com.linxonline.mallet.renderer.android.GL ;

import java.util.HashMap ;
import android.opengl.GLES20 ;

import java.nio.* ;

public class GLModelManager
{
	public static int[] genIndexID()
	{
		final int[] id = new int[1] ;
		GLES20.glGenBuffers( 1, id, 0 ) ;

		return id ;
	}

	public static int[] genVBOID()
	{
		final int[] id = new int[1] ;
		GLES20.glGenBuffers( 1, id, 0 ) ;

		return id ;
	}

	public static void unbind( final GLGeometryUploader.GLGeometry _geometry )
	{
		GLES20.glDeleteBuffers( 1, _geometry.vboID, 0 ) ;
		GLES20.glDeleteBuffers( 1, _geometry.indexID , 0 ) ;
	}
}