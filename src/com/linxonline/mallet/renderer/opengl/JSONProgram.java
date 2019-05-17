package com.linxonline.mallet.renderer.opengl ;

import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.formats.json.* ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Matrix4 ;

public class JSONProgram
{
	private final String name ;
	private final List<ShaderMap> shaders   = MalletList.<ShaderMap>newList() ;
	private final List<UniformMap> uniforms = MalletList.<UniformMap>newList() ;
	private final List<String> swivel       = MalletList.<String>newList() ;

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

	public List<UniformMap> getUniforms()
	{
		return uniforms ;
	}

	public List<String> getSwivel()
	{
		return swivel ;
	}

	/**
		Used with a ProgramMap created from ProgramAssist.
		Determine if any of the uniforms data are using resources.
	*/
	public void getUsedResources( final Set<String> _activeKeys, final ProgramMap<?> _data )
	{
		_activeKeys.add( getName() ) ;

		final int size = uniforms.size() ;
		for( int i = 0; i < size; i++ )
		{
			final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
			switch( uniform.getLeft() )
			{
				case SAMPLER2D :
				{
					final MalletTexture texture = ( MalletTexture )_data.get( uniform.getRight() ) ;
					_activeKeys.add( texture.getPath() ) ;
					break ;
				}
				case FONT      :
				{
					final MalletFont font = ( MalletFont )_data.get( uniform.getRight() ) ;
					_activeKeys.add( font.getID() ) ;
					break ;
				}
				default        : break ;
			}
		}
	}

	/**
		Determine if the data-instance mapped to a uniform 
		is of the correct type for that uniform.
	*/
	public boolean isUniformsValid( final Map<String, Object> _map )
	{
		final int size = uniforms.size() ;
		final int diff = _map.size() - size ;
		if( diff != 0 )
		{
			if( diff > 0 )
			{
				Logger.println( "Mallet Program contains more uniforms than OpenGL Program expects", Logger.Verbosity.MINOR ) ;
			}
			else if( diff < 0 )
			{
				Logger.println( "Mallet Program contains less uniforms than OpenGL Program expects", Logger.Verbosity.MINOR ) ;
			}
			return false ;
		}

		for( final JSONProgram.UniformMap uniform : uniforms )
		{
			final Object obj = _map.get( uniform.getRight() ) ;
			if( obj == null )
			{
				Logger.println( "OpenGL Program does not contain Map", Logger.Verbosity.MINOR ) ;
				return false ;
			}

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
					Logger.println( "OpenGL uniform type not implemented", Logger.Verbosity.MAJOR ) ;
					return false ;
				}
				case FLOAT32_MAT4 :
				{
					if( isInstance( Matrix4.class, obj ) == false )
					{
						return false ;
					}
					break ;
				}
				case SAMPLER2D    :
				{
					if( isInstance( MalletTexture.class, obj ) == false )
					{
						return false ;
					}
					break ;
				}
				case FONT         :
				{
					if( isInstance( MalletFont.class, obj ) == false )
					{
						return false ;
					}
					break ;
				}
				case UNKNOWN      :
				default           :
				{
					Logger.println( "OpenGL Program does not align to Mallet Program", Logger.Verbosity.MINOR ) ;
					return false ;
				}
			}
		}

		return true ;
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

		JSONObject.construct( stream, new JSONObject.ConstructCallback()
		{
			public void callback( final JSONObject _obj )
			{
				generate( _obj, _delegate ) ;
			}
		} ) ;
	}

	private static void generate( final JSONObject _jGL, final Delegate _delegate )
	{
		final JSONProgram program = new JSONProgram( _jGL.optString( "NAME", "undefined" ) ) ;
		final List<ShaderMap> paths = MalletList.<ShaderMap>newList() ;

		fillShaderPaths( paths, _jGL.getJSONArray( "VERTEX" ), Type.VERTEX ) ;
		fillShaderPaths( paths, _jGL.getJSONArray( "FRAGMENT" ), Type.FRAGMENT ) ;
		fillShaderPaths( paths, _jGL.getJSONArray( "GEOMETRY" ), Type.GEOMETRY ) ;
		fillShaderPaths( paths, _jGL.getJSONArray( "COMPUTE" ),  Type.COMPUTE ) ;

		if( paths.isEmpty() )
		{
			Logger.println( "Program has no shaders specified.", Logger.Verbosity.MAJOR ) ;
			_delegate.failed() ;
			return ;
		}

		fillUniforms( program.uniforms, _jGL.getJSONArray( "UNIFORMS" ) ) ;
		fillAttributes( program.swivel, _jGL.getJSONArray( "SWIVEL" ) ) ;

		readShaders( paths, program, _delegate ) ;
	}

	private static void readShaders( final List<ShaderMap> _paths, final JSONProgram _program, final Delegate _delegate )
	{
		if( _paths.isEmpty() )
		{
			_delegate.loaded( _program ) ;
			return ;
		}

		final ShaderMap map = _paths.remove( 0 ) ;
		final FileStream stream = GlobalFileSystem.getFile( map.getRight() ) ;
		if( stream.exists() == false )
		{
			Logger.println( "Unable to find: " + map.getRight(), Logger.Verbosity.MAJOR ) ;
			readShaders( _paths, _program, _delegate ) ;
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
				_program.shaders.add( new ShaderMap( map.getLeft(), source.toString() ) ) ;
				readShaders( _paths, _program, _delegate ) ;
			}
		}, 1 ) ;
	}

	private static boolean fillShaderPaths( final List<ShaderMap> _toFill, final JSONArray _base, final Type _type )
	{
		if( _base == null )
		{
			return false;
		}

		final int length = _base.length() ;
		if( length <= 0 )
		{
			return false ;
		}

		for( int i = 0; i < length; i++ )
		{
			_toFill.add( new ShaderMap( _type, _base.getString( i ) ) ) ;
		}

		return true ;
	}

	private static void fillUniforms( final List<UniformMap> _toFill, final JSONArray _base )
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
			final Uniform type = Uniform.convert( obj.optString( "TYPE", null ) ) ;

			if( name != null && type != Uniform.UNKNOWN )
			{
				_toFill.add( new UniformMap( type, name ) ) ;
			}
		}
	}

	private static void fillAttributes( final List<String> _toFill, final JSONArray _base )
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

	private static boolean isInstance( final Class _class, final Object _obj )
	{
		return _class.isInstance( _obj ) ;
	}

	public static class ShaderMap extends Tuple<Type, String>
	{
		public ShaderMap( final Type _type, final String _path )
		{
			super( _type, _path ) ;
		}
	}

	public static class UniformMap extends Tuple<Uniform, String>
	{
		public UniformMap( final Uniform _uniform, final String _name )
		{
			super( _uniform, _name ) ;
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

	public enum Uniform
	{
		BOOL,
		INT32,
		UINT32,
		FLOAT32,
		FLOAT64,
		FLOAT32_VEC2,
		FLOAT32_VEC3,
		FLOAT32_VEC4,
		FLOAT32_MAT4,
		SAMPLER2D,
		FONT,
		UNKNOWN ;

		public static Uniform convert( final String _uniform )
		{
			switch( _uniform )
			{
				case "BOOL"         : return Uniform.BOOL ;
				case "INT32"        : return Uniform.INT32 ;
				case "UINT32"       : return Uniform.UINT32 ;
				case "FLOAT32"      : return Uniform.FLOAT32 ;
				case "FLOAT64"      : return Uniform.FLOAT64 ;
				case "FLOAT32_VEC2" : return Uniform.FLOAT32_VEC2 ;
				case "FLOAT32_VEC3" : return Uniform.FLOAT32_VEC3 ;
				case "FLOAT32_VEC4" : return Uniform.FLOAT32_VEC4 ;
				case "FLOAT32_MAT4" : return Uniform.FLOAT32_MAT4 ;
				case "SAMPLER2D"    : return Uniform.SAMPLER2D ;
				case "FONT"         : return Uniform.FONT ;
				default             : return Uniform.UNKNOWN ;
			}
		}
	}

	public interface Delegate
	{
		public void loaded( final JSONProgram _program ) ;
		public void failed() ;
	}
}
