package com.linxonline.mallet.renderer.desktop.GL ;

import java.util.ArrayList ;
import javax.media.opengl.* ;

import com.linxonline.mallet.io.reader.TextReader ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.* ;

import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Tuple ;

public class GLProgramManager extends AbstractManager<GLProgram>
{
	//public static final int MVP_MATRIX           = 0 ;
	//public static final int POSITION_MATRIX      = 1 ;

	/**
		When loading a program the ProgramManager will load the 
		content a-synchronously.
		To ensure the programs are added safely to resources we 
		temporarily store the program in a queue.
	*/
	private final GLProgram PLACEHOLDER = new GLProgram( "PLACEHOLDER", null, null, null, null ) ;
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

				fill( paths, _jGL.optJSONArray( "VERTEX" ),   GL3.GL_VERTEX_SHADER ) ;
				fill( paths, _jGL.optJSONArray( "GEOMETRY" ), GL3.GL_GEOMETRY_SHADER ) ;
				fill( paths, _jGL.optJSONArray( "FRAGMENT" ), GL3.GL_FRAGMENT_SHADER ) ;

				final ArrayList<Tuple<String, GLProgram.DataType>> uniforms = new ArrayList<Tuple<String, GLProgram.DataType>>() ;
				final ArrayList<String> uniformTextures = new ArrayList<String>() ;
				final ArrayList<String> swivel = new ArrayList<String>() ;

				fillUniforms( uniforms,          _jGL.optJSONArray( "UNIFORMS" ) ) ;
				fillAttributes( uniformTextures, _jGL.optJSONArray( "UNIFORM_TEXTURES" ) ) ;
				fillAttributes( swivel,          _jGL.optJSONArray( "SWIVEL" ) ) ;

				readShaders( _jGL.optString( "NAME", "undefined" ), paths, shaders, uniforms, uniformTextures, swivel ) ;
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

			private void fillAttributes( final ArrayList<String> _toFill, final JSONArray _base )
			{
				if( _base == null )
				{
					return ;
				}

				final int length = _base.length() ;
				for( int i = 0; i < length; i++ )
				{
					_toFill.add( _base.optString( i ) ) ;
				}
			}

			private void fillUniforms( final ArrayList<Tuple<String, GLProgram.DataType>> _toFill, final JSONArray _base )
			{
				if( _base == null )
				{
					return ;
				}

				final int length = _base.length() ;
				for( int i = 0; i < length; i++ )
				{
					final JSONObject obj = _base.optJSONObject( i ) ;
					final String name = obj.optString( "NAME", null ) ;
					final GLProgram.DataType type = GLProgram.DataType.valueOf( obj.optString( "TYPE", null ) ) ;

					if( name != null && type != null )
					{
						_toFill.add( new Tuple<String, GLProgram.DataType>( name, type ) ) ;
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
									  final ArrayList<GLShaderMap> _jShaders,
									  final ArrayList<GLShader> _glShaders,
									  final ArrayList<Tuple<String, GLProgram.DataType>> _uniforms,
									  final ArrayList<String> _uniformTextures,
									  final ArrayList<String> _swivel )
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
						toBind.add( new GLProgram( _name, _glShaders, _uniforms, _uniformTextures, _swivel ) ) ;
					}

					return ;
				}

				final GLShaderMap map = _jShaders.remove( 0 ) ;
				final FileStream stream = GlobalFileSystem.getFile( map.path ) ;
				if( stream.exists() == false )
				{
					System.out.println( "Unable to find: " + map.path ) ;
					readShaders( _name, _jShaders, _glShaders, _uniforms, _uniformTextures, _swivel ) ;
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
						readShaders( _name, _jShaders, _glShaders, _uniforms, _uniformTextures, _swivel ) ;
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
				final GL3 gl = GLRenderer.getCanvas().getContext().getCurrentGL().getGL3() ;
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

		final ArrayList<String> swivel = _program.swivel ;
		final int size = swivel.size() ;

		for( int i = 0; i < size; i++ )
		{
			_gl.glBindAttribLocation( _program.id[0], _program.inAttributes[i], swivel.get( i ) ) ;
		}

		_gl.glLinkProgram( _program.id[0] ) ;

		_program.inMVPMatrix = _gl.glGetUniformLocation( _program.id[0], "inMVPMatrix" ) ;
		mapTexturesToProgram( _gl, _program ) ;

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

	/**
		Loop over a set of fixed 'inTex' uniform variables 
		from the GLProgram/GLShaders.
		Stop iterating as soon as an inTexi returns -1.
		inTex0 should map to GL_TEXTURE0
		inTex1 should map to GL_TEXTURE1
		inTex2 should map to GL_TEXTURE2 and so on..
		Currently an upper limit of 10 textures can be mapped.
	*/
	private static void mapTexturesToProgram( final GL3 _gl, final GLProgram _program )
	{
		final ArrayList<String> inTextures = _program.uniformTextures ;

		final int size = inTextures.size() ;
		for( int i = 0; i < size; i++ )
		{
			final String name = inTextures.get( 0 ) ;
			_program.inUniformTextures[i] = _gl.glGetUniformLocation( _program.id[0], name ) ;
		}
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
