package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.ui.gui.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public class UICheckbox extends UIElement
{
	private boolean checked = false ;

	private final Connect.Signal checkChanged = new Connect.Signal() ;

	public UICheckbox()
	{
		super() ;
		init() ;
	}

	private void init()
	{
		new InputComponent( this )
		{
			@Override
			public InputEvent.Action touchReleased( final InputEvent _input )
			{
				return mouseReleased( _input ) ;
			}

			@Override
			public InputEvent.Action mouseReleased( final InputEvent _input )
			{
				final UICheckbox parent = ( UICheckbox )getParent() ;
				parent.setChecked( !parent.isChecked() ) ;
				return InputEvent.Action.CONSUME ;
			}
		} ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			processInputEvent( _event ) ;
			switch( _event.getInputType() )
			{
				case MOUSE_MOVED :
				case TOUCH_MOVE  : return InputEvent.Action.PROPAGATE ;
				default          : return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public void setChecked( final boolean _checked )
	{
		if( checked != _checked )
		{
			checked = _checked ;
			UIElement.signal( this, checkChanged() ) ;
			makeDirty() ;
		}
	}

	public boolean isChecked()
	{
		return checked ;
	}

	public final Connect.Signal checkChanged()
	{
		return checkChanged ;
	}

	public static class Meta extends UIElement.Meta
	{
		public Meta()
		{
			super() ;
		}

		@Override
		public String getElementType()
		{
			return "UICHECKBOX" ;
		}
	}
}
