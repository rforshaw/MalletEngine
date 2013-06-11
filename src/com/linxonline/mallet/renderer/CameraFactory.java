package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.resources.ResourceManager ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

public class CameraFactory
{
	private final static String REQUEST_TYPE = "REQUEST_TYPE" ;
	private final static String CAMERA = "CAMERA" ;
	private final static String POS = "POS" ;
	private final static String ACC = "ACC" ;

	public static Event setCameraEvent( final Vector3 _pos )
	{
		final Settings settings = new Settings() ;
		settings.addInteger( REQUEST_TYPE, CameraRequestType.SET_CAMERA_POSITION ) ;
		settings.addObject( POS, _pos ) ;
		return new Event( CAMERA, settings ) ;
	}

	public static Event updateCameraEvent( final Vector3 _acc )
	{
		final Settings settings = new Settings() ;
		settings.addInteger( REQUEST_TYPE, CameraRequestType.UPDATE_CAMERA_POSITION ) ;
		settings.addObject( ACC, _acc ) ;
		return new Event( CAMERA, settings ) ;
	}
}