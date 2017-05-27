package com.linxonline.mallet.renderer ;

public final class WorldAssist
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

	/**
		Add the world to the rendering system so it 
		can be rendered out.
		A world should not be added multiple times.
	*/
	public static World addWorld( final World _world )
	{
		return assist.addWorld( _world ) ;
	}

	/**
		Remove the World from being rendered out.
		Removing the world does not destroy any 
		resources it may have.
	*/
	public static World removeWorld( final World _world )
	{
		return assist.removeWorld( _world ) ;
	}

	/**
		Destory world will remove any resources assigned.
		Calling destroy world will also remove the world 
		from being rendered.
		The world may not be destroyed instantly.
	*/
	public static void destroyWorld( final World _world )
	{
		assist.destroyWorld( _world ) ;
	}

	public static World setRenderDimensions( final World _world, final int _x, final int _y, final int _width, final int _height )
	{
		return assist.setRenderDimensions( _world, _x, _y, _width, _height ) ;
	}

	public static World setDisplayDimensions( final World _world, final int _x, final int _y, final int _width, final int _height )
	{
		return assist.setDisplayDimensions( _world, _x, _y, _width, _height ) ;
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
		public void destroyWorld( final World _world ) ;

		public World setRenderDimensions( final World _world, final int _x, final int _y, final int _width, final int _height ) ;
		public World setDisplayDimensions( final World _world, final int _x, final int _y, final int _width, final int _height ) ;

		/**
			Construct a world with the specified identifier 
			and what order it has when rendering.
		*/
		public World constructWorld( final String _id, final int _order ) ;
	}
}
