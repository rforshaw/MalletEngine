package com.linxonline.mallet.renderer.web.gl ;

import java.util.HashMap ;
import java.nio.* ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLBuffer ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.resources.model.Model ;

public class GLModelManager
{
	public static WebGLBuffer genIndexID( final WebGLRenderingContext _gl )
	{
		return _gl.createBuffer() ;
	}

	public static WebGLBuffer genVBOID( final WebGLRenderingContext _gl )
	{
		return _gl.createBuffer() ;
	}

	public static void unbind( final GLGeometryUploader.GLGeometry _geometry )
	{
		final WebGLRenderingContext gl = GLRenderer.getContext() ;
		if( gl != null )
		{
			gl.deleteBuffer( _geometry.vboID[0] ) ;
			gl.deleteBuffer( _geometry.indexID[0] ) ;
		}
	}
}