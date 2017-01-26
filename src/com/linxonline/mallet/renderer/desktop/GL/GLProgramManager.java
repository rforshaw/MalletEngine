package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.List ;
import javax.media.opengl.* ;

import com.linxonline.mallet.io.reader.TextReader ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.* ;

import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.desktop.GL.GLProgram.Uniform ;

public class GLProgramManager extends AbstractManager<GLProgram>
{
	/**
		When loading a program the ProgramManager will load the 
		content a-synchronously.
		To ensure the programs are added safely to resources we 
		temporarily store the program in a queue.
	*/
	private final GLProgram PLACEHOLDER = new GLProgram( "PLACEHOLDER", null, null, null ) ;
	private final List<GLProgram> toBind = MalletList.<GLProgram>newList() ;

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
				final List<GLShader> shaders = MalletList.<GLShader>newList() ;
				final List<GLShaderMap> paths = MalletList.<GLShaderMap>newList() ;

				fill( paths, _jGL.getJSONArray( "VERTEX" ),   GL3.GL_VERTEX_SHADER ) ;
				fill( paths, _jGL.getJSONArray( "GEOMETRY" ), GL3.GL_GEOMETRY_SHADER ) ;
				fill( paths, _jGL.getJSONArray( "FRAGMENT" ), GL3.GL_FRAGMENT_SHADER ) ;

				final List<Tuple<String, Uniform>> uniforms = MalletList.<Tuple<String, Uniform>>newList() ;
				final List<String> swivel = MalletList.<String>newList() ;

				fillUniforms( uniforms, _jGL.getJSONArray( "UNIFORMS" ) ) ;
				fillAttributes( swivel, _jGL.getJSONArray( "SWIVEL" ) ) ;

				readShaders( _jGL.optString( "NAME", "undefined" ), paths, shaders, uniforms, swivel ) ;
			}

			private void fill( final List<GLShaderMap> _toFill, final JSONArray _base, final int _type )
			{
				if( _base == null )
				{
					return ;
				}

				final int length = _base.length() ;
				for( int i = 0; i < length; i++ )
				{
					_toFill.add( new GLShaderMap( _base.getString( i ), _type ) ) ;
				}
			}

			private void fillAttributes( final List<String> _toFill, final JSONArray _base )
			{
				if( _base == null )
				{
					return ;
				}

				final int length = _base.length() ;
				for( int i = 0; i < length; i++ )
				{
					_toFill.add( _base.getString( i ) ) ;
				}
			}

			private void fillUniforms( final List<Tuple<String, Uniform>> _toFill, final JSONArray _base )
			{
				if( _base == null )
				{
					return ;
				}

				final int length = _base.length() ;
				for( int i = 0; i < length; i++ )
				{
					final JSONObject obj = _base.getJSONObject( i ) ;
					final String name = obj.optString( "NAME", null ) ;
					final Uniform type = Uniform.valueOf( obj.optString( "TYPE", null ) ) ;

					if( name != null && type != null )
					{
						_toFill.add( new Tuple<String, Uniform>( name, type ) ) ;
					}
				}
			}

			/**
				Recusive function.
				Loop through _jShaders loading the sources into 
				_glShaders, once _jShaders is empty construct 
				a GLProgram and add it to the toBind array.
			*/
			private void readShaders( final String _name,
									  final List<GLShaderMap> _jShaders,
									  final List<GLShader> _glShaders,
									  final List<Tuple<String, Uniform>> _uniforms,
									  final List<String> _swivel )
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
						toBind.add( new GLProgram( _name, _glShaders, _uniforms, _swivel ) ) ;
					}

					return ;
				}

				final GLShaderMap map = _jShaders.remove( 0 ) ;
				final FileStream stream = GlobalFileSystem.getFile( map.path ) ;
				if( stream.exists() == false )
				{
					System.out.println( "Unable to find: " + map.path ) ;
					readShaders( _name, _jShaders, _glShaders, _uniforms, _swivel ) ;
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
						readShaders( _name, _jShaders, _glShaders, _uniforms, _swivel ) ;
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
			// GLRenderer will continuosly call get() until it 
			// recieves a GLProgram, so we need to compile Programs
			// that are waiting for the OpenGL context 
			// when the render requests it.
			if( toBind.isEmpty() == false )
			{
				final GL3 gl = GLRenderer.getGL() ;
				if( gl == null )
				{
					System.out.println( "GL context doesn't exist" ) ;
					return null ;
				}

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

	public static void deleteProgram( final GL3 _gl, final GLProgram _program )
	{
		// During the build process a programs 
		// shaders list has already been detached 
		// and destroyed.
		_gl.glDeleteProgram( _program.id[0] ) ;
	}

	public static boolean buildProgram( final GL3 _gl, final GLProgram _program )
	{
		_program.id[0] = _gl.glCreateProgram() ;
		if( _program.id[0] < 1 )
		{
			System.out.println( "Failed to create program.." ) ;
			return false ;
		}

		for( final GLShader shader : _program.shaders )
		{
			// Attach only successfully compiled shaders
			if( compileShader( _gl, shader ) == true )
			{
				_gl.glAttachShader( _program.id[0], shader.id[0] ) ;
			}
		}

		final List<String> swivel = _program.swivel ;
		final int size = swivel.size() ;

		for( int i = 0; i < size; i++ )
		{
			_gl.glBindAttribLocation( _program.id[0], _program.inAttributes[i], swivel.get( i ) ) ;
		}

		_gl.glLinkProgram( _program.id[0] ) ;

		_program.inMVPMatrix = _gl.glGetUniformLocation( _program.id[0], "inMVPMatrix" ) ;

		// Once all of the shaders have been compiled 
		// and linked, we can then detach the shader sources
		// and delete the shaders from memory.
		for( final GLShader shader : _program.shaders )
		{
			_gl.glDetachShader( _program.id[0], shader.id[0] ) ;
			_gl.glDeleteShader( shader.id[0] ) ;
		}
		_program.shaders.clear() ;

		final int[] response = new int[]{ 0 } ;
		_gl.glGetProgramiv( _program.id[0], GL3.GL_LINK_STATUS, response, 0 ) ;
		if( response[0] == GL3.GL_FALSE )
		{
			final int[] logLength = new int[1] ;
			_gl.glGetProgramiv( _program.id[0], GL3.GL_INFO_LOG_LENGTH, logLength, 0 ) ;

			final byte[] log = new byte[logLength[0]] ;
			_gl.glGetProgramInfoLog( _program.id[0], logLength[0], ( int[] )null, 0, log, 0 ) ;

			System.out.println( "Error linking program: " + new String( log ) ) ;
			return false ;
		}

		return true ;
	}

	private static boolean compileShader( final GL3 _gl, final GLShader _shader )
	{
		_shader.id[0] = _gl.glCreateShader( _shader.type ) ;
		_gl.glShaderSource( _shader.id[0], 1, _shader.source, null ) ;
		_gl.glCompileShader( _shader.id[0] ) ;

		final int[] response = new int[]{ 0 } ;
		_gl.glGetShaderiv( _shader.id[0], GL3.GL_COMPILE_STATUS, response, 0 ) ;
		if( response[0] == GL3.GL_FALSE )
		{
			final int[] logLength = new int[1] ;
			_gl.glGetShaderiv( _shader.id[0], GL3.GL_INFO_LOG_LENGTH, logLength, 0 ) ;

			final byte[] log = new byte[logLength[0]] ;
			_gl.glGetShaderInfoLog( _shader.id[0], logLength[0], ( int[] )null, 0, log, 0 ) ;

			System.out.println( "Error compiling shader: " + _shader.file + "\n" + new String( log ) ) ;
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
