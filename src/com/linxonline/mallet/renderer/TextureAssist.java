package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;

/**
	All renderers should call setAssist and implement 
	a texture meta data generator.
	This class provides meta information about the 
	texture denoted by _path.
*/
public final class TextureAssist
{
	private static Assist assist ;			// Platform/Render specific implementation to retreive image meta information

	private TextureAssist() {}

	public static void setAssist( final TextureAssist.Assist _assist )
	{
		assist = _assist ;
	}

	/**
		Return the meta information associated to the 
		texture defined at file _path.
	*/
	public static MalletTexture.Meta createMeta( final String _path )
	{
		return assist.createMeta( _path ) ;
	}

	/**
		Using the passed in World generate appropriate 
		meta information that represents its framebuffer.
	*/
	public static MalletTexture.Meta createMeta( final World _world )
	{
		return assist.createMeta( _world ) ;
	}

	/**
		Return the maximum width and height texture 
		supported by the active rendering system.
	*/
	public Vector2 getMaximumTextureSize()
	{
		return assist.getMaximumTextureSize() ;
	}

	public static interface Assist
	{
		public MalletTexture.Meta createMeta( final String _path ) ;
		public MalletTexture.Meta createMeta( final World _world ) ;

		public Vector2 getMaximumTextureSize() ;
	}
}
