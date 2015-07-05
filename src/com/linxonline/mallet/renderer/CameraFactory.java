package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

/**
	Generate Events that can modify the Camera 
	within the Rendering System.
	At this moment the Rendering System only supports
	one camera, however the camera can be moved to 
	suggest multiple cameras in different locations.
*/
public final class CameraFactory
{
	private CameraFactory() {}

	public static Event setCameraPositionEvent( final Vector3 _pos )
	{
		final Settings settings = new Settings() ;
		settings.addObject( "REQUEST_TYPE", CameraRequestType.SET_CAMERA_POSITION ) ;
		settings.addObject( "POS", _pos ) ;
		return new Event( "CAMERA", settings ) ;
	}

	public static Event updateCameraPositionEvent( final Vector3 _acc )
	{
		final Settings settings = new Settings() ;
		settings.addObject( "REQUEST_TYPE", CameraRequestType.UPDATE_CAMERA_POSITION ) ;
		settings.addObject( "ACC", _acc ) ;
		return new Event( "CAMERA", settings ) ;
	}

	public static Event setCameraScaleEvent( final Vector3 _scale )
	{
		final Settings settings = new Settings() ;
		settings.addObject( "REQUEST_TYPE", CameraRequestType.SET_CAMERA_SCALE ) ;
		settings.addObject( "SCALE", _scale ) ;
		return new Event( "CAMERA", settings ) ;
	}

	public static Event updateCameraScaleEvent( final Vector3 _scale )
	{
		final Settings settings = new Settings() ;
		settings.addObject( "REQUEST_TYPE", CameraRequestType.UPDATE_CAMERA_SCALE ) ;
		settings.addObject( "SCALE", _scale ) ;
		return new Event( "CAMERA", settings ) ;
	}
}