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

	public static Camera getDefault()
	{
		return assist.getDefault() ;
	}

	public static Camera add( final Camera _camera )
	{
		return assist.add( _camera ) ;
	}

	public static Camera remove( final Camera _camera )
	{
		return assist.remove( _camera ) ;
	}

	public static Camera update( final Camera _camera )
	{
		return assist.update( _camera ) ;
	}

	public interface Assist
	{
		public Camera getDefault() ;

		public Camera add( final Camera _camera ) ;
		public Camera remove( final Camera _camera ) ;
		public Camera update( final Camera _camera ) ;
	}
}
