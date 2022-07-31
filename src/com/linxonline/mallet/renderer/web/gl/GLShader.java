package com.linxonline.mallet.renderer.web.gl ;

import org.teavm.jso.webgl.WebGLShader ;

/**
	Transitionary object used to retain important 
	shader information for the program build process.
	GLProgram contains a list of all GLShaders it needs.
	Once the GLProgram has been linked, all shader data 
	is removed.
*/
public class GLShader
{
	public final WebGLShader[] id = new WebGLShader[1] ;
	public final int type ;
	public final String file ;
	public String[] source = new String[1] ;

	public GLShader( final int _type, final String _file, final String _source )
	{
		type = _type ;
		file = _file ;
		source[0] = _source ;
	}
}
