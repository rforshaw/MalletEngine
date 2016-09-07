package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;

import org.teavm.jso.webgl.WebGLProgram ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.webgl.WebGLUniformLocation ;

import com.linxonline.mallet.resources.Resource ;
import com.linxonline.mallet.util.Tuple ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public class GLProgram extends Resource
{
	public enum DataType
	{
		BOOL,
		INT,
		UINT,
		FLOAT,
		DOUBLE,
		BVEC2,
		BVEC3,
		BVEC4,
		IVEC2,
		IVEC3,
		IVEC4,
		UVEC2,
		UVEC3,
		UVEC4,
		VEC2,
		VEC3,
		VEC4,
		DVEC2,
		DVEC3,
		DVEC4,
		MAT4,
		SAMPLER2D
	}

	public final String name ;
	public final WebGLProgram[] id = new WebGLProgram[1] ;

	public WebGLUniformLocation inMVPMatrix = null ;			// Uniform used in shader
	public final WebGLUniformLocation[] inUniforms ;
	public final WebGLUniformLocation[] inUniformTextures ;
	public final int[] inAttributes ;

	public final ArrayList<GLShader> shaders ;
	public final ArrayList<Tuple<String, DataType>> uniforms ;
	public final ArrayList<String> uniformTextures ;
	public final ArrayList<String> swivel ;

	public GLProgram( final String _name,
					  final ArrayList<GLShader> _shaders,
					  final ArrayList<Tuple<String, DataType>> _uniforms,
					  final ArrayList<String> _uniformTextures,
					  final ArrayList<String> _swivel )
	{
		name = _name ;

		shaders         = _shaders ;
		uniforms        = _uniforms ;
		uniformTextures = _uniformTextures ;
		swivel          = _swivel ;

		inUniforms        = ( uniforms != null )        ? new WebGLUniformLocation[uniforms.size()]        : new WebGLUniformLocation[0] ;
		inUniformTextures = ( uniformTextures != null ) ? new WebGLUniformLocation[uniformTextures.size()] : new WebGLUniformLocation[0] ;
		inAttributes      = ( swivel != null )          ? new int[swivel.size()]                           : new int[0] ;

		final int length = inAttributes.length ;
		for( int i = 0; i < length; i++ )
		{
			inAttributes[i] = i ;
		}
	}

	@Override
	public void destroy() {}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}
}