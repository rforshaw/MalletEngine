package com.linxonline.mallet.renderer ;

/**
	DrawAssist provides a set of functions to modify a Draw object
	in the rendering-system in an agnostic way.

	These functions will call into the active renderer which knows 
	how its implementation of the Draw object is formed.

	Allowing Draw data to be optimised for the active renderer.
	DrawAssist calls should be thread-safe.
*/
public final class DrawAssist
{
	private static Assist assist ;

	private DrawAssist() {}

	/**
		Called by current active Renderer.
		If swapping renderers all previous Draw objects will be invalid.
	*/
	public static void setAssist( final DrawAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static DrawUpdater add( final DrawUpdater _updater )
	{
		return assist.add( _updater ) ;
	}

	public static DrawUpdater remove( final DrawUpdater _updater )
	{
		return assist.remove( _updater ) ;
	}

	public static DrawInstancedUpdater add( final DrawInstancedUpdater _updater )
	{
		return assist.add( _updater ) ;
	}

	public static DrawInstancedUpdater remove( final DrawInstancedUpdater _updater )
	{
		return assist.remove( _updater ) ;
	}

	public static TextUpdater add( final TextUpdater _updater )
	{
		return assist.add( _updater ) ;
	}

	public static TextUpdater remove( final TextUpdater _updater )
	{
		return assist.remove( _updater ) ;
	}

	public static <T extends ABuffer> T add( final T _buffer )
	{
		return assist.add( _buffer ) ;
	}

	public static <T extends ABuffer> T remove( final T _buffer )
	{
		return assist.remove( _buffer ) ;
	}

	public static <T extends ABuffer> T update( final T _buffer )
	{
		return assist.update( _buffer ) ;
	}

	/**
		Required to be implemented by the active renderer for 
		DrawAssist to be used.
	*/
	public interface Assist
	{
		public DrawUpdater add( final DrawUpdater _updater ) ;
		public DrawUpdater remove( final DrawUpdater _updater ) ;

		public DrawInstancedUpdater add( final DrawInstancedUpdater _updater ) ;
		public DrawInstancedUpdater remove( final DrawInstancedUpdater _updater ) ;

		public TextUpdater add( final TextUpdater _updater ) ;
		public TextUpdater remove( final TextUpdater _updater ) ;

		public <T extends ABuffer> T add( final T _buffer ) ;
		public <T extends ABuffer> T remove( final T _buffer ) ;
		public <T extends ABuffer> T update( final T _buffer ) ;

		/*public GeometryBuffer add( final GeometryBuffer _buffer ) ;
		public GeometryBuffer remove( final GeometryBuffer _buffer ) ;
		public GeometryBuffer update( final GeometryBuffer _buffer ) ;

		public DrawBuffer add( final DrawBuffer _buffer ) ;
		public DrawBuffer remove( final DrawBuffer _buffer ) ;
		public DrawBuffer update( final DrawBuffer _buffer ) ;

		public TextBuffer add( final TextBuffer _buffer ) ;
		public TextBuffer remove( final TextBuffer _buffer ) ;
		public TextBuffer update( final TextBuffer _buffer ) ;

		public DrawInstancedBuffer add( final DrawInstancedBuffer _buffer ) ;
		public DrawInstancedBuffer remove( final DrawInstancedBuffer _buffer ) ;
		public DrawInstancedBuffer update( final DrawInstancedBuffer _buffer ) ;*/
	}
}
