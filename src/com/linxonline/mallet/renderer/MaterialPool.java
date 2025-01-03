package com.linxonline.mallet.renderer ;

import java.util.Map ;

import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

import com.linxonline.mallet.util.caches.GeneratePool ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.Texture ;
import com.linxonline.mallet.renderer.TextureArray ;

/**
	Used to create a resource based on a material object.
	The resource is typically a Program but does not need
	to be, the intended purpose is to allow a developer
	to define their own material format and handle it for
	their specific use-case.
*/
public final class MaterialPool<T> extends GeneratePool<T>
{
	public MaterialPool( Map<String, IGenerator<T>> _generators )
	{
		super( _generators, ".mat", ".MAT" ) ;
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
			program.mapUniform( "inTex0", new Texture( path ) ) ;

			return program ;
		}
	}

	/**
		Create a Program where the material specifies the texture.
		This will use the SIMPLE_TEXTURE program.
	*/
	public static class SimpleArrayGenerator implements IGenerator<Program>
	{
		@Override
		public Program generate( final String _materialPath, final JObject _obj )
		{
			final JArray jTextures = _obj.optJArray( "textures", JArray.construct() ) ;

			final int size = jTextures.length() ;
			final String[] paths = new String[size] ;

			for( int i = 0; i < size; ++i )
			{
				paths[i] = jTextures.getString( i ) ;
			}

			final Program program = ProgramAssist.add( new Program( "SIMPLE_ARRAY_TEXTURE" ) ) ;
			program.mapUniform( "inTex0", TextureArray.create( paths ) ) ;

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
			program.mapUniform( "inTex0", new Texture( path ) ) ;

			return program ;
		}
	}
}
