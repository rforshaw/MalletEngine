package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.renderer.opengl.JSONProgram ;
import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.io.Resource ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletTexture ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public class GLProgram extends ProgramManager.Program
{
	public final JSONProgram program ;
	public final int[] id = new int[1] ;	// GL Program ID

	// Model View Projection Matrix, doesn't need to be defined in *.jgl,
	// however it must be defined in atleast vertex shader. 
	public int inMVPMatrix = -1 ;
	public final int[] inUniforms ;			// Additional uniforms defined in *.jgl and shaders  
	public final int[] inAttributes ;		// Vertex swivel order defined in *.jgl
	public final int[] inBuffers ;			// Additional Storage buffers defined in *.jgl and shaders

	private GLProgram( final JSONProgram _program )
	{
		program = _program ;
		inUniforms   = new int[program.getUniforms().size()] ;
		inAttributes = new int[program.getSwivel().size()] ;
		inBuffers    = new int[program.getBuffers().size()] ;

		final int length = inAttributes.length ;
		for( int i = 0; i < length; i++ )
		{
			inAttributes[i] = i ;
		}
	}

	/**
		The program-map defined by the user is not efficient 
		for loading uniforms as it specifies MalletTextures and 
		MalletFonts instead of GLImage and GLFont.

		Construct a program-map that references render specific 
		resources instead.

		If we fail to build an efficient map then return null.
	*/
	public ProgramMap<GLProgram> buildMap( final ProgramMap<GLProgram> _map )
	{
		final ProgramMap<GLProgram> map = new ProgramMap<GLProgram>( _map ) ;

		final List<JSONProgram.UniformMap> uniforms = program.getUniforms() ;
		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
			switch( uniform.getLeft() )
			{
				case BOOL         :
				case INT32        :
				case UINT32       :
				case FLOAT32      :
				case FLOAT64      :
				case FLOAT32_VEC2 :
				case FLOAT32_VEC3 :
				case FLOAT32_VEC4 :
				{
					Logger.println( "Build uniform type not implemented", Logger.Verbosity.MAJOR ) ;
					return null ;
				}
				case FLOAT32_MAT4 : break ;
				case SAMPLER2D    :
				{
					final MalletTexture texture = ( MalletTexture )map.getUniform( uniform.getRight() ) ;
					final GLImage glTexture = GLRenderer.getTexture( texture.getPath() ) ;
					if( glTexture == null )
					{
						return null ;
					}

					map.setUniform( uniform.getRight(), glTexture ) ;
					break ;
				}
				case FONT         :
				{
					final MalletFont font = ( MalletFont )map.getUniform( uniform.getRight() ) ;
					final GLFont glFont = GLRenderer.getFont( font ) ;

					final GLImage texture = glFont.getTexture() ;
					if( texture == null )
					{
						return null ;
					}

					map.setUniform( uniform.getRight(), texture ) ;
					break ;
				}
				case UNKNOWN      :
				default           : return null ;
			}
		}

		map.setDirty( false ) ;
		return map ;
	}

	/**
		A GL Program will have information that it requires 
		before it can be used effectively.
		This information can be loaded in via uniforms.
		The jgl file defined what uniforms our shader program wants 
		and the order in-which we should receive them in.
	*/
	public boolean loadUniforms( final ProgramMap<GLProgram> _data )
	{
		final List<JSONProgram.UniformMap> uniforms = program.getUniforms() ;
		int textureUnit = 0 ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
			switch( uniform.getLeft() )
			{
				case BOOL         :
				case INT32        :
				case UINT32       :
				case FLOAT32      :
				case FLOAT64      :
				case FLOAT32_VEC2 :
				case FLOAT32_VEC3 :
				case FLOAT32_VEC4 :
				{
					Logger.println( "Load uniform type not implemented", Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case FLOAT32_MAT4 :
				{
					final Matrix4 m = ( Matrix4 )_data.getUniform( uniform.getRight() ) ;
					final float[] matrix = m.matrix ;

					MGL.glUniformMatrix4fv( inUniforms[i], 1, true, matrix, 0 ) ;
					break ;
				}
				case SAMPLER2D    :
				{
					final GLImage texture = ( GLImage )_data.getUniform( uniform.getRight() ) ;

					MGL.glActiveTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, texture.textureIDs[0] ) ;
					textureUnit += 1 ;
					break ;
				}
				case FONT         :
				{
					final GLImage texture = ( GLImage )_data.getUniform( uniform.getRight() ) ;

					MGL.glActiveTexture( MGL.GL_TEXTURE0 + textureUnit ) ;			//GLRenderer.handleError( "Activate Texture", _gl ) ;
					MGL.glBindTexture( MGL.GL_TEXTURE_2D, texture.textureIDs[0] ) ;	//GLRenderer.handleError( "Bind Texture", _gl ) ;
					textureUnit += 1 ;
					break ;
				}
				case UNKNOWN      :
				default           : return false ;
			}
		}

		return true ;
	}

	/**
		Should only be used on a ProgramMap created by the user.
		Will crash if used with a ProgramMap built using buildMap().

		The program map contains the references to resources 
		that are potential managed by the renderer.

		Using the uniforms loop over the map and record 
		within _activeKeys the keys for those resources.
	*/
	public void getUsedResources( final Set<String> _activeKeys, final ProgramMap<GLProgram> _data )
	{
		program.getUsedResources( _activeKeys, _data ) ;
	}

	/**
		Ensure that the Mallet Program maps correctly with 
		the GL Program it is apparently associated with.
	*/
	public boolean isValidMap( final Map<String, Object> _map )
	{
		return program.isUniformsValid( _map ) ;
	}

	@Override
	public String getName()
	{
		return program.getName() ;
	}

	@Override
	public long getMemoryConsumption()
	{
		return 0L ;
	}

	@Override
	public void destroy() {}

	@Override
	public String type()
	{
		return "GLPROGRAM" ;
	}

	/**
		Call to generate a GLProgram.
	*/
	public static GLProgram build( final JSONProgram _program )
	{
		final GLProgram program = new GLProgram( _program ) ;

		program.id[0] = MGL.glCreateProgram() ;
		if( program.id[0] < 1 )
		{
			System.out.println( "Failed to create program.." ) ;
			return null ;
		}

		final List<JSONProgram.ShaderMap> shaders = _program.getShaders() ;
		final int[] shaderIDs = new int[shaders.size()] ;

		{
			final int size = shaders.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.ShaderMap shader = shaders.get( i ) ;
				// Attach only successfully compiled shaders
				if( compileShader( shader, i, shaderIDs ) == true )
				{
					MGL.glAttachShader( program.id[0], shaderIDs[i] ) ;
				}
			}

			shaders.clear() ;
		}

		{
			final List<String> swivel = _program.getSwivel() ;
			final int size = swivel.size() ;

			for( int i = 0; i < size; i++ )
			{
				MGL.glBindAttribLocation( program.id[0], program.inAttributes[i], swivel.get( i ) ) ;
			}
		}

		MGL.glLinkProgram( program.id[0] ) ;

		program.inMVPMatrix = MGL.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;

		{
			final List<String> buffers = _program.getBuffers() ;
			final int size = buffers.size() ;

			for( int i = 0; i < size; i++ )
			{
				final int loc = MGL.glGetProgramResourceIndex( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, buffers.get( i ) ) ;
				program.inBuffers[i] = loc ;
				//System.out.println( "Storage Block Binding: " + loc ) ;
				//System.out.println( "Error: " + MGL.glGetError() ) ;
			}
		}

		// Once all of the shaders have been compiled 
		// and linked, we can then detach the shader sources
		// and delete the shaders from memory.
		for( int i = 0; i < shaderIDs.length; i++ )
		{
			MGL.glDetachShader( program.id[0], shaderIDs[i] ) ;
			MGL.glDeleteShader( shaderIDs[i] ) ;
		}

		final int[] response = new int[]{ 0 } ;
		MGL.glGetProgramiv( program.id[0], MGL.GL_LINK_STATUS, response, 0 ) ;
		if( response[0] == MGL.GL_FALSE )
		{
			final int[] logLength = new int[1] ;
			MGL.glGetProgramiv( program.id[0], MGL.GL_INFO_LOG_LENGTH, logLength, 0 ) ;

			final byte[] log = new byte[logLength[0]] ;
			MGL.glGetProgramInfoLog( program.id[0], logLength[0], ( int[] )null, 0, log, 0 ) ;

			System.out.println( "Error linking program: " + new String( log ) ) ;
			GLProgram.delete( program ) ;
			return null ;
		}

		return program ;
	}

	public static void delete( final GLProgram _program )
	{
		// During the build process a programs 
		// shaders list has already been detached 
		// and destroyed.
		MGL.glDeleteProgram( _program.id[0] ) ;
	}

	private static boolean compileShader( final JSONProgram.ShaderMap _map, final int _index, final int[] _shaderIDs )
	{
		final JSONProgram.Type type = _map.getLeft() ;
		final String[] source = new String[1] ;
		source[0] = _map.getRight() ;

		_shaderIDs[_index] = MGL.glCreateShader( convertType( type ) ) ;
		MGL.glShaderSource( _shaderIDs[_index], 1, source, null ) ;
		MGL.glCompileShader( _shaderIDs[_index] ) ;

		final int[] response = new int[]{ 0 } ;
		MGL.glGetShaderiv( _shaderIDs[_index], MGL.GL_COMPILE_STATUS, response, 0 ) ;
		if( response[0] == MGL.GL_FALSE )
		{
			final int[] logLength = new int[1] ;
			MGL.glGetShaderiv( _shaderIDs[_index], MGL.GL_INFO_LOG_LENGTH, logLength, 0 ) ;

			final byte[] log = new byte[logLength[0]] ;
			MGL.glGetShaderInfoLog( _shaderIDs[_index], logLength[0], ( int[] )null, 0, log, 0 ) ;

			System.out.println( "Error compiling shader: " + new String( log ) ) ;
			return false ;
		}

		return true ;
	}

	private static int convertType( final JSONProgram.Type _type )
	{
		switch( _type )
		{
			case VERTEX   : return MGL.GL_VERTEX_SHADER ;
			case FRAGMENT : return MGL.GL_FRAGMENT_SHADER ;
			case GEOMETRY : return MGL.GL_GEOMETRY_SHADER ;
			case COMPUTE  : return MGL.GL_COMPUTE_SHADER ;
			default       : return -1 ;
		}
	}
}
