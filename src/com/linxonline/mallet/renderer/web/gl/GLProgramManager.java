package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;

import org.teavm.jso.webgl.WebGLRenderingContext ;

import com.linxonline.mallet.io.reader.TextReader ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.* ;

import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.util.settings.Settings ;

public class GLProgramManager extends AbstractManager<GLProgram>
{
	public static final int MVP_MATRIX           = 7 ;
	public static final int POSITION_MATRIX      = 8 ;

	public static final int VERTEX_ARRAY         = 0 ;
	public static final int COLOUR_ARRAY         = 1 ;
	public static final int NORMAL_ARRAY         = 2 ;
	public static final int TEXTURE_COORD_ARRAY0 = 3 ;
	public static final int TEXTURE_COORD_ARRAY1 = 4 ;
	public static final int TEXTURE_COORD_ARRAY2 = 5 ;
	public static final int TEXTURE_COORD_ARRAY3 = 6 ;

	public GLProgramManager()
	{
		final ResourceLoader<GLProgram> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<GLProgram>()
		{
			public boolean isLoadable( final String _file )
			{
				return GlobalFileSystem.isExtension( _file, ".jgl", ".JGL" ) ;
			}

			public GLProgram load( final String _file, final Settings _settings )
			{
				final FileStream stream = GlobalFileSystem.getFile( _file ) ;
				if( stream.exists() == false )
				{
					System.out.println( "Unable to find: " + _file ) ;
					return null ;
				}

				//System.out.println( "Loading Shader Program: " + _file ) ;
				return generateGLProgram( JSONObject.construct( stream ) ) ;
			}

			private GLProgram generateGLProgram( final JSONObject _jGL )
			{
				final ArrayList<GLShader> shaders = new ArrayList<GLShader>() ;

				{
					//System.out.println( "Generate Vertex Shaders.." ) ;
					final JSONArray vertexShaders = _jGL.optJSONArray( "VERTEX" ) ;
					if( vertexShaders != null )
					{
						readShaders( vertexShaders, shaders, GL3.VERTEX_SHADER ) ;
					}
				}

				{
					//System.out.println( "Generate Fragment Shaders.." ) ;
					final JSONArray fragmentShaders = _jGL.optJSONArray( "FRAGMENT" ) ;
					if( fragmentShaders != null )
					{
						readShaders( fragmentShaders, shaders, GL3.FRAGMENT_SHADER ) ;
					}
				}

				if( shaders.isEmpty() == true )
				{
					System.out.println( "Unable to generate GLProgram, no shaders specified." ) ;
					return null ;
				}

				return new GLProgram( _jGL.optString( "NAME", "undefined" ), shaders ) ;
			}

			private void readShaders( final JSONArray _jShaders,
									  final ArrayList<GLShader> _glShaders,
									  final int _type )
			{
				final int length = _jShaders.length() ;
				for( int i = 0; i < length; i++ )
				{
					final String path = _jShaders.optString( i ) ;
					final String source = TextReader.getTextAsString( path ) ;
					if( source != null )
					{
						_glShaders.add( new GLShader( _type, path, source ) ) ;
					}
				}
			}
		} ) ;
	}

	public static void deleteProgram( final WebGLRenderingContext _gl, final GLProgram _program )
	{
		// During the build process a programs 
		// shaders list has already been detached 
		// and destroyed.
		_gl.deleteProgram( _program.id[0] ) ;
	}

	public static boolean buildProgram( final WebGLRenderingContext _gl, final GLProgram _program )
	{
		_program.id[0] = _gl.createProgram() ;
		if( _program.id[0] == null )
		{
			System.out.println( "Failed to create program.." ) ;
			return false ;
		}

		for( final GLShader shader : _program.shaders )
		{
			// Attach only successfully compiled shaders
			if( compileShader( _gl, shader ) == true )
			{
				_gl.attachShader( _program.id[0], shader.id[0] ) ;
			}
		}

		//_gl.glBindUniformLocation( _program.id[0], MVP_MATRIX, "inMVPMatrix" ) ;
		//_gl.glBindUniformLocation( _program.id[0], POSITION_MATRIX, "inPositionMatrix" ) ;
		_gl.bindAttribLocation( _program.id[0], VERTEX_ARRAY, "inVertex" ) ;
		_gl.bindAttribLocation( _program.id[0], COLOUR_ARRAY, "inColour" ) ;
		_gl.bindAttribLocation( _program.id[0], TEXTURE_COORD_ARRAY0, "inTexCoord0" ) ;
		_gl.bindAttribLocation( _program.id[0], NORMAL_ARRAY, "inNormal" ) ;

		_gl.linkProgram( _program.id[0] ) ;

		// Once all of the shaders have been compiled 
		// and linked, we can then detach the shader sources
		// and delete the shaders from memory.
		for( final GLShader shader : _program.shaders )
		{
			_gl.detachShader( _program.id[0], shader.id[0] ) ;
			_gl.deleteShader( shader.id[0] ) ;
		}
		_program.shaders.clear() ;

		final boolean response = _gl.getProgramParameterb( _program.id[0], GL3.LINK_STATUS ) ;
		if( response == false )
		{
			final String log = _gl.getProgramInfoLog( _program.id[0] ) ;
			System.out.println( "Error linking program: " + log ) ;
			return false ;
		}

		return true ;
	}

	private static boolean compileShader( final WebGLRenderingContext _gl, final GLShader _shader )
	{
		_shader.id[0] = _gl.createShader( _shader.type ) ;
		_gl.shaderSource( _shader.id[0], _shader.source[0] ) ;
		_gl.compileShader( _shader.id[0] ) ;

		final boolean response = _gl.getShaderParameterb( _shader.id[0], GL3.COMPILE_STATUS ) ;
		if( response == false )
		{
			final String log = _gl.getShaderInfoLog( _shader.id[0] ) ;
			System.out.println( "Error compiling shader: " + _shader.file + "\n" + log ) ;
			return false ;
		}

		return true ;
	}
}