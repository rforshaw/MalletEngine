package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.event.Event ;

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
	private static final Event<Object> DRAW_CLEAN = Event.create( "DRAW_CLEAN" ) ; 

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

	/**
		Request the active rendering system to clean-up any 
		unused resources it may still be referencing.
	*/
	public static Event<Object> constructDrawClean()
	{
		return DRAW_CLEAN ;
	}

	public static <T extends IUpdater<? extends ABuffer>> T add( final T _updater )
	{
		return assist.add( _updater ) ;
	}

	public static <T extends IUpdater<? extends ABuffer>> T remove( final T _updater )
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
		public <T extends IUpdater<? extends ABuffer>> T add( final T _updater ) ;
		public <T extends IUpdater<? extends ABuffer>> T remove( final T _updater ) ;

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
