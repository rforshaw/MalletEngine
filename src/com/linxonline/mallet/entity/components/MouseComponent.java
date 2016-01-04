package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.entity.Entity ;

/*==============================================================*/
// MouseComponent - Provides components that tracks mouse		// 
// position													    //
/*==============================================================*/
public class MouseComponent extends InputComponent
{
	protected final Vector2 mouse = new Vector2() ;
	protected boolean mouseMoved    = false ;
	protected boolean mouse1Pressed = false ;
	protected boolean mouse2Pressed = false ;
	protected boolean mouse3Pressed = false ;

	public MouseComponent()
	{
		this( InputComponent.InputMode.UI ) ;
	}

	public MouseComponent( InputComponent.InputMode _mode )
	{
		super( "MOUSECOMPONENT", "INPUTCOMPONENT", _mode ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
	}

	public void processInputEvent( final InputEvent _event )
	{
		switch( _event.getInputType() )
		{
			case MOUSE_MOVED     : 
			case TOUCH_MOVE      : mouseMoved = true ;
								   updateMousePosition( _event ) ; break ;
			case MOUSE1_PRESSED  : mouse1Pressed = true ;          break ;
			case MOUSE1_RELEASED : mouse1Pressed = false ;         break ;
			case MOUSE2_PRESSED  : mouse2Pressed = true ;          break ;
			case MOUSE2_RELEASED : mouse2Pressed = false ;         break ;
			case MOUSE3_PRESSED  : mouse3Pressed = true ;          break ;
			case MOUSE3_RELEASED : mouse3Pressed = false ;         break ;
		}
	}

	public void updateMousePosition( final InputEvent _event )
	{
		mouse.setXY( _event.mouseX, _event.mouseY ) ;
		if( inputAdapter != null )
		{
			final Vector3 pos = parent.getPosition() ;
			pos.x = inputAdapter.convertInputToRenderX( mouse.x ) ;
			pos.y = inputAdapter.convertInputToRenderY( mouse.y ) ;
		}
	}
}