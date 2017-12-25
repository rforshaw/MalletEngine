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

	/**
		If the UICheckbox is being added to a UILayout
		then you don't have to define the position, 
		offset, or length.
	*/
	public UITextField()
	{
		this( new Vector3(), new Vector3(), new Vector3(), null ) ;
	}

	public UITextField( final Vector3 _length )
	{
		this( new Vector3(), new Vector3(), _length, null ) ;
	}

	public UITextField( final Vector3 _offset,
					   final Vector3 _length )
	{
		this( new Vector3(), _offset, _length, null ) ;
	}

	public UITextField( final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _length )
	{
		this( _position, _offset, _length, null ) ;
	}

	public UITextField( final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _length,
					   final ABase<UIButton> _listener )
	{
		super( _position, _offset, _length ) ;
		addListener( _listener ) ;
		addEvent( new Event<EventController>( "ADD_BACKEND_EVENT", controller ) ) ;
	}

	@Override
	public void engage()
	{
		super.engage() ;
		controller.passEvent( new Event<Boolean>( "DISPLAY_SYSTEM_KEYBOARD", true ) ) ;
	}

	@Override
	public void disengage()
	{
		super.disengage() ;
		controller.passEvent( new Event<Boolean>( "DISPLAY_SYSTEM_KEYBOARD", false ) ) ;
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
		public Meta() {}

		@Override
		public String getElementType()
		{
			return "UITEXTFIELD" ;
		}
	}
}
