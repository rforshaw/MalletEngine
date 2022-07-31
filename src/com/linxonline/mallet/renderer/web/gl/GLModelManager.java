package com.linxonline.mallet.renderer.web.gl ;

import org.teavm.jso.webgl.WebGLBuffer ;

public class GLModelManager
{
	private GLModelManager() {}

	public static WebGLBuffer[] genIndexID()
	{
		final WebGLBuffer[] id = new WebGLBuffer[1] ;
		id[0] = MGL.createBuffer() ;

		return id ;
	}

	public static WebGLBuffer[] genVBOID()
	{
		final WebGLBuffer[] id = new WebGLBuffer[1] ;
		id[0] = MGL.createBuffer() ;

		return id ;
	}
}
