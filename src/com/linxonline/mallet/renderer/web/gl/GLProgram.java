package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLProgram ;
import org.teavm.jso.webgl.WebGLShader ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.webgl.WebGLUniformLocation ;

import com.linxonline.mallet.renderer.opengl.JSONProgram ;
import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Storage ;
import com.linxonline.mallet.renderer.AssetLookup ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletTexture ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public final class GLProgram extends ProgramManager.Program
{
	public final JSONProgram program ;
	public final WebGLProgram[] id = new WebGLProgram[1] ;	// GL Program ID

	// Model View Projection Matrix, doesn't need to be defined in *.jgl,
	// however it must be defined in atleast vertex shader.
	public WebGLUniformLocation inModelMatrix = null ;
	public WebGLUniformLocation inMVPMatrix = null ;
	public final WebGLUniformLocation[] inUniforms ;		// Additional uniforms defined in *.jgl and shaders
	public final WebGLUniformLocation[] inDrawUniforms ;	// Additional uniforms defined in *.jgl and shaders  
	public final int[] inAttributes ;						// Vertex swivel order defined in *.jgl

	private GLProgram( final JSONProgram _program )
	{
		program = _program ;
		inUniforms = new WebGLUniformLocation[program.getUniforms().size()] ;
		inDrawUniforms = new WebGLUniformLocation[program.getDrawUniforms().size()] ;
		inAttributes = new int[program.getAttribute().size()] ;

		final int length = inAttributes.length ;
		for( int i = 0; i < length; i++ )
		{
			inAttributes[i] = i ;
		}

		return null ;
	}

	public Attribute getAttribute( final String _name )
	{
		for( int i = 0; i < inAttributes.length; ++i )
		{
			final Attribute attr = inAttributes[i] ;
			if( attr.isName( _name ) )
			{
				return attr ;
			}
		}

		return null ;
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

	public boolean loadDrawUniforms( final UniformState _state, final Draw _draw )
	{
		return builder.loadDrawUniforms( _state, _draw ) ;
	}

	public boolean buildDrawUniforms( final Program _program, final UniformState _state )
	{
		final UniformList list = _program.getDrawUniforms() ;

		final int size = list.size() ;
		final int[] locations = new int[size] ;

		for( int i = 0; i < size; ++i )
		{
			final String name = list.get( i ) ;
			final int location = MGL.glGetUniformLocation( id[0], name ) ;
			if( location < 0 )
			{
				Logger.println( "Unable to find: " + name + " required as a draw uniform.", Logger.Verbosity.MAJOR ) ;
				return false ;
			}

			locations[i] = location ;
		}

		_state.drawUniformLocations = locations ;
		return true ;
	}

	public boolean buildProgramUniforms( final Program _program, final List<ILoadUniform> _toFill )
	{
		return builder.buildProgramUniforms( _program, this, _toFill ) ;
	}

	/**
		Call to generate a GLProgram.
	*/
	public static GLProgram build( final JSONProgram _program )
	{
		final GLProgram program = new GLProgram( _program ) ;

		program.id[0] = MGL.createProgram() ;
		if( program.id[0] == null )
		{
			System.out.println( "Failed to create program.." ) ;
			return null ;
		}

		final List<JSONProgram.ShaderMap> shaders = _program.getShaders() ;
		final WebGLShader[] shaderIDs = new WebGLShader[shaders.size()] ;

		{
			final int size = shaders.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.ShaderMap shader = shaders.get( i ) ;
				// Attach only successfully compiled shaders
				if( compileShader( shader, i, shaderIDs ) == true )
				{
					MGL.attachShader( program.id[0], shaderIDs[i] ) ;
				}
			}

			shaders.clear() ;
		}

		{
			final List<String> swivel = _program.getAttribute() ;
			final int size = swivel.size() ;

			for( int i = 0; i < size; i++ )
			{
				MGL.bindAttribLocation( program.id[0], program.inAttributes[i], swivel.get( i ) ) ;
			}
		}

		MGL.linkProgram( program.id[0] ) ;

		program.inModelMatrix = MGL.getUniformLocation( program.id[0], "inModelMatrix" ) ;
		program.inMVPMatrix = MGL.getUniformLocation( program.id[0], "inMVPMatrix" ) ;

		{
			final List<JSONProgram.Uniform> uniforms = _program.getUniforms() ;

			final int size = uniforms.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.Uniform uniform = uniforms.get( i ) ;
				program.inUniforms[i] = MGL.getUniformLocation( program.id[0], uniform.getName() ) ;
			}
		}

		{
			final List<JSONProgram.Uniform> uniforms = _program.getDrawUniforms() ;

			final int size = uniforms.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.Uniform uniform = uniforms.get( i ) ;
				program.inDrawUniforms[i] = MGL.getUniformLocation( program.id[0], uniform.getName() ) ;
			}
		}

		// Once all of the shaders have been compiled 
		// and linked, we can then detach the shader sources
		// and delete the shaders from memory.
		for( int i = 0; i < shaderIDs.length; i++ )
		{
			MGL.detachShader( program.id[0], shaderIDs[i] ) ;
			MGL.deleteShader( shaderIDs[i] ) ;
		}

		final boolean response = MGL.getProgramParameterb( program.id[0], MGL.GL_LINK_STATUS ) ;
		if( response == false )
		{
			final String log = MGL.getProgramInfoLog( program.id[0] ) ;
			System.out.println( "Error linking program: " + log ) ;
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
		MGL.deleteProgram( _program.id[0] ) ;
	}

	private static boolean compileShader( final JSONProgram.ShaderMap _map, final int _index, final WebGLShader[] _shaderIDs )
	{
		final JSONProgram.Type type = _map.getLeft() ;
		final String[] source = new String[1] ;
		source[0] = _map.getRight() ;

		final WebGLShader shader = MGL.createShader( convertType( type ) ) ;
		MGL.shaderSource( shader, source[0] ) ;
		MGL.compileShader( shader ) ;

		final boolean response = MGL.getShaderParameterb( shader, MGL.GL_COMPILE_STATUS ) ;
		if( response == false )
		{
			final String log = MGL.getShaderInfoLog( shader ) ;
			System.out.println( "Error compiling shader: \n" + log ) ;
			return false ;
		}

		_shaderIDs[_index] = shader ;
		return true ;
	}

	private static int convertType( final JSONProgram.Type _type )
	{
		switch( _type )
		{
			case VERTEX   : return MGL.GL_VERTEX_SHADER ;
			case FRAGMENT : return MGL.GL_FRAGMENT_SHADER ;
			//case GEOMETRY : return MGL.GL_GEOMETRY_SHADER ;
			//case COMPUTE  : return MGL.GL_COMPUTE_SHADER ;
			default       : return -1 ;
		}
	}
}
