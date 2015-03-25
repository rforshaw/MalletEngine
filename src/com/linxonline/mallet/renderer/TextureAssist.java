package com.linxonline.mallet.renderer ;


/**
	All renderers should call setAssist and implement 
	a texture meta data generator.
	This class provides meta information about the 
	texture denoted by _path.
*/
public class TextureAssist
{
	private static Assist assist ;			// Platform/Render specific implementation to retreive image meta information

	private TextureAssist() {}

	
	public static void setAssist( final TextureAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static MalletTexture.Meta createMeta( final String _path )
	{
		return assist.create( _path ) ;
	}

	public static interface Assist
	{
		public MalletTexture.Meta create( final String _path ) ;
	}
}