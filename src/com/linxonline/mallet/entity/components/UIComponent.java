package com.linxonline.mallet.entity.components ;

import java.util.ArrayList ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;

public class UIComponent extends InputComponent
{
	private final ArrayList<UIElement> elements = new ArrayList<UIElement>() ;
	private final ArrayList<Event<?>> events = new ArrayList<Event<?>>() ;

	public UIComponent()
	{
		super( "UI", "UICOMPONENT", InputMode.UI ) ;
	}

	public UIComponent( final String _name )
	{
		super( _name, "UICOMPONENT", InputMode.UI ) ;
	}

	public UIComponent( final String _name, final String _group )
	{
		super( _name, _group, InputMode.UI ) ;
	}

	public UIComponent( final String _name, InputMode _mode )
	{
		super( _name, "UICOMPONENT", _mode ) ;
	}

	public UIComponent( final String _name, final String _group, InputMode _mode )
	{
		super( _name, _group, _mode ) ;
	}

	@Override
	public void setInputAdapterInterface( final InputAdapterInterface _adapter )
	{
		inputAdapter = _adapter ;
		for( final UIElement element : elements )
		{
			element.setInputAdapterInterface( _adapter ) ;
		}
	}

	public void addElement( final UIElement _element )
	{
		if( elements.contains( _element ) == false )
		{
			_element.setInputAdapterInterface( inputAdapter ) ;
			elements.add( _element ) ;
		}
	}

	public void removeElement( final UIElement _element )
	{
		elements.remove( _element ) ;
	}

	@Override
	public void update( final float _dt )
	{
		super.update( _dt ) ;
		for( final UIElement element : elements )
		{
			element.update( _dt, events ) ;
		}

		final EventController controller = this.getComponentEventController() ;
		for( final Event event : events )
		{
			controller.passEvent( event ) ;
		}
		events.clear() ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( super.passInputEvent( _event ) == InputEvent.Action.CONSUME )
		{
			return InputEvent.Action.CONSUME ;
		}

		for( final UIElement element : elements )
		{
			if( element.passInputEvent( _event ) == InputEvent.Action.CONSUME )
			{
				return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}
}