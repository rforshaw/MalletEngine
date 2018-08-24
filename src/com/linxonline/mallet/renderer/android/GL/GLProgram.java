package com.linxonline.mallet.renderer.android.GL ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import android.opengl.GLES30 ;

import com.linxonline.mallet.renderer.opengl.JSONProgram ;
import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.util.buffers.IFloatBuffer ;
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

	public GLProgram( final JSONProgram _program )
	{
		program = _program ;
		inUniforms   = new int[program.getUniforms().size()] ;
		inAttributes = new int[program.getSwivel().size()] ;

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
					final MalletTexture texture = ( MalletTexture )map.get( uniform.getRight() ) ;
					final GLImage glTexture = GLRenderer.getTexture( texture.getPath() ) ;
					if( glTexture == null )
					{
						return null ;
					}

					map.set( uniform.getRight(), glTexture ) ;
					break ;
				}
				case FONT         :
				{
					final MalletFont font = ( MalletFont )map.get( uniform.getRight() ) ;
					final GLFont glFont = GLRenderer.getFont( font ) ;

					final GLImage texture = glFont.getTexture() ;
					if( texture == null )
					{
						return null ;
					}

					map.set( uniform.getRight(), texture ) ;
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
					final Matrix4 m = ( Matrix4 )_data.get( uniform.getRight() ) ;
					final IFloatBuffer ibuffer = m.matrix ;
					final com.linxonline.mallet.util.buffers.android.FloatBuffer buffer = ( com.linxonline.mallet.util.buffers.android.FloatBuffer )ibuffer ;
					final float[] matrix = buffer.getArray() ;

					GLES30.glUniformMatrix4fv( inUniforms[i], 1, true, matrix, 0 ) ;
					break ;
				}
				case SAMPLER2D    :
				{
					final GLImage texture = ( GLImage )_data.get( uniform.getRight() ) ;

					GLES30.glActiveTexture( GLES30.GL_TEXTURE0 + textureUnit ) ;
					GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, texture.textureIDs[0] ) ;
					textureUnit += 1 ;
					break ;
				}
				case FONT         :
				{
					final GLImage texture = ( GLImage )_data.get( uniform.getRight() ) ;

					GLES30.glActiveTexture( GLES30.GL_TEXTURE0 + textureUnit ) ;			//GLRenderer.handleError( "Activate Texture", _gl ) ;
					GLES30.glBindTexture( GLES30.GL_TEXTURE_2D, texture.textureIDs[0] ) ;	//GLRenderer.handleError( "Bind Texture", _gl ) ;
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

	public static void delete( final GLProgram _program )
	{
		// During the build process a programs 
		// shaders list has already been detached 
		// and destroyed.
		GLES30.glDeleteProgram( _program.id[0] ) ;
	}

	public static GLProgram build( final JSONProgram _program )
	{
		final GLProgram program = new GLProgram( _program ) ;

		program.id[0] = GLES30.glCreateProgram() ;
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
					GLES30.glAttachShader( program.id[0], shaderIDs[i] ) ;
				}
			}

			shaders.clear() ;
		}

		{
			final List<String> swivel = _program.getSwivel() ;
			final int size = swivel.size() ;

			for( int i = 0; i < size; i++ )
			{
				GLES30.glBindAttribLocation( program.id[0], program.inAttributes[i], swivel.get( i ) ) ;
			}
		}

		GLES30.glLinkProgram( program.id[0] ) ;

		program.inMVPMatrix = GLES30.glGetUniformLocation( program.id[0], "inMVPMatrix" ) ;

		// Once all of the shaders have been compiled 
		// and linked, we can then detach the shader sources
		// and delete the shaders from memory.
		for( int i = 0; i < shaderIDs.length; i++ )
		{
			GLES30.glDetachShader( program.id[0], shaderIDs[i] ) ;
			GLES30.glDeleteShader( shaderIDs[i] ) ;
		}

		final int[] response = new int[]{ 0 } ;
		GLES30.glGetProgramiv( program.id[0], GLES30.GL_LINK_STATUS, response, 0 ) ;
		if( response[0] == GLES30.GL_FALSE )
		{
			System.out.println( "Error linking program: " + GLES30.glGetProgramInfoLog( program.id[0] ) ) ;
			GLProgram.delete( program ) ;
			return null ;
		}

		return program ;
	}

	private static boolean compileShader( final JSONProgram.ShaderMap _map, final int _index, final int[] _shaderIDs )
	{
		_shaderIDs[_index] = GLES30.glCreateShader( convertType( _map.getLeft() ) ) ;
		GLES30.glShaderSource( _shaderIDs[_index], _map.getRight() ) ;
		GLES30.glCompileShader( _shaderIDs[_index] ) ;

		final int[] response = new int[]{ 0 } ;
		GLES30.glGetShaderiv( _shaderIDs[_index], GLES30.GL_COMPILE_STATUS, response, 0 ) ;
		if( response[0] == GLES30.GL_FALSE )
		{
			System.out.println( "Error compiling shader: " + GLES30.glGetShaderInfoLog( _shaderIDs[_index] ) ) ;
			return false ;
		}

		return true ;
	}

	private static int convertType( final JSONProgram.Type _type )
	{
		switch( _type )
		{
			case VERTEX   : return GLES30.GL_VERTEX_SHADER ;
			case FRAGMENT : return GLES30.GL_FRAGMENT_SHADER ;
			//case GEOMETRY : return GLES30.GL_GEOMETRY_SHADER ;
			//case COMPUTE  : return GLES30.GL_COMPUTE_SHADER ;
			default       : return -1 ;
		}
	}
}
