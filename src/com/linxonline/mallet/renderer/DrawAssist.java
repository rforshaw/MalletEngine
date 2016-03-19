package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.event.Event ;

public class DrawAssist
{
	private static Assist assist ;

	private DrawAssist() {}

	public static void setAssist( final DrawAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static Event<DrawDelegateCallback> constructDrawDelegate( DrawDelegateCallback _callback )
	{
		return new Event<DrawDelegateCallback>( "DRAW_DELEGATE", _callback ) ;
	}
	
	public static DrawData amendShape( final DrawData _draw, final Shape _shape )
	{
		return assist.amendShape( _draw, _shape ) ;
	}

	public static DrawData amendTexture( final DrawData _draw, final MalletTexture _texture )
	{
		return assist.amendTexture( _draw, _texture ) ;
	}

	public static DrawData amendClip( final DrawData _draw, final Shape _clipSpace, final Vector3 _position, final Vector3 _offset )
	{
		return assist.amendClip( _draw, _clipSpace, _position, _offset ) ;
	}

	public static DrawData amendRotate( final DrawData _draw, final float _x, final float _y, final float _z )
	{
		return assist.amendRotate( _draw, _x, _y, _z ) ;
	}

	public static DrawData amendScale( final DrawData _draw, final float _x, final float _y, final float _z )
	{
		return assist.amendScale( _draw, _x, _y, _z ) ;
	}

	public static DrawData amendPosition( final DrawData _draw, final float _x, final float _y, final float _z )
	{
		return assist.amendPosition( _draw, _x, _y, _z ) ;
	}

	public static DrawData amendText( final DrawData _draw, final String _text )
	{
		return assist.amendText( _draw, _text ) ;
	}

	public static DrawData amendUI( final DrawData _draw, final boolean _ui )
	{
		return assist.amendUI( _draw, _ui ) ;
	}

	public static DrawData amendInterpolation( final DrawData _draw, final Interpolation _interpolation )
	{
		return assist.amendInterpolation( _draw, _interpolation ) ;
	}

	public static DrawData amendUpdateType( final DrawData _draw, final UpdateType _type )
	{
		return assist.amendUpdateType( _draw, _type ) ;
	}

	public static DrawData attachProgram( final DrawData _draw, final String _key )
	{
		return assist.attachProgram( _draw, _key ) ;
	}

	public static DrawData forceUpdate( final DrawData _draw )
	{
		return assist.forceUpdate( _draw ) ;
	}

	public static DrawData createTextDraw( final String _text,
											final MalletFont _font,
											final Vector3 _position,
											final Vector3 _offset,
											final Vector3 _rotation,
											final Vector3 _scale,
											final int _order )
	{
		return assist.createTextDraw( _text, _font, _position, _offset, _rotation, _scale, _order ) ;
	}

	public static DrawData createDraw( final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order )
	{
		return assist.createDraw( _position, _offset, _rotation, _scale, _order ) ;
	}

	public interface Assist
	{
		public DrawData amendShape( final DrawData _draw, final Shape _shape ) ;
		public DrawData amendTexture( final DrawData _draw, final MalletTexture _texture ) ;
		public DrawData amendClip( final DrawData _draw, final Shape _clipSpace, final Vector3 _position, final Vector3 _offset ) ;
		public DrawData amendRotate( final DrawData _draw, final float _x, final float _y, final float _z ) ;
		public DrawData amendScale( final DrawData _draw, final float _x, final float _y, final float _z ) ;
		public DrawData amendPosition( final DrawData _draw, final float _x, final float _y, final float _z ) ;
		public DrawData amendText( final DrawData _draw, final String _text ) ;
		public DrawData amendUI( final DrawData _draw, final boolean _ui ) ;

		public DrawData amendInterpolation( final DrawData _draw, final Interpolation _interpolation ) ;
		public DrawData amendUpdateType( final DrawData _draw, final UpdateType _type ) ;

		public DrawData attachProgram( final DrawData _draw, final String _key ) ;
		public DrawData forceUpdate( final DrawData _draw ) ;

		public DrawData createTextDraw( final String _text,
										final MalletFont _font,
										final Vector3 _position,
										final Vector3 _offset,
										final Vector3 _rotation,
										final Vector3 _scale,
										final int _order ) ;

		public DrawData createDraw( final Vector3 _position,
									final Vector3 _offset,
									final Vector3 _rotation,
									final Vector3 _scale,
									final int _order ) ;
	}
}