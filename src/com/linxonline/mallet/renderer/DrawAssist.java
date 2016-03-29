package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

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
public class DrawAssist
{
	private static final Event DRAW_CLEAN = new Event( "DRAW_CLEAN", null ) ; 

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
	public static Event constructDrawClean()
	{
		return DRAW_CLEAN ;
	}

	public static Draw amendShape( final Draw _draw, final Shape _shape )
	{
		return assist.amendShape( _draw, _shape ) ;
	}

	public static Draw amendTexture( final Draw _draw, final MalletTexture _texture )
	{
		return assist.amendTexture( _draw, _texture ) ;
	}

	public static Draw amendClip( final Draw _draw, final Shape _clipSpace, final Vector3 _position, final Vector3 _offset )
	{
		return assist.amendClip( _draw, _clipSpace, _position, _offset ) ;
	}

	public static Draw amendRotate( final Draw _draw, final float _x, final float _y, final float _z )
	{
		return assist.amendRotate( _draw, _x, _y, _z ) ;
	}

	public static Draw amendScale( final Draw _draw, final float _x, final float _y, final float _z )
	{
		return assist.amendScale( _draw, _x, _y, _z ) ;
	}

	public static Draw amendPosition( final Draw _draw, final float _x, final float _y, final float _z )
	{
		return assist.amendPosition( _draw, _x, _y, _z ) ;
	}

	public static Draw amendText( final Draw _draw, final String _text )
	{
		return assist.amendText( _draw, _text ) ;
	}

	public static Draw amendUI( final Draw _draw, final boolean _ui )
	{
		return assist.amendUI( _draw, _ui ) ;
	}

	public Draw amendColour( final Draw _draw, final MalletColour _colour )
	{
		return assist.amendColour( _draw, _colour ) ;
	}

	public static Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation )
	{
		return assist.amendInterpolation( _draw, _interpolation ) ;
	}

	public static Draw amendUpdateType( final Draw _draw, final UpdateType _type )
	{
		return assist.amendUpdateType( _draw, _type ) ;
	}

	public static Draw attachProgram( final Draw _draw, final String _key )
	{
		return assist.attachProgram( _draw, _key ) ;
	}

	public static Draw forceUpdate( final Draw _draw )
	{
		return assist.forceUpdate( _draw ) ;
	}

	public static Shape getDrawShape( final Draw _draw )
	{
		return assist.getDrawShape( _draw ) ;
	}

	public static int getTextureSize( final Draw _draw )
	{
		return assist.getTextureSize( _draw ) ;
	}

	public static MalletTexture getTexture( final Draw _draw, final int _index )
	{
		return assist.getTexture( _draw, _index ) ;
	}

	public static void clearTextures( final Draw _draw )
	{
		assist.clearTextures( _draw ) ;
	}

	public static Vector3 getRotate( final Draw _draw )
	{
		return assist.getRotate( _draw ) ;
	}

	public static Vector3 getScale( final Draw _draw )
	{
		return assist.getScale( _draw ) ;
	}

	public static Vector3 getPosition( final Draw _draw )
	{
		return assist.getPosition( _draw ) ;
	}

	public static String getText( final Draw _draw )
	{
		return assist.getText( _draw ) ;
	}

	public static MalletColour getColour( final Draw _draw )
	{
		return assist.getColour( _draw ) ;
	}

	public static boolean isUI( final Draw _draw )
	{
		return assist.isUI( _draw ) ;
	}

	/**
		Create a Text Draw object.
		Handles the nuances of text rendering and ensures a performant display.
	*/
	public static Draw createTextDraw( final String _text,
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
		public Draw amendShape( final Draw _draw, final Shape _shape ) ;
		public Draw amendTexture( final Draw _draw, final MalletTexture _texture ) ;
		public Draw amendClip( final Draw _draw, final Shape _clipSpace, final Vector3 _position, final Vector3 _offset ) ;
		public Draw amendRotate( final Draw _draw, final float _x, final float _y, final float _z ) ;
		public Draw amendScale( final Draw _draw, final float _x, final float _y, final float _z ) ;
		public Draw amendPosition( final Draw _draw, final float _x, final float _y, final float _z ) ;
		public Draw amendText( final Draw _draw, final String _text ) ;
		public Draw amendUI( final Draw _draw, final boolean _ui ) ;
		public Draw amendColour( final Draw _draw, final MalletColour _colour ) ;

		public Draw amendInterpolation( final Draw _draw, final Interpolation _interpolation ) ;
		public Draw amendUpdateType( final Draw _draw, final UpdateType _type ) ;

		public Draw attachProgram( final Draw _draw, final String _key ) ;
		public Draw forceUpdate( final Draw _draw ) ;

		public Shape getDrawShape( final Draw _draw ) ;
		public Vector3 getRotate( final Draw _draw ) ;
		public Vector3 getScale( final Draw _draw ) ;
		public Vector3 getPosition( final Draw _draw ) ;
		public String getText( final Draw _draw ) ;
		public MalletColour getColour( final Draw _draw ) ;
		public boolean isUI( final Draw _draw ) ;

		public int getTextureSize( final Draw _draw ) ;
		public MalletTexture getTexture( final Draw _draw, final int _index ) ;
		public void clearTextures( final Draw _draw ) ;

		public Draw createTextDraw( final String _text,
										final MalletFont _font,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order ) ;

		public Draw createDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _order ) ;
	}
}