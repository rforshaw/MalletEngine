package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.List ;
import java.nio.charset.StandardCharsets ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.opengl.JSONProgram ;
import com.linxonline.mallet.renderer.opengl.ProgramManager ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Draw ;

import com.linxonline.mallet.renderer.UniformList ;
import com.linxonline.mallet.renderer.IUniform ;
import com.linxonline.mallet.renderer.BoolUniform ;
import com.linxonline.mallet.renderer.FloatUniform ;
import com.linxonline.mallet.renderer.UIntUniform ;
import com.linxonline.mallet.renderer.IntUniform ;

import com.linxonline.mallet.renderer.Font ;
import com.linxonline.mallet.renderer.Texture ;
import com.linxonline.mallet.renderer.TextureArray ;

import com.linxonline.mallet.util.Logger ;

/**
	GLProgram retains a collection of GLSL shaders 
	that are used during the rendering process.
*/
public final class GLProgram extends ProgramManager.Program
{
	public final JSONProgram program ;
	public final int[] id = new int[1] ;	// GL Program ID

	public int inModelMatrix = -1 ;
	public int inViewMatrix = -1 ;
	public int inProjectionMatrix = -1 ;

	public int inResolution = -1 ;

	public Attribute[] inAttributes ;
	public SSBuffer[] inBuffers ;

	private final static UniformBuilder builder = new UniformBuilder() ;

	private GLProgram( final JSONProgram _program )
	{
		program = _program ;
	}

	public SSBuffer getSSBuffer( final String _name )
	{
		for( int i = 0; i < inBuffers.length; ++i )
		{
			final SSBuffer buffer = inBuffers[i] ;
			if( buffer != null && buffer.isName( _name ) )
			{
				return buffer ;
			}
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

		MGL.glLinkProgram( program.id[0] ) ;

		program.inModelMatrix = MGL.glGetUniformLocation( program.id[0], "inModelMatrix" ) ;
		program.inViewMatrix = MGL.glGetUniformLocation( program.id[0], "inViewMatrix" ) ;
		program.inProjectionMatrix = MGL.glGetUniformLocation( program.id[0], "inProjectionMatrix" ) ;
		program.inResolution = MGL.glGetUniformLocation( program.id[0], "inResolution" ) ;

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

		{
			final int[] count = new int[1] ;
			MGL.glGetProgramiv( program.id[0], MGL.GL_ACTIVE_ATTRIBUTES, count, 0 ) ;

			program.inAttributes = new Attribute[count[0]] ;
			final int size = program.inAttributes.length ;
			for( int i = 0; i < size; ++i )
			{
				program.inAttributes[i] = new Attribute( program.id[0], i ) ;
			}
		}

		{
			final int[] count = new int[3] ;

			MGL.glGetProgramInterfaceiv( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, MGL.GL_ACTIVE_RESOURCES, count, 0 ) ;
			MGL.glGetProgramInterfaceiv( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, MGL.GL_MAX_NAME_LENGTH, count, 1 ) ;

			final int ssbCount = count[0] ;
			final int ssbMaxNameLength = count[1] ;

			final byte[] ssbMaxName = new byte[ssbMaxNameLength] ;

			final int[] props = new int[]
			{
				MGL.GL_BUFFER_BINDING
			} ;

			final int[] lengths = new int[props.length] ;
			final int[] params = new int[props.length] ;

			program.inBuffers = new SSBuffer[ssbCount] ;

			for( int i = 0; i < ssbCount; ++i )
			{
				// Find the name of the resource.
				MGL.glGetProgramResourceName​( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, i, ssbMaxNameLength, count, 2, ssbMaxName, 0 ) ;

				final int ssbActualNameLength = count[2] ;
				final byte[] ssbActualName = new byte[ssbActualNameLength] ;

				// Copy the name into a buffer the correct size.
				System.arraycopy( ssbMaxName, 0, ssbActualName, 0, ssbActualNameLength ) ;

				// Grab the index of the resource
				final int index = MGL.glGetProgramResourceIndex( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, ssbActualName, 0 ) ;
				if( index == MGL.GL_INVALID_INDEX )
				{
					System.out.println( "Invalid index for: " + program.inBuffers[i].toString() ) ;
					program.inBuffers[i] = null ;
					continue ;
				}

				// Using the index grab the binding point.
				MGL.glGetProgramResourceiv( program.id[0], MGL.GL_SHADER_STORAGE_BLOCK, index, props.length, props, 0, lengths.length, lengths, 0, params, 0 ) ;

				final int binding = params[0] ;
				program.inBuffers[i] = new SSBuffer( ssbActualName, index, binding ) ;
			}
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
			System.out.println( "Source: " + source[0] ) ;
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

	public static final class SSBuffer
	{
		private final int index ;
		private final int binding ;
		private final String name ;

		public SSBuffer( final byte[] _bName, final int _index, final int _binding )
		{
			name = new String( _bName, 0, _bName.length, StandardCharsets.UTF_8 ) ;
			index = _index ;
			binding = _binding ;
		}

		public boolean isName( final String _name )
		{
			return name.equals( _name ) ;
		}

		public String getName()
		{
			return name ;
		}

		public int getIndex()
		{
			return index ;
		}

		public int getBinding()
		{
			return binding ;
		}

		@Override
		public String toString()
		{
			return name + " : " + index + " : " + binding ;
		}
	}

	public static final class Attribute
	{
		private final int index ;
		private final int location ;

		// 0 - name length, 1 - size, 2 - type
		private final int[] params = new int[3] ;
		private final String name ;

		public Attribute( final int _id, final int _index )
		{
			index = _index ;

			final byte[] bName = new byte[256] ;
			MGL.glGetActiveAttrib( _id, _index, bName.length, params, 0, params, 1, params, 2, bName, 0 ) ;
			name = new String( bName, 0, params[0], StandardCharsets.UTF_8 ) ;

			location = MGL.glGetAttribLocation( _id, name ) ;
		}

		public int getLocation()
		{
			return location ;
		}

		public int getSize()
		{
			return params[1] ;
		}

		public int getType()
		{
			return params[2] ;
		}

		public boolean isName( final String _name )
		{
			return name.equals( _name ) ;
		}

		@Override
		public String toString()
		{
			return name + " Type: " + getType() + " Size: " + getSize() ;
		}
	}

	public static final class UniformState
	{
		public int[] drawUniformLocations = new int[0] ;
		public int textureUnit = 0 ;

		public boolean hasDrawUniforms()
		{
			return drawUniformLocations.length > 0 ;
		}

		public void reset()
		{
			textureUnit = 0 ;
		}
	}

	public interface ILoadUniform
	{
		public boolean load( final UniformState _state ) ;
	}

	private static final class UniformBuilder implements IUniform.IEach
	{
		private int programID = -1 ;
		private List<ILoadUniform> toFill = null ;

		public void set( final int _id, final List<ILoadUniform> _toFill )
		{
			programID = _id ;
			toFill = _toFill ;
		}

		public boolean buildProgramUniforms( final Program _program, final GLProgram _glProgram, final List<ILoadUniform> _toFill )
		{
			programID = _glProgram.id[0] ;
			toFill = _toFill ;

			return _program.forEachUniform( builder ) ;
		}

		private boolean loadUniform( final int _location, final IUniform _uniform )
		{
			return switch( _uniform )
			{
				case BoolUniform val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform1i( _location, val.getState() ? 1 : 0) ;
						return true ;
					} ) ;

					yield true ;
				}
				case UIntUniform val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform1ui( _location, val.getState() ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case IntUniform val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform1i( _location, val.getState() ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case IntVector2 val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform2i( _location, val.x, val.y ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case IntVector3 val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform3i( _location, val.x, val.y, val.z ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case FloatUniform val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform1f( _location, val.getState() ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case Vector2 val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform2f( _location, val.x, val.y ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case Vector3 val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniform3f( _location, val.x, val.y, val.z ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case Matrix4 val ->
				{
					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glUniformMatrix4fv( _location, 1, false, val.matrix, 0 ) ;
						return true ;
					} ) ;

					yield true ;
				}
				case Texture texture ->
				{
					if( texture == null )
					{
						Logger.println( "Requires texture: " + texture.toString(), Logger.Verbosity.MAJOR ) ;
						yield false ;
					}

					final GLImage glTexture = GLRenderer.getTexture( texture ) ;
					if( glTexture == null )
					{
						yield false ;
					}

					final int minFilter = GLImage.calculateMinFilter( texture.getMinificationFilter() ) ;
					final int magFilter = GLImage.calculateMagFilter( texture.getMaxificationFilter() ) ;

					final int uWrap = GLImage.calculateWrap( texture.getUWrap() ) ;
					final int vWrap = GLImage.calculateWrap( texture.getVWrap() ) ;

					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glActiveTexture( MGL.GL_TEXTURE0 + _state.textureUnit ) ;
						MGL.glBindTexture( MGL.GL_TEXTURE_2D, glTexture.textureIDs[0] ) ;
						MGL.glUniform1i( _location, _state.textureUnit ) ;

						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, uWrap ) ;
						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, vWrap ) ;
						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, magFilter ) ;
						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, minFilter ) ;

						_state.textureUnit += 1 ;
						return true ;
					} ) ;

					yield true ;
				}
				case TextureArray texture ->
				{
					if( texture == null )
					{
						Logger.println( "Requires texture array: " + texture.toString(), Logger.Verbosity.MAJOR ) ;
						yield false ;
					}

					final GLImage glTexture = GLRenderer.getTextureArray( texture ) ;
					if( glTexture == null )
					{
						yield false ;
					}

					final int minFilter = GLImage.calculateMinFilter( texture.getMinificationFilter() ) ;
					final int magFilter = GLImage.calculateMagFilter( texture.getMagnificationFilter() ) ;

					final int uWrap = GLImage.calculateWrap( texture.getUWrap() ) ;
					final int vWrap = GLImage.calculateWrap( texture.getVWrap() ) ;

					toFill.add( ( final UniformState _state ) ->
					{
						MGL.glActiveTexture( MGL.GL_TEXTURE0 + _state.textureUnit ) ;
						MGL.glBindTexture( MGL.GL_TEXTURE_2D_ARRAY, glTexture.textureIDs[0] ) ;
						MGL.glUniform1i( _location, _state.textureUnit ) ;

						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_S, uWrap ) ;
						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_WRAP_T, vWrap ) ;
						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MAG_FILTER, magFilter ) ;
						MGL.glTexParameteri( MGL.GL_TEXTURE_2D, MGL.GL_TEXTURE_MIN_FILTER, minFilter ) ;

						_state.textureUnit += 1 ;
						return true ;
					} ) ;

					yield true ;
				}
				case Font font ->
				{
					yield true ;
				}
				default -> false ;
			} ;
		}

		@Override
		public boolean each( final String _absoluteName, final IUniform _uniform )
		{
			final int location = MGL.glGetUniformLocation( programID, _absoluteName ) ;
			return loadUniform( location, _uniform ) ;
		}

		public boolean loadDrawUniforms( final UniformState _state, final Draw _draw )
		{
			final int size = _state.drawUniformLocations.length ;
			for( int i = 0; i < size; ++i )
			{
				final IUniform uniform = _draw.getUniform( i ) ;
				final int location = _state.drawUniformLocations[i] ;
				if( location == -1 )
				{
					// The uniform is not found in this program, but
					// that doesn't mean it's not used in another program.
					continue ;
				}

				if( loadUniform( location, uniform ) == false )
				{
					return false ;
				}
			}

			return true ;
		}
	}
}
