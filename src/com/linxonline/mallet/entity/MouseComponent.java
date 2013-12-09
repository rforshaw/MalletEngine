package com.linxonline.mallet.entity ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.entity.Entity ;

/*==============================================================*/
// MouseComponent - Provides components that tracks mouse		// 
// position													    //
/*==============================================================*/

public class MouseComponent extends Component 
							implements InputHandler
{
	private InputAdapterInterface inputAdapter = null ;
	private final Vector2 mouse = new Vector2() ;
	private boolean mouse1Pressed = false ;
	private boolean mouse2Pressed = false ;
	private boolean mouse3Pressed = false ;

	public MouseComponent()
	{
		super( "MOUSECOMPONENT", "INPUTCOMPONENT" ) ;
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		inputAdapter = _adapter ;
	}

	@Override
	public void update( final float _dt )
	{
		final Vector3 pos = parent.getPosition() ;
		pos.x = inputAdapter.convertInputToRenderX( mouse.x ) ;
		pos.y = inputAdapter.convertInputToRenderY( mouse.y ) ;
	}

	public void passInputEvent( final InputEvent _event )
	{
		final InputType eventType = _event.getInputType() ;

		switch( eventType )
		{
			case MOUSE_MOVED     :
			case TOUCH_MOVE      : updateMousePosition( _event ) ;    break ;
			case MOUSE1_PRESSED  : setMouse1Button( true ) ;          break ;
			case MOUSE1_RELEASED : setMouse1Button( false ) ;         break ;
			case MOUSE2_PRESSED  : setMouse2Button( true ) ;          break ;
			case MOUSE2_RELEASED : setMouse2Button( false ) ;         break ;
			case MOUSE3_PRESSED  : setMouse3Button( true ) ;          break ;
			case MOUSE3_RELEASED : setMouse3Button( false ) ;         break ;
		}
	}
	
	public void setMouse1Button( final boolean _pressed )
	{
		mouse1Pressed = _pressed ;
	}
	
	public void setMouse2Button( final boolean _pressed )
	{
		mouse2Pressed = _pressed ;
	}
	
	public void setMouse3Button( final boolean _pressed )
	{
		mouse3Pressed = _pressed ;
	}
	
	public void updateMousePosition( final InputEvent _event )
	{
		mouse.setXY( _event.mouseX, _event.mouseY ) ;
	}
}