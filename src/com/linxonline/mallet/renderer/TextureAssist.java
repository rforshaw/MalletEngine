package com.linxonline.mallet.renderer ;

public class TextureAssist
{
	private static Assist assist ;

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