package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLUniformLocation ;

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

	/**
		When loading a program the ProgramManager will load the 
		content a-synchronously.
		To ensure the programs are added safely to resources we 
		temporarily store the program in a queue.
	*/
	private final GLProgram PLACEHOLDER = new GLProgram( "PLACEHOLDER", null ) ;
	private final ArrayList<GLProgram> toBind = new ArrayList<GLProgram>() ;

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

				add( _file, PLACEHOLDER ) ;
				JSONObject.construct( stream, new JSONObject.ConstructCallback()
				{
					public void callback( final JSONObject _obj )
					{
						generateGLProgram( _obj ) ;
					}
				} ) ;

				return null ;
			}

			private void generateGLProgram( final JSONObject _jGL )
			{
				final ArrayList<GLShader> shaders = new ArrayList<GLShader>() ;
				final ArrayList<GLShaderMap> paths = new ArrayList<GLShaderMap>() ;

				fill( paths, _jGL.optJSONArray( "VERTEX" ), GL3.VERTEX_SHADER ) ;
				//fill( paths, _jGL.optJSONArray( "GEOMETRY" ), GL3.GEOMETRY_SHADER ) ;
				fill( paths, _jGL.optJSONArray( "FRAGMENT" ), GL3.FRAGMENT_SHADER ) ;

				readShaders( _jGL.optString( "NAME", "undefined" ), paths, shaders ) ;
			}

			private void fill( final ArrayList<GLShaderMap> _toFill, final JSONArray _base, final int _type )
			{
				if( _base == null )
				{
					return ;
				}

				final int length = _base.length() ;
				for( int i = 0; i < length; i++ )
				{
					_toFill.add( new GLShaderMap( _base.optString( i ), _type ) ) ;
				}
			}

			/**
				Recusive function.
				Loop through _jShaders loading the sources into 
				_glShaders, once _jShaders is empty construct 
				a GLProgram and add it to the toBind array.
			*/
			private void readShaders( final String _name,
									  final ArrayList<GLShaderMap> _jShaders,
									  final ArrayList<GLShader> _glShaders )
			{
				if( _jShaders.isEmpty() == true )
				{
					if( _glShaders.isEmpty() == true )
					{
						System.out.println( "Unable to generate GLProgram, no shaders specified." ) ;
						return ;
					}

					synchronized( toBind )
					{
						// We don't want to compile the Shaders now
						// as that will take control of the OpenGL context.
						toBind.add( new GLProgram( _name, _glShaders ) ) ;
					}

					return ;
				}

				final GLShaderMap map = _jShaders.remove( 0 ) ;
				final FileStream stream = GlobalFileSystem.getFile( map.path ) ;
				if( stream.exists() == false )
				{
					System.out.println( "Unable to find: " + map.path ) ;
					readShaders( _name, _jShaders, _glShaders ) ;
					return ;
				}

				stream.getStringInCallback( new StringInCallback()
				{
					private final StringBuilder source = new StringBuilder() ;

					public int resourceAsString( final String[] _resource, final int _length )
					{
						for( int i = 0; i < _length; i++ )
						{
							source.append( _resource[i] ) ;
							source.append( '\n' ) ;
						}
					
						return 1 ;
					}

					public void start() {}

					public void end()
					{
						_glShaders.add( new GLShader( map.type, map.path, source.toString() ) ) ;
						readShaders( _name, _jShaders, _glShaders ) ;
					}
				}, 1 ) ;
			}
		} ) ;
	}

	@Override
	public GLProgram get( final String _key )
	{
		synchronized( toBind )
		{
			final WebGLRenderingContext gl = GLRenderer.getContext() ;
			if( gl == null )
			{
				return null ;
			}

			// GLRenderer will continuosly call get() until it 
			// recieves a GLProgram, so we need to compile Programs
			// that are waiting for the OpenGL context 
			// when the render requests it.
			if( toBind.isEmpty() == false )
			{
				for( final GLProgram program : toBind )
				{
					if( GLProgramManager.buildProgram( gl, program ) == false )
					{
						System.out.println( "Failed to compile program: " + program.name ) ;
						GLProgramManager.deleteProgram( gl, program ) ;
					}
					add( program.name, program ) ;
				}
				toBind.clear() ;
			}
		}

		final GLProgram program = super.get( _key ) ;

		// PLACEHOLDER is used to prevent the program loader 
		// loading the same program twice when loading async, 
		return ( program != PLACEHOLDER ) ? program : null ;
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

		_gl.bindAttribLocation( _program.id[0], VERTEX_ARRAY, "inVertex" ) ;
		_gl.bindAttribLocation( _program.id[0], COLOUR_ARRAY, "inColour" ) ;
		_gl.bindAttribLocation( _program.id[0], TEXTURE_COORD_ARRAY0, "inTexCoord0" ) ;
		_gl.bindAttribLocation( _program.id[0], NORMAL_ARRAY, "inNormal" ) ;

		_gl.linkProgram( _program.id[0] ) ;

		_program.inMVPMatrix = _gl.getUniformLocation( _program.id[0], "inMVPMatrix" ) ;
		mapTexturesToProgram( _gl, _program ) ;

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

	/**
		Loop over a set of fixed 'inTex' uniform variables 
		from the GLProgram/GLShaders.
		Stop iterating as soon as an inTexi returns -1.
		inTex0 should map to GL_TEXTURE0
		inTex1 should map to GL_TEXTURE1
		inTex2 should map to GL_TEXTURE1 and so on..
		Currently an upper limit of 10 textures can be mapped.
	*/
	private static void mapTexturesToProgram( final WebGLRenderingContext _gl, final GLProgram _program )
	{
		final WebGLUniformLocation[] inTex = new WebGLUniformLocation[10] ;
		for( int i = 0; i < 10; i++ )
		{
			final String inTexName = "inTex" + i ;
			inTex[i] = _gl.getUniformLocation( _program.id[0], inTexName ) ;

			if( inTex[i] == null )
			{
				_program.copyTextures( inTex, i ) ;
				return ;
			}

			_program.copyTextures( inTex, inTex.length ) ;
		}
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
	
	private class GLShaderMap
	{
		public String path ;
		public int type ;

		public GLShaderMap( final String _path, final int _type )
		{
			path = _path ;
			type = _type ;
		}
	}
}