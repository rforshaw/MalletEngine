package com.linxonline.mallet.renderer.desktop.GL ;

public final class GLModelManager
{
	private GLModelManager() {}

	public static int[] genIndexID()
	{
		final int[] id = new int[1] ;
		MGL.glGenBuffers( 1, id, 0 ) ;

		return id ;
	}

	public static int[] genVBOID()
	{
		final int[] id = new int[1] ;
		MGL.glGenBuffers( 1, id, 0 ) ;

		return id ;
	}

	public static void unbind( final GLGeometryUploader.GLGeometry _geometry )
	{
		MGL.glDeleteBuffers( 1, _geometry.vboID, 0 ) ;		//GLRenderer.handleError( "Delete VBO", gl ) ;
		MGL.glDeleteBuffers( 1, _geometry.indexID, 0 ) ;		//GLRenderer.handleError( "Delete Index", gl ) ;
	}
}
