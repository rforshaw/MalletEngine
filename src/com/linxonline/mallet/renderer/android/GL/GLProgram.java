package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

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
	public int[] inTex = null ;

	public final ArrayList<GLShader> shaders ;

	public GLProgram( final String _name, final ArrayList<GLShader> _shaders )
	{
		name = _name ;
		shaders = _shaders ;
	}

	public void copyTextures( final int[] _inTex, final int _length )
	{
		inTex = new int[_length] ;
		System.arraycopy( _inTex, 0, inTex, 0, _length ) ;
	}

	@Override
	public void destroy()
	{
		GLProgramManager.deleteProgram( this ) ;
		if( shaders != null )
		{
			shaders.clear() ;
		}
	}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}
}