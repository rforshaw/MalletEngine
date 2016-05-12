package com.linxonline.mallet.renderer ;

public class WorldAssist
{
	private static Assist assist ;

	private WorldAssist() {}

	public static void setAssist( final WorldAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static World constructWorld( final String _id, final int _order )
	{
		return assist.constructWorld( _id, _order ) ;
	}

	public interface Assist
	{
		public World constructWorld( final String _id, final int _order ) ;
	}
}