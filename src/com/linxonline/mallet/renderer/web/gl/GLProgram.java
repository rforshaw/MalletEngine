package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;

import org.teavm.jso.webgl.WebGLProgram ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.webgl.WebGLUniformLocation ;

import com.linxonline.mallet.resources.Resource ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public class GLProgram extends Resource
{
	public final String name ;
	public final WebGLProgram[] id = new WebGLProgram[1] ;

	public WebGLUniformLocation inMVPMatrix = null ;			// Uniform used in shader
	public WebGLUniformLocation[] inTex = null ;

	public final ArrayList<GLShader> shaders ;

	public GLProgram( final String _name, final ArrayList<GLShader> _shaders )
	{
		name = _name ;
		shaders = _shaders ;
	}

	public void copyTextures( final WebGLUniformLocation[] _inTex, final int _length )
	{
		inTex = new WebGLUniformLocation[_length] ;
		System.arraycopy( _inTex, 0, inTex, 0, _length ) ;
	}

	@Override
	public void destroy() {}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}
}