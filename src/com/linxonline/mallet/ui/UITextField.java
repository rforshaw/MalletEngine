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
	}

	@Override
	public void refresh()
	{
		super.refresh() ;
		controller.update() ;
	}

	public void setCursorIndex( final int _index )
	{
		if( cursorIndex != _index )
		{
			cursorIndex = _index ;
			UIElement.signal( this, cursorIndexChanged() ) ;
		}
	}

	public int getCursorIndex()
	{
		return cursorIndex ;
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public Connect.Signal cursorIndexChanged()
	{
		return cursorIndexChanged ;
	}

	public Connect.Signal textChanged()
	{
		return textChanged ;
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
