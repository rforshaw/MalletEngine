package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public final class CameraAssist
{
	private static Assist assist ;

	private CameraAssist() {}

	public static void setAssist( final CameraAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static Camera getDefaultCamera()
	{
		return assist.getDefaultCamera() ;
	}

	public static Camera amendOrthographic( final Camera _camera,
											final float _top,
											final float _bottom,
											final float _left,
											final float _right,
											final float _near,
											final float _far )
	{
		return assist.amendOrthographic( _camera, _top, _bottom, _left, _right, _near, _far ) ;
	}

	public static Camera amendPosition( final Camera _camera, final float _x, final float _y, final float _z )
	{
		return assist.amendPosition( _camera, _x, _y, _z ) ;
	}

	public static Camera amendRotation( final Camera _camera, final float _x, final float _y, final float _z )
	{
		return assist.amendRotation( _camera, _x, _y, _z ) ;
	}

	public static Camera amendScale( final Camera _camera, final float _x, final float _y, final float _z )
	{
		return assist.amendScale( _camera, _x, _y, _z ) ;
	}

	public static Camera amendUIPosition( final Camera _camera, final float _x, final float _y, final float _z )
	{
		return assist.amendUIPosition( _camera, _x, _y, _z ) ;
	}

	public static Camera amendScreenResolution( final Camera _camera, final int _width, final int _height )
	{
		return assist.amendScreenResolution( _camera, _width, _height ) ;
	}

	public static Camera amendDisplayResolution( final Camera _camera, final int _width, final int _height )
	{
		return assist.amendDisplayResolution( _camera, _width, _height ) ;
	}

	public static Camera amendDisplayOffset( final Camera _camera, final int _x, final int _y )
	{
		return assist.amendDisplayOffset( _camera, _x, _y ) ;
	}

	public static Camera amendScreenOffset( final Camera _camera, final int _x, final int _y )
	{
		return assist.amendScreenOffset( _camera, _x, _y ) ;
	}

	public static boolean getPosition( final Camera _camera, final Vector3 _populate )
	{
		return assist.getPosition( _camera, _populate ) ;
	}

	public static boolean getRotation( final Camera _camera, final Vector3 _populate )
	{
		return assist.getRotation( _camera, _populate ) ;
	}

	public static boolean getScale( final Camera _camera, final Vector3 _populate )
	{
		return assist.getScale( _camera, _populate ) ;
	}

	public static boolean getDimensions( final Camera _camera, final Vector3 _populate )
	{
		return assist.getDimensions( _camera, _populate ) ;
	}

	public static boolean getUIPosition( final Camera _camera, final Vector3 _populate )
	{
		return assist.getUIPosition( _camera, _populate ) ;
	}

	public static float convertInputToCameraX( final Camera _camera, final float _inputX )
	{
		return assist.convertInputToCameraX( _camera, _inputX ) ;
	}

	public static float convertInputToCameraY( final Camera _camera, final float _inputY )
	{
		return assist.convertInputToCameraY( _camera, _inputY ) ;
	}

	public static float convertInputToUICameraX( final Camera _camera, final float _inputX )
	{
		return assist.convertInputToUICameraX( _camera, _inputX ) ;
	}

	public static float convertInputToUICameraY( final Camera _camera, final float _inputY )
	{
		return assist.convertInputToUICameraY( _camera, _inputY ) ;
	}

	public static Camera addCamera( final Camera _camera, final World _world )
	{
		return assist.addCamera( _camera, _world ) ;
	}

	public static Camera removeCamera( final Camera _camera, final World _world )
	{
		return assist.removeCamera( _camera, _world ) ;
	}

	public static Camera createCamera( final String _id,
									   final Vector3 _position,
									   final Vector3 _rotation,
									   final Vector3 _scale )
	{
		return assist.createCamera( _id, _position, _rotation, _scale ) ;
	}

	public interface Assist
	{
		public Camera getDefaultCamera() ;

		public Camera amendOrthographic( final Camera _camera,
										 final float _top,
										 final float _bottom,
										 final float _left,
										 final float _right,
										 final float _near,
										 final float _far ) ;

		public Camera amendPosition( final Camera _camera, final float _x, final float _y, final float _z ) ;
		public Camera amendRotation( final Camera _camera, final float _x, final float _y, final float _z ) ;
		public Camera amendScale( final Camera _camera, final float _x, final float _y, final float _z ) ;

		public Camera amendUIPosition( final Camera _camera, final float _x, final float _y, final float _z ) ;

		public Camera amendScreenResolution( final Camera _camera, final int _width, final int _height ) ;
		public Camera amendScreenOffset( final Camera _camera, final int _x, final int _y ) ;

		public Camera amendDisplayResolution( final Camera _camera, final int _width, final int _height ) ;
		public Camera amendDisplayOffset( final Camera _camera, final int _x, final int _y ) ;

		public boolean getPosition( final Camera _camera, final Vector3 _populate ) ;
		public boolean getRotation( final Camera _camera, final Vector3 _populate ) ;
		public boolean getScale( final Camera _camera, final Vector3 _populate ) ;
		public boolean getDimensions( final Camera _camera, final Vector3 _populate ) ;

		public boolean getUIPosition( final Camera _camera, final Vector3 _populate ) ;

		public float convertInputToCameraX( final Camera _camera, final float _inputX ) ;
		public float convertInputToCameraY( final Camera _camera, final float _inputY ) ;

		public float convertInputToUICameraX( final Camera _camera, final float _inputX ) ;
		public float convertInputToUICameraY( final Camera _camera, final float _inputY ) ;

		public Camera addCamera( final Camera _camera, final World _world ) ;
		public Camera removeCamera( final Camera _camera, final World _world ) ;

		public Camera createCamera( final String _id,
									final Vector3 _position,
									final Vector3 _rotation,
									final Vector3 _scale ) ;
	}
}
