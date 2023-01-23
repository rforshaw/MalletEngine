package com.linxonline.mallet.renderer.android.opengl ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.renderer.opengl.JSONProgram ;
import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.AssetLookup ;

import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.BoolUniform ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;

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
public final class GLProgram extends ProgramManager.Program
{
	public final JSONProgram program ;
	public final int[] id = new int[1] ;	// GL Program ID

	// Model View Projection Matrix, doesn't need to be defined in *.jgl,
	// however it must be defined in atleast vertex shader.
	public int inModelMatrix = -1 ;
	public int inMVPMatrix = -1 ;
	public final int[] inUniforms ;			// Additional uniforms defined in *.jgl and shaders
	public final int[] inDrawUniforms ;		// Additional uniforms defined in *.jgl and shaders  
	public final int[] inAttributes ;		// Vertex swivel order defined in *.jgl
	public final int[] inBuffers ;			// Additional Storage buffers defined in *.jgl and shaders

	private GLProgram( final JSONProgram _program )
	{
		program = _program ;
		inUniforms = new int[program.getUniforms().size()] ;
		inDrawUniforms = new int[program.getDrawUniforms().size()] ;
		inAttributes = new int[program.getAttribute().size()] ;
		inBuffers    = new int[program.getBuffers().size()] ;

		final int length = inAttributes.length ;
		for( int i = 0; i < length; i++ )
		{
			inAttributes[i] = i ;
		}
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
	public void destroy()
	{
		delete( this ) ;
	}

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
			final List<String> swivel = _program.getAttribute() ;
			final int size = swivel.size() ;

			for( int i = 0; i < size; i++ )
			{
				MGL.glBindAttribLocation( program.id[0], program.inAttributes[i], swivel.get( i ) ) ;
			}
		}

		MGL.glLinkProgram( program.id[0] ) ;

		program.inModelMatrix = MGL.glGetUniformLocation( program.id[0], "inModelMatrix" ) ;
		program.inMVPMatrix = MGL.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;

		{
			final List<JSONProgram.UniformMap> uniforms = _program.getUniforms() ;

			final int size = uniforms.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
				program.inUniforms[i] = MGL.glGetUniformLocation( program.id[0], uniform.getRight() ) ;
			}
		}

		{
			final List<JSONProgram.UniformMap> uniforms = _program.getDrawUniforms() ;

			final int size = uniforms.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
				program.inDrawUniforms[i] = MGL.glGetUniformLocation( program.id[0], uniform.getRight() ) ;
			}
		}

		{
			final List<String> buffers = _program.getBuffers() ;
			final int size = buffers.size() ;

			for( int i = 0; i < size; i++ )
			{
				final int loc = MGL.glGetProgramResourceIndex( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, buffers.get( i ) ) ;
				program.inBuffers[i] = loc ;
				System.out.println( "Storage Block Binding: " + loc ) ;
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
			System.out.println( program.getName() + " Error linking program: " + MGL.glGetProgramInfoLog( program.id[0] ) ) ;
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
		_shaderIDs[_index] = MGL.glCreateShader( convertType( _map.getLeft() ) ) ;
		MGL.glShaderSource( _shaderIDs[_index], _map.getRight() ) ;
		MGL.glCompileShader( _shaderIDs[_index] ) ;

		final int[] response = new int[]{ 0 } ;
		MGL.glGetShaderiv( _shaderIDs[_index], MGL.GL_COMPILE_STATUS, response, 0 ) ;
		if( response[0] == MGL.GL_FALSE )
		{
			System.out.println( "Error compiling shader: " + MGL.glGetShaderInfoLog( _shaderIDs[_index] ) ) ;
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
			//case GEOMETRY : return GLES30.GL_GEOMETRY_SHADER ;
			//case COMPUTE  : return GLES30.GL_COMPUTE_SHADER ;
			default       : return -1 ;
		}
	}
}
