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
	public final ArrayList<GLShader> shaders ;

	public GLProgram( final String _name, final ArrayList<GLShader> _shaders )
	{
		name = _name ;
		shaders = _shaders ;
	}

	@Override
	public void destroy() {}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}
}