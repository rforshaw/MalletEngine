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
	private static final Event<Object> DRAW_CLEAN = new Event<Object>( "DRAW_CLEAN", null ) ; 

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
		Request a DrawDelegate from the active rendering system.
		The DrawDelegate allows the user to add/remove Draw objects
		from being rendered.

		A DrawDelegate is not required for constructing a Draw object, 
		but is required for displaying it.
	*/
	public static Event<DrawDelegateCallback> constructDrawDelegate( final DrawDelegateCallback _callback )
	{
		return new Event<DrawDelegateCallback>( "DRAW_DELEGATE", _callback ) ;
	}

	/**
		Request the active rendering system to clean-up any 
		unused resources it may still be referencing.
	*/
	public static Event<Object> constructDrawClean()
	{
		return DRAW_CLEAN ;
	}

	public static Draw amendUI( final Draw _draw, final boolean _ui )
	{
		return assist.amendUI( _draw, _ui ) ;
	}

	public static Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation )
	{
		return assist.amendInterpolation( _draw, _interpolation ) ;
	}

	public static Draw amendUpdateType( final Draw _draw, final UpdateType _type )
	{
		return assist.amendUpdateType( _draw, _type ) ;
	}

	public static Draw forceUpdate( final Draw _draw )
	{
		return assist.forceUpdate( _draw ) ;
	}

	public static boolean isUI( final Draw _draw )
	{
		return assist.isUI( _draw ) ;
	}

	/**
		Create a Text Draw object.
		Handles the nuances of text rendering and ensures a performant display.
	*/
	public static TextDraw createTextDraw( final StringBuilder _text,
										   final MalletFont _font,
										   final Vector3 _position,
										   final Vector3 _offset,
										   final Vector3 _rotation,
										   final Vector3 _scale,
										   final int _order )
	{
		return assist.createTextDraw( _text, _font, _position, _offset, _rotation, _scale, _order ) ;
	}

	/**
		Create a Text Draw object.
		Handles the nuances of text rendering and ensures a performant display.
	*/
	public static TextDraw createTextDraw( final String _text,
										   final MalletFont _font,
										   final Vector3 _position,
										   final Vector3 _offset,
										   final Vector3 _rotation,
										   final Vector3 _scale,
										   final int _order )
	{
		return assist.createTextDraw( _text, _font, _position, _offset, _rotation, _scale, _order ) ;
	}

	public static Draw createClipDraw( final Vector3 _position,
									   final Vector3 _offset,
									   final Vector3 _rotation,
									   final Vector3 _scale,
									   final int _startOrder,
									   final int _endOrder )
	{
		return assist.createClipDraw( _position, _offset, _rotation, _scale, _startOrder, _endOrder ) ;
	}

	/**
		Create a basic Draw object.
		Can be used for almost anything, except rendering text.
	*/
	public static Draw createDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _order )
	{
		return assist.createDraw( _position, _offset, _rotation, _scale, _order ) ;
	}

	/**
		Required to be implemented by the active renderer for 
		DrawAssist to be used.
	*/
	public interface Assist
	{
		public Draw amendUI( final Draw _draw, final boolean _ui ) ;

		public Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation ) ;
		public Draw amendUpdateType( final Draw _draw, final UpdateType _type ) ;

		public Draw forceUpdate( final Draw _draw ) ;

		public boolean isUI( final Draw _draw ) ;

		public TextDraw createTextDraw( final StringBuilder _text,
										final MalletFont _font,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order ) ;

		public TextDraw createTextDraw( final String _text,
										final MalletFont _font,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order ) ;

		public Draw createClipDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _startOrder,
									final int _endOrder ) ;
										
		public Draw createDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _order ) ;
	}
}
