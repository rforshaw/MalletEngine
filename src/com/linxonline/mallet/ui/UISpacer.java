package com.linxonline.mallet.ui ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UISpacer extends UIElement
{
	@Override
	public void update( final float _dt ) {}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		return InputEvent.Action.PROPAGATE ;
	}
}