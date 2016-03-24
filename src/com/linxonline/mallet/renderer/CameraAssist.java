package com.linxonline.mallet.renderer ;

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

	public interface Assist
	{
		public Camera getCamera() ;

		public Camera amendPosition( final Camera _camera, final float _x, final float _y, final float _z ) ;
		public Camera amendRotation( final Camera _camera, final float _x, final float _y, final float _z ) ;
		public Camera amendScale( final Camera _camera, final float _x, final float _y, final float _z ) ;
	}
}
