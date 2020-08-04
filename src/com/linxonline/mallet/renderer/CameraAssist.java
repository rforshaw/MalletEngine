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

		public Camera addCamera( final Camera _camera, final World _world ) ;
		public Camera removeCamera( final Camera _camera, final World _world ) ;

		public Camera createCamera( final String _id,
									final Vector3 _position,
									final Vector3 _rotation,
									final Vector3 _scale ) ;
	}
}
