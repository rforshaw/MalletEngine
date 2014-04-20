package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.renderer.CameraFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.event.Event ;
import com.linxonline.mallet.maths.* ;

public class CameraComponent extends RenderComponent
{
	private final Event setPosition = CameraFactory.setCameraPositionEvent( new Vector3() ) ;
	private final Event updateEvent = CameraFactory.updateCameraPositionEvent( new Vector3() ) ;

	public CameraComponent() {}

	public void setPosition( Vector3 _pos )
	{
		final Settings settings = ( Settings )setPosition.getVariable() ;
		settings.addObject( "POS", _pos ) ;
		passEvent( setPosition ) ;
	}

	public void updateCameraPosition( Vector3 _acc )
	{
		final Settings settings = ( Settings )updateEvent.getVariable() ;
		settings.addObject( "ACC", _acc ) ;
		passEvent( updateEvent ) ;
	}
}