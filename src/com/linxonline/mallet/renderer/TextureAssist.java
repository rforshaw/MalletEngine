package com.linxonline.mallet.renderer ;

import java.util.Set ;

import com.linxonline.mallet.maths.Vector2 ;

/**
	All renderers should call setAssist and implement 
	a texture meta data generator.
	This class provides meta information about the 
	texture denoted by _path.
*/
public final class TextureAssist
{
	private static Assist assist ;			// Platform/Render specific implementation to retrieve image meta information

	private TextureAssist() {}

	public static void setAssist( final TextureAssist.Assist _assist )
	{
		assist = _assist ;
	}

	/**
		Return the meta information associated to the 
		texture defined at file _path.
	*/
	public static Texture.Meta createMeta( final String _path )
	{
		return assist.createMeta( _path ) ;
	}

	/**
		Return the maximum width and height texture 
		supported by the active rendering system.
	*/
	public static Vector2 getMaximumTextureSize()
	{
		return assist.getMaximumTextureSize() ;
	}

	public static boolean texturesLoaded()
	{
		return assist.texturesLoaded() ;
	}

	/**
		Return a list of texture paths that have been loaded.
		Note: This does not include textures currently in
		the process of being loaded. 
	*/
	public static Set<String> getLoadedTextures( final Set<String> _fill )
	{
		return assist.getLoadedTextures( _fill ) ;
	}

	/**
		Return all texture paths loaded and in the process of being loaded.
	*/
	public static Set<String> getAllTextures( final Set<String> _fill )
	{
		return assist.getAllTextures( _fill ) ;
	}

	public static void clean( final Set<String> _active )
	{
		assist.clean( _active ) ;
	}

	public static interface Assist
	{
		public Texture.Meta createMeta( final String _path ) ;

		public Vector2 getMaximumTextureSize() ;

		public boolean texturesLoaded() ;

		public Set<String> getLoadedTextures( final Set<String> _fill ) ;
		public Set<String> getAllTextures( final Set<String> _fill ) ;

		public void clean( final Set<String> _active ) ;
	}
}
