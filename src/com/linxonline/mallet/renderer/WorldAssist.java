package com.linxonline.mallet.renderer ;

public class WorldAssist
{
	private static Assist assist ;

	private WorldAssist() {}

	public static void setAssist( final WorldAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static World getDefaultWorld()
	{
		return assist.getDefaultWorld() ;
	}

	public static World addWorld( final World _world )
	{
		return assist.addWorld( _world ) ;
	}

	public static World removeWorld( final World _world )
	{
		return assist.removeWorld( _world ) ;
	}

	public static World constructWorld( final String _id, final int _order )
	{
		return assist.constructWorld( _id, _order ) ;
	}

	public interface Assist
	{
		public World getDefaultWorld() ;

		public World addWorld( final World _world ) ;
		public World removeWorld( final World _world ) ;

		public World constructWorld( final String _id, final int _order ) ;
	}
}