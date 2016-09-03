package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import javax.media.opengl.* ;

import com.linxonline.mallet.resources.Resource ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public class GLProgram extends Resource
{
	public final String name ;
	public final int[] id = new int[1] ;

	public int inMVPMatrix = -1 ;			// Uniform used in shader
	public final int[] inUniforms ;
	public final int[] inUniformTextures ;
	public final int[] inAttributes ;

	public int[] inTex = null ;

	public final ArrayList<GLShader> shaders ;
	public final ArrayList<String> uniforms ;
	public final ArrayList<String> uniformTextures ;
	public final ArrayList<String> swivel ;

	public GLProgram( final String _name,
					  final ArrayList<GLShader> _shaders,
					  final ArrayList<String> _uniforms,
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

	public void copyTextures( final int[] _inTex, final int _length )
	{
		inTex = new int[_length] ;
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
