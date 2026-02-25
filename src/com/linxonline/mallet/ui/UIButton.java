package com.linxonline.mallet.ui ;

import com.linxonline.mallet.input.* ;

/**
	Used to determine if the user has acted within a UI area.

	When the user has clicked, rolled over, or the location 
	is reset to a neutral position an event defined by the 
	developer is sent through the entity's event-system.

	The event can then be picked up by other components such as a 
	render-component to modify the visual element of the entity.
*/
public class UIButton extends UIElement
{
	private final Connect.Signal pressed = new Connect.Signal() ;
	private final Connect.Signal released = new Connect.Signal() ;

	public UIButton()
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
			public InputEvent.Action touchPressed( final InputEvent _input )
			{
				return mousePressed( _input ) ;
			}

			@Override
			public InputEvent.Action mouseReleased( final InputEvent _input )
			{
				final UIButton parent = ( UIButton )getParent() ;
				UIElement.signal( parent, parent.released() ) ;
				return InputEvent.Action.CONSUME ;
			}

			@Override
			public InputEvent.Action mousePressed( final InputEvent _input )
			{
				final UIButton parent = ( UIButton )getParent() ;
				UIElement.signal( parent, parent.pressed() ) ;
				return InputEvent.Action.CONSUME ;
			}
		} ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isDisabled() == true )
		{
			return InputEvent.Action.PROPAGATE ;
		}

		if( isIntersectInput( _event ) == true )
		{
			final InputEvent.Action action = processInputEvent( _event ) ;
			switch( _event.getInputType() )
			{
				case MOUSE_MOVED :
				case TOUCH_MOVE  : return InputEvent.Action.PROPAGATE ;
				default          : return action ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public final Connect.Signal pressed()
	{
		return pressed ;
	}

	public final Connect.Signal released()
	{
		return released ;
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
			return "UIBUTTON" ;
		}
	}
}
