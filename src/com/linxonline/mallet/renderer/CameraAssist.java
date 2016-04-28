package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.* ;

public class CameraAssist
{
	private static Assist assist ;

	public static void setAssist( final CameraAssist.Assist _assist )
	{
		assist = _assist ;
	}

	public static Camera getCamera()
	{
		return assist.getCamera() ;
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
		return assist.amendRotation( _camera, _x, _y, _z ) ;
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

	public interface Assist
	{
		public Camera getCamera() ;

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

		public boolean getPosition( final Camera _camera, final Vector3 _populate ) ;
		public boolean getRotation( final Camera _camera, final Vector3 _populate ) ;
		public boolean getScale( final Camera _camera, final Vector3 _populate ) ;
	}
}
