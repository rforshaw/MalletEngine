package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import javax.media.opengl.* ;

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
	public final int[] id = new int[1] ;

	public int inMVPMatrix = -1 ;			// Uniform used in shader
	public final int[] inUniforms ;
	public final int[] inUniformTextures ;
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

		inUniforms        = ( uniforms != null )        ? new int[uniforms.size()]        : new int[0] ;
		inUniformTextures = ( uniformTextures != null ) ? new int[uniformTextures.size()] : new int[0] ;
		inAttributes      = ( swivel != null )          ? new int[swivel.size()]          : new int[0] ;

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