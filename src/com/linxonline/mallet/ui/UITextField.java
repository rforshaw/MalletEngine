 package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.ui.gui.GUIText ;

public class UITextField extends UIElement
{
	private final EventController controller = new EventController() ;
	private final StringBuilder text = new StringBuilder() ;

	private int cursorIndex = 0 ;

	private final Connect.Signal cursorIndexChanged = new Connect.Signal() ;
	private final Connect.Signal textChanged = new Connect.Signal() ;
	private final Connect.Signal submitChanged = new Connect.Signal() ;

	public UITextField()
	{
		super() ;
		addEvent( new Event<EventController>( "ADD_BACKEND_EVENT", controller ) ) ;

		UIElement.connect( this, elementDestroyed(), new Connect.Slot<UITextField>()
		{
			@Override
			public void slot( final UITextField _this )
			{
				addEvent( new Event<EventController>( "REMOVE_BACKEND_EVENT", controller ) ) ;
			}
		} ) ;

		UIElement.connect( this, elementEngaged(), new Connect.Slot<UITextField>()
		{
			@Override
			public void slot( final UITextField _this )
			{
				controller.passEvent( new Event<Boolean>( "DISPLAY_SYSTEM_KEYBOARD", true ) ) ;
			}
		} ) ;

		UIElement.connect( this, elementDisengaged(), new Connect.Slot<UITextField>()
		{
			@Override
			public void slot( final UITextField _this )
			{
				controller.passEvent( new Event<Boolean>( "DISPLAY_SYSTEM_KEYBOARD", false ) ) ;
			}
		} ) ;

		setKeyPressedAction( new InputAction()
		{
			@Override
			public InputEvent.Action action( final UIElement.Component _listener, final InputEvent _event )
			{
				switch( _event.getKeyCode() )
				{
					case ENTER :
					{
						UIElement.signal( UITextField.this, submitChanged() ) ;
						return InputEvent.Action.CONSUME ;
					}
				}

				return _listener.keyPressed( _event ) ;
			}
		} ) ;
	}

	/**
		A UITextField is only considered unengaged once a mouse-click
		or touch even is activated outside of its area.
	*/
	@Override
	public boolean continueEngagement( final InputEvent _input )
	{
		switch( _input.getInputType() )
		{
			default             : return true ;
			case MOUSE1_PRESSED : return isIntersectInput( _input ) ;
			case TOUCH_DOWN     : return isIntersectInput( _input ) ;
		}
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		controller.update() ;
	}

	public final void setCursorIndex( final int _index )
	{
		if( cursorIndex != _index )
		{
			cursorIndex = _index ;
			UIElement.signal( this, cursorIndexChanged() ) ;
		}
	}

	public final int getCursorIndex()
	{
		return cursorIndex ;
	}

	public final StringBuilder getText()
	{
		return text ;
	}

	public final Connect.Signal cursorIndexChanged()
	{
		return cursorIndexChanged ;
	}

	public final Connect.Signal textChanged()
	{
		return textChanged ;
	}

	public final Connect.Signal submitChanged()
	{
		return submitChanged ;
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
			return "UITEXTFIELD" ;
		}
	}
}
