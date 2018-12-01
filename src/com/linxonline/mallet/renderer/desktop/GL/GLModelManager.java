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
}
