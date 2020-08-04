package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.renderer.Camera ;
import com.linxonline.mallet.renderer.CameraAssist ;

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

	public MouseComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public MouseComponent( final Entity _parent, final Entity.AllowEvents _allow )
	{
		this( _parent, _allow, InputComponent.InputMode.UI ) ;
	}

	public MouseComponent( final Entity _parent, final InputComponent.InputMode _mode )
	{
		this( _parent, Entity.AllowEvents.YES, _mode ) ;
	}

	public MouseComponent( final Entity _parent, final Entity.AllowEvents _allow, final InputComponent.InputMode _mode )
	{
		super( _parent, _allow, _mode ) ;
	}

	@Override
	public void processInputEvent( final InputEvent _event )
	{
		switch( _event.getInputType() )
		{
			case MOUSE_MOVED     : 
			case TOUCH_MOVE      : mouseMoved = true ;
								   mouseMoved( _event ) ;
								   updateMousePosition( _event ) ; break ;
			case TOUCH_DOWN      :
			case MOUSE1_PRESSED  : mouse1Pressed = true ;
								   mousePressed( _event ) ;
								   updateMousePosition( _event ) ; break ;
			case TOUCH_UP        :
			case MOUSE1_RELEASED : mouse1Pressed = false ;
								   mouseReleased( _event ) ;
								   updateMousePosition( _event ) ; break ;
			case MOUSE2_PRESSED  : mouse2Pressed = true ;          break ;
			case MOUSE2_RELEASED : mouse2Pressed = false ;         break ;
			case MOUSE3_PRESSED  : mouse3Pressed = true ;          break ;
			case MOUSE3_RELEASED : mouse3Pressed = false ;         break ;
			default              : break ;
		}
	}

	private void updateMousePosition( final InputEvent _event )
	{
		mouse.setXY( _event.mouseX, _event.mouseY ) ;
		final Camera camera = CameraAssist.getDefaultCamera() ;

		mouse.x = camera.convertInputToX( mouse.x ) ;
		mouse.y = camera.convertInputToY( mouse.y ) ;
		applyMousePosition( mouse ) ;
	}

	public void mouseMoved( final InputEvent _event ) {}

	public void mousePressed( final InputEvent _event ) {}

	public void mouseReleased( final InputEvent _event ) {}
	
	public void applyMousePosition( final Vector2 _mouse ) {}
}
