package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public class CameraAssist
{
	private static Assist assist ;

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

	public static Camera amendScreenResolution( final Camera _camera, final int _width, final int _height )
	{
		return assist.amendScreenResolution( _camera, _width, _height ) ;
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

		public Camera amendScreenResolution( final Camera _camera, final int _width, final int _height ) ;
		public Camera amendScreenOffset( final Camera _camera, final int _x, final int _y ) ;

		public boolean getPosition( final Camera _camera, final Vector3 _populate ) ;
		public boolean getRotation( final Camera _camera, final Vector3 _populate ) ;
		public boolean getScale( final Camera _camera, final Vector3 _populate ) ;

		public Camera createCamera( final String _id,
									final Vector3 _position,
									final Vector3 _rotation,
									final Vector3 _scale ) ;
	}
}
