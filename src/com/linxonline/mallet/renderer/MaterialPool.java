package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.Map ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.MalletTexture ;

/**
	Used to create a resource based on a material object.
	The resource is typically a Program but does not need
	to be, the intended purpose is to allow a developer
	to define their own material format and handle it for
	their specific use-case.
*/
public class MaterialPool<T>
{
	private final Map<String, IGenerator<T>> creators = MalletMap.<String, IGenerator<T>>newMap() ;
	private final Map<String, T> materials = MalletMap.<String, T>newMap() ;

	public MaterialPool( Map<String, IGenerator<T>> _generators )
	{
		creators.putAll( _generators ) ;
	}

	public T create( final String _path )
	{
		T material = materials.get( _path ) ;
		if( material != null )
		{
			return material ;
		}

		if( GlobalFileSystem.isExtension( _path, ".mat", ".MAT" ) == false )
		{
			Logger.println( "Material: " + _path + " invalid extension.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final FileStream stream = GlobalFileSystem.getFile( _path ) ;
		if( stream.exists() == false )
		{
			Logger.println( "Material: " + _path + " doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		final JObject jMaterial = JObject.construct( stream ) ;
		
		final String type = jMaterial.optString( "type", null ) ;
		final IGenerator<T> generator = creators.get( type ) ;
		if( generator == null )
		{
			Logger.println( "Material: " + type + " unable to find matching generator.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		material = generator.generate( _path, jMaterial ) ;
		if( material != null )
		{
			materials.put( _path, material ) ;
		}

		return material ;
	}

	public interface IGenerator<T>
	{
		public T generate( final String _materialPath, final JObject _obj ) ;
	}

	/**
		Create a Program where the material specifies the texture.
		This will use the SIMPLE_TEXTURE program.
	*/
	public static class SimpleGenerator implements IGenerator<Program>
	{
		@Override
		public Program generate( final String _materialPath, final JObject _obj )
		{
			final String path = _obj.optString( "texture", "" ) ;

			final Program program = ProgramAssist.add( new Program( "SIMPLE_TEXTURE" ) ) ;
			program.mapUniform( "inTex0", new MalletTexture( path ) ) ;

			return program ;
		}
	}

	/**
		Create a Program where the material specifies the texture.
		This will use the SIMPLE_INSTANCE_TEXTURE program.
	*/
	public static class SimpleInstancedGenerator implements IGenerator<Program>
	{
		@Override
		public Program generate( final String _materialPath, final JObject _obj )
		{
			final String path = _obj.optString( "texture", "" ) ;

			final Program program = ProgramAssist.add( new Program( "SIMPLE_INSTANCE_TEXTURE" ) ) ;
			program.mapUniform( "inTex0", new MalletTexture( path ) ) ;

			return program ;
		}
	}
}
