package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.IUniform ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.* ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public final class JSONProgram
{
	private final String name ;
	private final List<ShaderMap> shaders = MalletList.<ShaderMap>newList() ;
	private final List<Uniform> uniforms = MalletList.<Uniform>newList() ;
	private final List<Uniform> drawUniforms = MalletList.<Uniform>newList() ;
	private final List<String> buffers = MalletList.<String>newList() ;
	private final List<String> swivel = MalletList.<String>newList() ;

	public JSONProgram( final String _name )
	{
		name = _name ;
	}

	public String getName()
	{
		return name ;
	}

	public List<ShaderMap> getShaders()
	{
		return shaders ;
	}

	public List<Uniform> getUniforms()
	{
		return uniforms ;
	}

	public List<Uniform> getDrawUniforms()
	{
		return drawUniforms ;
	}

	public List<String> getBuffers()
	{
		return buffers ;
	}

	public List<String> getAttribute()
	{
		return swivel ;
	}

	/**
		Used with a ProgramMap created from ProgramAssist.
		Determine if any of the uniforms data are using resources.
	*/
	public void getUsedResources( final Set<String> _activeKeys, final Program _program )
	{
		_activeKeys.add( getName() ) ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONProgram.Uniform uniform = uniforms.get( i ) ;
			switch( uniform.getType() )
			{
				case SAMPLER2D :
				{
					final MalletTexture texture = ( MalletTexture )_program.getUniform( uniform.getName() ) ;
					_activeKeys.add( texture.getPath() ) ;
					break ;
				}
				case FONT      :
				{
					final MalletFont font = ( MalletFont )_program.getUniform( uniform.getName() ) ;
					_activeKeys.add( font.getID() ) ;
					break ;
				}
				default        : break ;
			}
		}
	}

	@Override
	public String toString()
	{
		return name ;
	}

	public static boolean isLoadable( final String _file )
	{
		return GlobalFileSystem.isExtension( _file, ".jgl", ".JGL" ) ;
	}

	public static void load( final String _file, final Delegate _delegate )
	{
		final FileStream stream = GlobalFileSystem.getFile( _file ) ;
		if( stream.exists() == false )
		{
			Logger.println( "Unable to find: " + _file, Logger.Verbosity.MAJOR ) ;
			_delegate.failed() ;
		}

		JObject.construct( stream, new JObject.ConstructCallback()
		{
			public void callback( final JObject _obj )
			{
				generate( _obj, _delegate ) ;
			}
		} ) ;
	}

	private static void generate( final JObject _jGL, final Delegate _delegate )
	{
		final JSONProgram program = new JSONProgram( _jGL.optString( "NAME", "undefined" ) ) ;

		fillShaderPaths( program, _jGL.getJArray( "VERTEX" ), Type.VERTEX ) ;
		fillShaderPaths( program, _jGL.getJArray( "FRAGMENT" ), Type.FRAGMENT ) ;
		fillShaderPaths( program, _jGL.getJArray( "GEOMETRY" ), Type.GEOMETRY ) ;
		fillShaderPaths( program, _jGL.getJArray( "COMPUTE" ),  Type.COMPUTE ) ;

		if( program.shaders.isEmpty() )
		{
			Logger.println( "Program has no shaders specified.", Logger.Verbosity.MAJOR ) ;
			_delegate.failed() ;
			return ;
		}

		fillUniforms( program.uniforms, _jGL.getJArray( "UNIFORMS" ) ) ;
		fillUniforms( program.drawUniforms, _jGL.getJArray( "DRAW_UNIFORMS" ) ) ;
		fillAttributes( program.swivel, _jGL.getJArray( "ATTRIBUTES" ) ) ;
		fillBuffers( program.buffers, _jGL.getJArray( "BUFFERS" ) ) ;

		_delegate.loaded( program ) ;
	}

	private static void fillShaderPaths( final JSONProgram _program, final JArray _base, final Type _type )
	{
		if( _base == null )
		{
			return ;
		}

		final int length = _base.length() ;
		if( length <= 0 )
		{
			return ;
		}

		final StringBuilder source = new StringBuilder() ;

		for( int i = 0; i < length; i++ )
		{
			final String path = _base.getString( i ) ;
			
			final FileStream stream = GlobalFileSystem.getFile( path ) ;
			if( stream.exists() == false )
			{
				Logger.println( "Unable to find: " + path, Logger.Verbosity.MAJOR ) ;
				continue ;
			}

			try( final StringInStream in = stream.getStringInStream() )
			{
				String line = in.readLine() ;
				while( line != null )
				{
					source.append( line ) ;
					source.append( '\n' ) ;
					line = in.readLine() ;
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace() ;
			}
		}

		_program.shaders.add( new ShaderMap( _type, source.toString() ) ) ;
	}

	private static void fillUniforms( final List<Uniform> _toFill, final JArray _base )
	{
		if( _base == null )
		{
			return ;
		}

		final int length = _base.length() ;
		for( int i = 0; i < length; i++ )
		{
			final JObject jUniform = _base.getJObject( i ) ;

			final Uniform uniform = new Uniform( jUniform ) ;
			if( uniform.isValid() )
			{
				_toFill.add( uniform ) ;
			}
		}
	}

	private static void fillAttributes( final List<String> _toFill, final JArray _base )
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

	private static void fillBuffers( final List<String> _toFill, final JArray _base )
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

	public static class ShaderMap extends Tuple<Type, String>
	{
		public ShaderMap( final Type _type, final String _source )
		{
			super( _type, _source ) ;
		}
	}

	public static class Uniform
	{
		private final String name ;
		private final String absoluteName ;
		private final IUniform.Type type ;

		private final Uniform[] values ;
		private final Uniform arrayCurrentSize ;	// Used to denote what values within the array are valid.

		/**
			Intended for a uniforms specified at
			the base of a shader.
		*/
		public Uniform( JObject _jUniform )
		{
			this( "", _jUniform ) ;
		}

		private Uniform( final String prefix, JObject _jUniform )
		{
			name = _jUniform.optString( "NAME", "" ) ;
			absoluteName = String.format( "%s%s", prefix, name ) ;
			type = IUniform.Type.convert( _jUniform.optString( "TYPE", null ) ) ;

			switch( type )
			{
				default     :
				{
					values = null ;
					arrayCurrentSize = null ;
					break ;
				}
				case STRUCT :
				{
					arrayCurrentSize = null ;

					final JArray jStruct = _jUniform.getJArray( "STRUCT" ) ;
					final int length = jStruct.length() ;

					values = new Uniform[length] ;
					final String structPrefix = String.format( "%s.", absoluteName ) ;

					for( int i = 0; i < length; i++ )
					{
						final Uniform uniform = new Uniform( structPrefix, jStruct.getJObject( i ) ) ;
						if( uniform.isValid() )
						{
							values[i] = uniform ;
						}
					}
					break ;
				}
				case ARRAY :
				{
					final int size = _jUniform.optInt( "ARRAY_MAX_SIZE", 0 ) ;
					values = new Uniform[size] ;

					// The defines the data stored within the array.
					final JObject jStored = _jUniform.getJObject( "STORED_UNIFORM" ) ;

					// Construct the uniform to define what data the
					// array stores, this is mostly important for structs.
					final Uniform uniform = new Uniform( jStored ) ;
					switch( uniform.getType() )
					{
						case STRUCT :
						{
							for( int i = 0; i < size; ++i )
							{
								// arrayPrefix should looks something like: array_name[0].
								// Note the '.' which 
								final String arrayPrefix = String.format( "%s[%d].", absoluteName, i ) ;
								values[i] = new Uniform( arrayPrefix, jStored ) ;
							}
							break ;
						}
						default     :
						{
							for( int i = 0; i < size; ++i )
							{
								final String arrayPrefix = String.format( "%s[%d]", absoluteName, i ) ;
								values[i] = new Uniform( arrayPrefix, uniform.getType() ) ;
							}
							break ;
						}
					}

					// The uniform used by the shader to state how much
					// of the array is actually being used.
					arrayCurrentSize = new Uniform( _jUniform.getJObject( "ARRAY_CURRENT_SIZE" ) ) ;
					break ;
				}
			}
		}

		/**
			Intended for uniforms within a primitive array.
			The prefix passed in should looks something
			like: array_name[0]
		*/
		private Uniform( final String _prefix, final IUniform.Type _type )
		{
			values = null ;
			arrayCurrentSize = null ;

			name = "" ;
			absoluteName = _prefix ;
			type = _type ;
		}

		/**
			Return the shader path required to set
			this uniform.
		*/
		public String getName()
		{
			return name ;
		}

		public String getAbsoluteName()
		{
			return name ;
		}

		/**
			Return the uniform specified at the index
			location for either an array or a struct.
		*/
		public Uniform get( final int _index )
		{
			return values[_index] ;
		}

		/**
			Return the number of uniforms within the array
			or within a struct.
		*/
		public int length()
		{
			return values.length ;
		}

		public IUniform.Type getType()
		{
			return type ;
		}

		public boolean isValid()
		{
			return type != IUniform.Type.UNKNOWN ;
		}
	}

	public enum Type
	{
		VERTEX,
		GEOMETRY,
		FRAGMENT,
		COMPUTE,
		UNKNOWN
	}

	public interface Delegate
	{
		public void loaded( final JSONProgram _program ) ;
		public void failed() ;
	}
}
