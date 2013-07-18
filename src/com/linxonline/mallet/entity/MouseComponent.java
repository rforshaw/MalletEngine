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

	public MouseComponent()
	{
		super( "MOUSECOMPONENT", "INPUTCOMPONENT" ) ;
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		//System.out.println( "Setting input Adapter: " + _adapter ) ;
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

		if( eventType == InputType.MOUSE_MOVED ||
			eventType == InputType.TOUCH_MOVE )
		{
			mouse.setXY( _event.mouseX, _event.mouseY ) ;
		}
	}
}