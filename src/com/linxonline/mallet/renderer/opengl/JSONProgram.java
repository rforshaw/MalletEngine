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

	public static class ShaderMap extends Tuple<Type, String>
	{
		public ShaderMap( final Type _type, final String _source )
		{
			super( _type, _source ) ;
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
