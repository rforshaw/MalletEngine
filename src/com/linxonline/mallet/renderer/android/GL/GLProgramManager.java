package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;
import android.opengl.GLES30 ;

import com.linxonline.mallet.io.reader.TextReader ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.* ;

import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.util.settings.Settings ;

public class GLProgramManager extends AbstractManager<GLProgram>
{
	public static final int MVP_MATRIX           = 0 ;
	public static final int POSITION_MATRIX      = 1 ;
	public static final int VERTEX_ARRAY         = 2 ;
	public static final int COLOUR_ARRAY         = 3 ;
	public static final int NORMAL_ARRAY         = 4 ;
	public static final int TEXTURE_COORD_ARRAY0 = 5 ;
	public static final int TEXTURE_COORD_ARRAY1 = 6 ;
	public static final int TEXTURE_COORD_ARRAY2 = 7 ;
	public static final int TEXTURE_COORD_ARRAY3 = 8 ;

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

				return generateGLProgram( JSONObject.construct( stream ) ) ;
			}

			private GLProgram generateGLProgram( final JSONObject _jGL )
			{
				final ArrayList<GLShader> shaders = new ArrayList<GLShader>() ;

				{
					final JSONArray vertexShaders = _jGL.optJSONArray( "VERTEX" ) ;
					if( vertexShaders != null )
					{
						readShaders( vertexShaders, shaders, GLES30.GL_VERTEX_SHADER ) ;
					}
				}

				/*{
					final JSONArray geometryShaders = _jGL.optJSONArray( "GEOMETRY" ) ;
					if( geometryShaders != null )
					{
						readShaders( geometryShaders, shaders, GLES30.GL_GEOMETRY_SHADER ) ;
					}
				}*/

				{
					final JSONArray fragmentShaders = _jGL.optJSONArray( "FRAGMENT" ) ;
					if( fragmentShaders != null )
					{
						readShaders( fragmentShaders, shaders, GLES30.GL_FRAGMENT_SHADER ) ;
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

	public static void deleteProgram( final GLProgram _program )
	{
		// During the build process a programs 
		// shaders list has already been detached 
		// and destroyed.
		GLES30.glDeleteProgram( _program.id[0] ) ;
	}

	public static boolean buildProgram( final GLProgram _program )
	{
		_program.id[0] = GLES30.glCreateProgram() ;
		if( _program.id[0] < 1 )
		{
			System.out.println( "Failed to create program.." ) ;
			return false ;
		}

		for( final GLShader shader : _program.shaders )
		{
			// Attach only successfully compiled shaders
			if( compileShader( shader ) == true )
			{
				GLES30.glAttachShader( _program.id[0], shader.id[0] ) ;
			}
		}

		GLES30.glBindAttribLocation( _program.id[0], VERTEX_ARRAY, "inVertex" ) ;
		GLES30.glBindAttribLocation( _program.id[0], COLOUR_ARRAY, "inColour" ) ;
		GLES30.glBindAttribLocation( _program.id[0], TEXTURE_COORD_ARRAY0, "inTexCoord0" ) ;
		GLES30.glBindAttribLocation( _program.id[0], NORMAL_ARRAY,"inNormal" ) ;

		GLES30.glLinkProgram( _program.id[0] ) ;

		_program.inMVPMatrix = GLES30.glGetUniformLocation( _program.id[0], "inMVPMatrix" ) ;
		mapTexturesToProgram( _program ) ;

		// Once all of the shaders have been compiled 
		// and linked, we can then detach the shader sources
		// and delete the shaders from memory.
		for( final GLShader shader : _program.shaders )
		{
			GLES30.glDetachShader( _program.id[0], shader.id[0] ) ;
			GLES30.glDeleteShader( shader.id[0] ) ;
		}
		_program.shaders.clear() ;

		final int[] response = new int[]{ 0 } ;
		GLES30.glGetProgramiv( _program.id[0], GLES30.GL_LINK_STATUS, response, 0 ) ;
		if( response[0] == GLES30.GL_FALSE )
		{
			System.out.println( "Error linking program: " + GLES30.glGetProgramInfoLog( _program.id[0] ) ) ;
			return false ;
		}

		return true ;
	}

	/**
		Loop over a set of fixed 'inTex' uniform variables 
		from the GLProgram/GLShaders.
		Stop iterating as soon as an inTexi returns -1.
		inTex0 should map to GL_TEXTURE0
		inTex1 should map to GL_TEXTURE1
		inTex2 should map to GL_TEXTURE1 and so on..
		Currently an upper limit of 10 textures can be mapped.
	*/
	private static void mapTexturesToProgram( final GLProgram _program )
	{
		final int[] inTex = new int[10] ;
		for( int i = 0; i < 10; i++ )
		{
			final String inTexName = "inTex" + i ;
			inTex[i] = GLES30.glGetUniformLocation( _program.id[0], inTexName ) ;

			if( inTex[i] == -1 )
			{
				_program.copyTextures( inTex, i ) ;
				return ;
			}

			_program.copyTextures( inTex, inTex.length ) ;
		}
	}

	private static boolean compileShader( final GLShader _shader )
	{
		_shader.id[0] = GLES30.glCreateShader( _shader.type ) ;
		GLES30.glShaderSource( _shader.id[0], _shader.source[0] ) ;
		GLES30.glCompileShader( _shader.id[0] ) ;

		final int[] response = new int[]{ 0 } ;
		GLES30.glGetShaderiv( _shader.id[0], GLES30.GL_COMPILE_STATUS, response, 0 ) ;
		if( response[0] == GLES30.GL_FALSE )
		{
			System.out.println( "Error compiling shader: " + _shader.file + "\n" + GLES30.glGetShaderInfoLog( _shader.id[0] ) ) ;
			return false ;
		}

		return true ;
	}
}