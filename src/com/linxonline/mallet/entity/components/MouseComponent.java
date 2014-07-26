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

public class MouseComponent extends Component 
							implements InputHandler
{
	protected InputAdapterInterface inputAdapter = null ;
	protected final Vector2 mouse = new Vector2() ;
	protected boolean mouseMoved    = false ;
	protected boolean mouse1Pressed = false ;
	protected boolean mouse2Pressed = false ;
	protected boolean mouse3Pressed = false ;

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
	public void passInitialEvents( final ArrayList<Event<?>> _events )
	{
		final Event<InputHandler> event = new Event<InputHandler>( "ADD_GAME_STATE_INPUT", this ) ;
		_events.add( event ) ;
	}

	@Override
	public void passFinalEvents( final ArrayList<Event<?>> _events )
	{
		final Event<InputHandler> event = new Event<InputHandler>( "REMOVE_GAME_STATE_INPUT", this ) ;
		_events.add( event ) ;
	}

	@Override
	public void update( final float _dt )
	{
		if( inputAdapter != null )
		{
			final Vector3 pos = parent.getPosition() ;
			pos.x = inputAdapter.convertInputToRenderX( mouse.x ) ;
			pos.y = inputAdapter.convertInputToRenderY( mouse.y ) ;
		}
	}

	public void passInputEvent( final InputEvent _event )
	{
		final InputType eventType = _event.getInputType() ;

		switch( eventType )
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
	}
}