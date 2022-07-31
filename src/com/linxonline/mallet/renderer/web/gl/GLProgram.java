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
	public final WebGLProgram[] id = new WebGLProgram[1] ;	// GL Program ID

	// Model View Projection Matrix, doesn't need to be defined in *.jgl,
	// however it must be defined in atleast vertex shader.
	public WebGLUniformLocation inModelMatrix = null ;
	public WebGLUniformLocation inMVPMatrix = null ;
	public final WebGLUniformLocation[] inUniforms ;	// Additional uniforms defined in *.jgl and shaders  
	public final int[] inAttributes ;					// Vertex swivel order defined in *.jgl

	private GLProgram( final JSONProgram _program )
	{
		program = _program ;
		inUniforms   = new WebGLUniformLocation[program.getUniforms().size()] ;
		inAttributes = new int[program.getAttribute().size()] ;

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
	public boolean remap( final Program _map, final Program _remap )
	{
		_remap.copy( _map ) ;

		final List<JSONProgram.UniformMap> uniforms = program.getUniforms() ;
		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
			switch( uniform.getLeft() )
			{
				case INT32        :
				case UINT32       :
				case FLOAT32      :
				case FLOAT64      :
				case FLOAT32_VEC2 :
				case FLOAT32_VEC3 :
				case FLOAT32_VEC4 :
				{
					Logger.println( "Build uniform type not implemented: " + uniform.getLeft(), Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case BOOL         :
				case FLOAT32_MAT4 : break ;
				case SAMPLER2D    :
				{
					final MalletTexture texture = ( MalletTexture )_remap.getUniform( uniform.getRight() ) ;
					if( texture == null )
					{
						Logger.println( "Requires texture: " + uniform.getRight(), Logger.Verbosity.MAJOR ) ;
					}

					final GLImage glTexture = GLRenderer.getTexture( texture ) ;
					if( glTexture == null )
					{
						return false ;
					}

					_remap.mapUniform( uniform.getRight(), new Texture( glTexture, texture ) ) ;
					break ;
				}
				case FONT         :
				{
					final MalletFont font = ( MalletFont )_remap.getUniform( uniform.getRight() ) ;
					final GLFont glFont = GLRenderer.getFont( font ) ;
					final GLImage texture = glFont.getTexture() ;

					_remap.mapUniform( uniform.getRight(), texture ) ;
					break ;
				}
				case UNKNOWN      :
				default           : return false ;
			}
		}

		return true ;
	}

	/**
		A GL Program will have information that it requires 
		before it can be used effectively.
		This information can be loaded in via uniforms.
		The jgl file defined what uniforms our shader program wants 
		and the order in-which we should receive them in.
	*/
	public boolean loadUniforms( final Program _data )
	{
		final List<JSONProgram.UniformMap> uniforms = program.getUniforms() ;
		int textureUnit = 0 ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
			switch( uniform.getLeft() )
			{
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
				case BOOL         :
				{
					Logger.println( uniform.getRight(), Logger.Verbosity.MAJOR ) ;
					final boolean val = ( Boolean )_data.getUniform( uniform.getRight() ) ;
					MGL.uniform1i( inUniforms[i], val ? 1 : 0) ;
					break ;
				}
				case FLOAT32_MAT4 :
				{
					final Matrix4 m = ( Matrix4 )_data.getUniform( uniform.getRight() ) ;
					final float[] matrix = m.matrix ;

					MGL.uniformMatrix4fv( inUniforms[i], true, matrix ) ;
					break ;
				}
				case SAMPLER2D    :
				{
					final Texture texture = ( Texture )_data.getUniform( uniform.getRight() ) ;
					final GLImage image = texture.image ;

					MGL.uniform1i( inUniforms[i], textureUnit ) ;

					MGL.activeTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.bindTexture( MGL.GL_TEXTURE_2D, image.textureIDs[0] ) ;

					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, texture.uWrap ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, texture.vWrap ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, texture.magFilter ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, texture.minFilter ) ;

					textureUnit += 1 ;
					break ;
				}
				case FONT         :
				{
					final GLImage texture = ( GLImage )_data.getUniform( uniform.getRight() ) ;

					MGL.uniform1i( inUniforms[i], textureUnit ) ;

					MGL.activeTexture( MGL.GL_TEXTURE0 + textureUnit ) ;
					MGL.bindTexture( MGL.GL_TEXTURE_2D, texture.textureIDs[0] ) ;

					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, MGL.GL_CLAMP_TO_EDGE ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, MGL.GL_REPEAT ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, MGL.GL_LINEAR ) ;
					MGL.texParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, MGL.GL_LINEAR ) ;

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
		A GL Program will have information that it requires 
		before it can be used effectively.
		This information can be loaded in via storage buffers.
		The jgl file defined what buffers our shader program wants 
		and the order in-which we should receive them in.
	*/
	public void bindBuffers( final Program _data, final AssetLookup<Storage, GLStorage> _storages  )
	{
		final List<String> buffers = program.getBuffers() ;

		/*final int size = buffers.size() ;
		for( int i = 0; i < size; ++i )
		{
			final String name = buffers.get( i ) ;
			final Storage storage = _data.getStorage( name ) ;
			if( storage == null )
			{
				System.out.println( "Failed to find storage buffer, skipping..." ) ;
				continue ;
			}

			final GLStorage glStorage = _storages.getRHS( storage.index() ) ;
			//System.out.println( name + " BindBase: " + i + " StorageID: " + glStorage.id[0] ) ;
			MGL.bindBufferBase( MGL.GL_SHADER_STORAGE_BUFFER, i, glStorage.id[0] ) ;
		}*/
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
			final List<JSONProgram.UniformMap> uniforms = _program.getUniforms() ;
			int textureUnit = 0 ;

			final int size = uniforms.size() ;
			for( int i = 0; i < size; i++ )
			{
				final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
				switch( uniform.getLeft() )
				{
					case FONT         :
					case SAMPLER2D    :
					{
						program.inUniforms[i] = MGL.getUniformLocation( program.id[0], uniform.getRight() ) ;
						textureUnit += 1 ;
						break ;
					}
				}
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
			//System.out.println( "Error compiling shader: " + _shader.file + "\n" + log ) ;
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

	private static class Texture
	{
		public GLImage image ;
		public int minFilter ;
		public int magFilter ;
		public int uWrap ;
		public int vWrap ;

		public Texture( GLImage _image, final MalletTexture _texture )
		{
			set( _image, _texture ) ;
		}

		public void set( GLImage _image, final MalletTexture _texture )
		{
			image = _image ;
			minFilter = calculateMinFilter( _texture.getMinificationFilter() ) ;
			magFilter = calculateMagFilter( _texture.getMaxificationFilter() ) ;

			uWrap = calculateWrap( _texture.getUWrap() ) ;
			vWrap = calculateWrap( _texture.getVWrap() ) ;
		}

		private int calculateMagFilter( MalletTexture.Filter _filter )
		{
			switch( _filter )
			{
				default          : return MGL.GL_LINEAR ;
				case LINEAR      : return MGL.GL_LINEAR ;
				case NEAREST     : return MGL.GL_NEAREST ;
			}
		}

		private int calculateMinFilter( MalletTexture.Filter _filter )
		{
			switch( _filter )
			{
				default          : return MGL.GL_LINEAR ;
				case MIP_LINEAR  : return MGL.GL_LINEAR_MIPMAP_LINEAR ;
				case MIP_NEAREST : return MGL.GL_NEAREST_MIPMAP_NEAREST ;
				case LINEAR      : return MGL.GL_LINEAR ;
				case NEAREST     : return MGL.GL_NEAREST ;
			}
		}

		private int calculateWrap( MalletTexture.Wrap _wrap )
		{
			switch( _wrap )
			{
				default         :
				case REPEAT     : return MGL.GL_REPEAT ;
				case CLAMP_EDGE : return MGL.GL_CLAMP_TO_EDGE ;
			}
		}
	}
}
