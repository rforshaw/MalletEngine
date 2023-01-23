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
	private final List<UniformMap> uniforms = MalletList.<UniformMap>newList() ;
	private final List<UniformMap> drawUniforms = MalletList.<UniformMap>newList() ;
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

	public List<UniformMap> getUniforms()
	{
		return uniforms ;
	}

	public List<UniformMap> getDrawUniforms()
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
			final JSONProgram.UniformMap uniform = uniforms.get( i ) ;
			switch( uniform.getLeft() )
			{
				case SAMPLER2D :
				{
					final MalletTexture texture = ( MalletTexture )_program.getUniform( uniform.getRight() ) ;
					_activeKeys.add( texture.getPath() ) ;
					break ;
				}
				case FONT      :
				{
					final MalletFont font = ( MalletFont )_program.getUniform( uniform.getRight() ) ;
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

	private static void fillUniforms( final List<UniformMap> _toFill, final JArray _base )
	{
		if( _base == null )
		{
			return ;
		}

		final int length = _base.length() ;
		for( int i = 0; i < length; i++ )
		{
			final JObject obj = _base.getJObject( i ) ;
			final String name = obj.optString( "NAME", null ) ;
			final IUniform.Type type = IUniform.Type.convert( obj.optString( "TYPE", null ) ) ;

			if( name != null && type != IUniform.Type.UNKNOWN )
			{
				_toFill.add( new UniformMap( type, name ) ) ;
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

	public static class UniformMap extends Tuple<IUniform.Type, String>
	{
		public UniformMap( final IUniform.Type _uniform, final String _name )
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

	public interface Delegate
	{
		public void loaded( final JSONProgram _program ) ;
		public void failed() ;
	}
}
