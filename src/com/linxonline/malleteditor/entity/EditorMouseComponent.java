package com.linxonline.malleteditor.entity ;

import com.linxonline.mallet.renderer.CameraFactory ;
import com.linxonline.mallet.entity.components.RenderComponent ;
import com.linxonline.mallet.entity.components.MouseComponent ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.mallet.maths.* ;

public class EditorMouseComponent extends MouseComponent
{
	private boolean moveCamera = false ;

	private final Vector2 oldMouse = new Vector2() ;
	private final Vector3 mouseDiff = new Vector3() ;
	private final Vector3 scrollDiff = new Vector3( 1.0f, 1.0f, 1.0f ) ;

	private final Event cameraMoveEvent = CameraFactory.updateCameraPositionEvent( mouseDiff ) ;
	private final Event cameraScaleEvent = CameraFactory.setCameraScaleEvent( scrollDiff ) ;
	private final RenderComponent render ;

	public EditorMouseComponent( final RenderComponent _render )
	{
		render = _render ;
	}

	public void passInputEvent( final InputEvent _event )
	{
		super.passInputEvent( _event ) ; 		// Update Mouse Input
		final InputType eventType = _event.getInputType() ;

		switch( eventType )
		{
			case KEYBOARD_PRESSED  : updateKeyboardPressed( _event.getKeyCode() ) ;  break ;
			case KEYBOARD_RELEASED : updateKeyboardReleased( _event.getKeyCode() ) ; break ;
			case SCROLL_WHEEL      : updateScrollWheel( ( float )_event.mouseX ) ;   break ; // mouseX or mouseY defines the same ScrollWheel movement
		}
	}

	public void update( final float _dt )
	{
		super.update( _dt ) ;			// Updates the entity position to current mouse position
		mouseDiff.x = -( mouse.x - oldMouse.x ) ;
		mouseDiff.y = -( mouse.y - oldMouse.y ) ;

		if( ( mouse1Pressed == true && mouseMoved == true && moveCamera == true ) ||	// CTRL and Mouse 1 to move Camera
			( mouse2Pressed == true ) )													// Mouse 2 (Scroll button) to move Camera
		{
			render.passEvent( cameraMoveEvent ) ;
		}

		mouseMoved = false ;
		oldMouse.setXY( mouse.x, mouse.y ) ;
	}
	
	private void updateKeyboardPressed( KeyCode _code )
	{
		switch( _code )
		{
			case CTRL : moveCamera = true ;
		}
	}
	
	private void updateKeyboardReleased( KeyCode _code )
	{
		switch( _code )
		{
			case CTRL : moveCamera = false ;
		}
	}
	
	private void updateScrollWheel( final float _scroll )
	{
		scrollDiff.x -= _scroll * 0.01f ;
		scrollDiff.y -= _scroll * 0.01f ;
		
		if( scrollDiff.x <= 0.1f || scrollDiff.y <= 0.1f )
		{
			scrollDiff.x = 0.1f ;
			scrollDiff.y = 0.1f ;
		}
		
		render.passEvent( cameraScaleEvent ) ;
	}
}